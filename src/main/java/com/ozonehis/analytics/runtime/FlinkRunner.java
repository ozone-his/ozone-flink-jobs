package com.ozonehis.analytics.runtime;

import com.ozonehis.analytics.config.AnalyticsConfig;
import com.ozonehis.analytics.pipeline.ExecutionMode;
import com.ozonehis.analytics.pipeline.Pipeline;
import java.util.List;
import java.util.Objects;
import org.apache.flink.table.api.EnvironmentSettings;
import org.apache.flink.table.api.StatementSet;
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

    public FlinkRunner(AnalyticsConfig config) {
        this.config = Objects.requireNonNull(config, "config");
    }

    /**
     * Registers catalogs and tables, then submits the pipeline's INSERT statements as a single
     * Flink job.
     *
     * @throws Exception if submission fails, or if a batch pipeline fails while running
     */
    public void run(Pipeline pipeline) throws Exception {
        LOG.info("Starting pipeline '{}' in {} mode", pipeline.name(), pipeline.mode());

        TableEnvironment tableEnv = createTableEnvironment(pipeline.mode());
        registerCatalogs(tableEnv);
        registerTables(tableEnv, pipeline);
        submit(tableEnv, pipeline);
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

    private void registerTables(TableEnvironment tableEnv, Pipeline pipeline) {
        List<String> definitions = pipeline.tableDefinitions();
        if (definitions.isEmpty()) {
            throw new IllegalStateException(
                    "Pipeline '" + pipeline.name() + "' declared no tables; check the configured source paths");
        }

        LOG.info("Registering {} table(s)", definitions.size());
        definitions.forEach(tableEnv::executeSql);
    }

    /**
     * Submits every statement as one {@link StatementSet}.
     *
     * <p>Submitting them together lets Flink plan them as a single job, so sinks reading the same
     * source share one scan instead of each opening its own, and the pipeline gets a single
     * checkpoint and recovery boundary. The trade-off is a shared failure domain: a failure in one
     * sink restarts the whole job.
     */
    private void submit(TableEnvironment tableEnv, Pipeline pipeline) throws Exception {
        List<String> statements = pipeline.insertStatements();
        if (statements.isEmpty()) {
            throw new IllegalStateException("Pipeline '" + pipeline.name()
                    + "' produced no INSERT statements; check the configured query paths");
        }

        StatementSet statementSet = tableEnv.createStatementSet();
        statements.forEach(statementSet::addInsertSql);

        LOG.info("Submitting {} statement(s) as a single job", statements.size());
        TableResult result = statementSet.execute();
        result.getJobClient()
                .ifPresent(
                        client -> LOG.info("Submitted job {} for pipeline '{}'", client.getJobID(), pipeline.name()));

        if (pipeline.mode() == ExecutionMode.BATCH) {
            // Awaiting surfaces a batch failure as an exception. Without it the process would exit
            // successfully the moment the job was accepted, and a failed run would look like a
            // clean one. A streaming job runs until cancelled, so there is nothing to await.
            LOG.info("Awaiting completion of pipeline '{}'", pipeline.name());
            result.await();
            LOG.info("Pipeline '{}' completed", pipeline.name());
        }
    }
}
