package com.ozonehis.analytics.runtime;

import com.ozonehis.analytics.config.AnalyticsConfig;
import com.ozonehis.analytics.pipeline.ExecutionMode;
import com.ozonehis.analytics.pipeline.Job;
import com.ozonehis.analytics.pipeline.Pipeline;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.apache.flink.configuration.PipelineOptions;
import org.apache.flink.table.api.EnvironmentSettings;
import org.apache.flink.table.api.TableEnvironment;
import org.apache.flink.table.api.TableResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Executes a {@link Pipeline} on a Flink cluster.
 *
 * <p>This is the only class that talks to Flink. Everything about <em>how</em> the pipeline runs —
 * parallelism, checkpointing, state backend, S3, high availability — comes from the cluster's own
 * {@code config.yaml}, not from here. The job declares only what to compute.
 *
 * <p>The pipeline is expressed entirely in SQL, so it uses {@link TableEnvironment} rather than
 * {@code StreamTableEnvironment}: there is no DataStream interop to bridge, and the same
 * abstraction serves both streaming and batch.
 */
public final class FlinkRunner {

    private static final Logger LOG = LoggerFactory.getLogger(FlinkRunner.class);

    private final AnalyticsConfig config;

    private final RunningJobs runningJobs;

    public FlinkRunner(AnalyticsConfig config) {
        this(config, RestRunningJobs.fromEnvironment());
    }

    /**
     * @param runningJobs consulted before a streaming pipeline is submitted, so that only the jobs
     *     the cluster is missing are submitted and jobs running an outdated query are replaced.
     *     Injected so the decision is testable without a cluster.
     */
    public FlinkRunner(AnalyticsConfig config, RunningJobs runningJobs) {
        this.config = Objects.requireNonNull(config, "config");
        this.runningJobs = Objects.requireNonNull(runningJobs, "runningJobs");
    }

    /**
     * Submits each of the pipeline's {@link Job}s as its own Flink job. A streaming pipeline returns
     * once every job is submitted and running; a batch pipeline additionally waits for all of them
     * to finish, so that a failure in any one surfaces as a non-zero exit.
     *
     * <p>Each job gets its own {@link TableEnvironment}: a source table is registered per job, which
     * is what lets a streaming job scope its Kafka consumer group to itself instead of sharing one
     * catalog — and one set of group ids — across every job.
     *
     * @throws Exception if submission fails, or if a batch job fails while running
     */
    public void run(Pipeline pipeline) throws Exception {
        LOG.info("Starting pipeline '{}' in {} mode", pipeline.name(), pipeline.mode());

        List<Job> jobs = pipeline.jobs();
        if (jobs.isEmpty()) {
            throw new IllegalStateException(
                    "Pipeline '" + pipeline.name() + "' produced no jobs; check the configured source and query paths");
        }

        // Only streaming reconciles. Batch and export run in application mode on a cluster created
        // for that run, so there is never a previous job to collide with, and skipping one would
        // silently turn a re-run into a no-op.
        if (pipeline.mode() == ExecutionMode.STREAMING) {
            Reconciliation plan = reconcile(jobs, runningJobs.active());
            if (plan.toSubmit().isEmpty()) {
                LOG.info(
                        "All {} job(s) of '{}' are already running their current query; nothing to submit",
                        jobs.size(),
                        pipeline.name());
                return;
            }
            // Cancel before submitting: the replacement needs the slots the outdated job holds, and
            // two versions writing the same table at once would race. The replacement starts without
            // state, so it reads its topics from the beginning and rebuilds the whole table.
            for (RunningJobs.ActiveJob stale : plan.toCancel()) {
                LOG.info(
                        "Job '{}' ({}) runs an outdated query; cancelling it so it can be replaced",
                        stale.name(),
                        stale.id());
                runningJobs.cancel(stale.id());
            }
            if (plan.toSubmit().size() < jobs.size()) {
                LOG.info(
                        "{} of {} job(s) already running their current query; submitting the other {}",
                        jobs.size() - plan.toSubmit().size(),
                        jobs.size(),
                        plan.toSubmit().size());
            }
            jobs = plan.toSubmit();
        }

        LOG.info("Submitting {} as {} independent job(s)", pipeline.name(), jobs.size());
        List<TableResult> running = new ArrayList<>(jobs.size());
        for (Job job : jobs) {
            running.add(submit(pipeline.mode(), job));
        }

        if (pipeline.mode() == ExecutionMode.BATCH) {
            // Await every batch job. Without this the process would exit successfully the moment the
            // jobs were accepted, and a failed run would look like a clean one. Submitting all first
            // and awaiting after lets them run concurrently rather than one at a time. A streaming
            // job runs until cancelled, so there is nothing to await.
            LOG.info("Awaiting completion of {} batch job(s)", running.size());
            for (TableResult result : running) {
                result.await();
            }
            LOG.info("Pipeline '{}' completed", pipeline.name());
        }
    }

    /** What to do to bring a cluster in line with a streaming pipeline. */
    record Reconciliation(List<RunningJobs.ActiveJob> toCancel, List<Job> toSubmit) {}

    /**
     * Compares the pipeline's jobs with what the cluster reports {@code active}. Pure, so the rule
     * can be asserted on directly.
     *
     * <ul>
     *   <li>A job whose {@linkplain #clusterName cluster name}, fingerprint included, is active is
     *       left alone.
     *   <li>Otherwise every active job of the same table is outdated: cancel it, then submit the
     *       job. That covers a changed query and a job submitted by a build that did not name jobs
     *       with a fingerprint yet, which is replaced once.
     *   <li>Jobs on the cluster that belong to no job of this pipeline are never touched.
     * </ul>
     *
     * Both lists keep the pipeline's own order.
     */
    static Reconciliation reconcile(List<Job> jobs, List<RunningJobs.ActiveJob> active) {
        List<RunningJobs.ActiveJob> toCancel = new ArrayList<>();
        List<Job> toSubmit = new ArrayList<>();
        for (Job job : jobs) {
            String current = clusterName(job);
            if (active.stream().anyMatch(running -> running.name().equals(current))) {
                LOG.debug("Job '{}' is already running its current query; not resubmitting it", current);
                continue;
            }
            active.stream().filter(running -> sameTable(running.name(), job)).forEach(toCancel::add);
            toSubmit.add(job);
        }
        return new Reconciliation(List.copyOf(toCancel), List.copyOf(toSubmit));
    }

    /**
     * The name a streaming job runs under on the cluster: its own name, {@code @}, and the
     * {@linkplain JobFingerprint fingerprint} of what it runs, e.g. {@code streaming-patients@1a2b3c4d}.
     * A changed query gives a different name, which is how an outdated job is recognized.
     */
    static String clusterName(Job job) {
        return job.name() + "@" + JobFingerprint.of(job);
    }

    private static boolean sameTable(String clusterName, Job job) {
        return clusterName.equals(job.name()) || clusterName.startsWith(job.name() + "@");
    }

    private TableEnvironment createTableEnvironment(ExecutionMode mode) {
        EnvironmentSettings.Builder settings = EnvironmentSettings.newInstance();
        return TableEnvironment.create(
                switch (mode) {
                    case STREAMING -> settings.inStreamingMode().build();
                    case BATCH -> settings.inBatchMode().build();
                });
    }

    // registerCatalog(String, Catalog) is deprecated in favour of createCatalog(String,
    // CatalogDescriptor), which is options/factory-based. We deliberately construct dialect-specific
    // catalog instances (PostgresCatalog/MySqlCatalog from the split JDBC connector) and register
    // them directly, so the instance-based call is the right fit; the deprecation is suppressed
    // rather than worked around.
    @SuppressWarnings("deprecation")
    private void registerCatalogs(TableEnvironment tableEnv) {
        for (AnalyticsConfig.Catalog catalog : config.catalogs()) {
            LOG.info("Registering {} catalog '{}'", catalog.dialect(), catalog.name());
            tableEnv.registerCatalog(
                    catalog.name(), Catalogs.create(catalog, getClass().getClassLoader()));
        }
    }

    /** Builds an isolated environment for one job, registers its tables, and submits its INSERT. */
    private TableResult submit(ExecutionMode mode, Job job) {
        if (job.tableDefinitions().isEmpty()) {
            throw new IllegalStateException(
                    "Job '" + job.name() + "' declared no tables; check the configured source paths");
        }

        TableEnvironment tableEnv = createTableEnvironment(mode);
        // Without this Flink assigns its own name (e.g. "insert-into_catalog.db.table"), so the job
        // this code deliberately named would still show up unnamed everywhere that matters: the Web
        // UI, `flink list`, metrics tags. Each job has its own TableEnvironment, so this only scopes
        // to the one job being submitted. Streaming jobs carry the fingerprint of their query in the
        // name, which is what the next run compares against to spot an outdated job.
        tableEnv.getConfig().set(PipelineOptions.NAME, mode == ExecutionMode.STREAMING ? clusterName(job) : job.name());
        registerCatalogs(tableEnv);
        job.tableDefinitions().forEach(tableEnv::executeSql);

        LOG.info("Submitting job '{}'", tableEnv.getConfig().get(PipelineOptions.NAME));
        TableResult result = tableEnv.executeSql(job.insertStatement());
        result.getJobClient()
                .ifPresent(client -> LOG.info("Submitted Flink job {} for '{}'", client.getJobID(), job.name()));
        return result;
    }
}
