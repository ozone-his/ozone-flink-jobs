package com.ozonehis.analytics.runtime;

import static org.assertj.core.api.Assertions.assertThat;

import com.ozonehis.analytics.pipeline.Job;
import com.ozonehis.analytics.runtime.FlinkRunner.Reconciliation;
import com.ozonehis.analytics.runtime.RunningJobs.ActiveJob;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * Resubmitting a job the cluster is already running duplicates it, and leaving a job that runs an
 * outdated query means a changed query never takes effect. So the runner submits only what is
 * missing and replaces what is outdated. Both halves of that decision are pure and asserted on here
 * with no cluster.
 */
class ReconciliationTest {

    private static Job job(String name, String query) {
        return new Job(name, List.of("CREATE TABLE t (id INT)"), "INSERT INTO t " + query);
    }

    private static final Job PATIENTS = job("streaming-patients", "SELECT 1");
    private static final Job VISITS = job("streaming-visits", "SELECT 2");
    private static final Job ENCOUNTERS = job("streaming-encounters", "SELECT 3");
    private static final List<Job> THREE = List.of(PATIENTS, VISITS, ENCOUNTERS);

    /** The job as the cluster reports it once this build submitted it. */
    private static ActiveJob running(String id, Job job) {
        return new ActiveJob(id, FlinkRunner.clusterName(job));
    }

    @Test
    void submitsEverythingWhenTheClusterIsEmpty() {
        Reconciliation plan = FlinkRunner.reconcile(THREE, List.of());

        assertThat(plan.toSubmit()).isEqualTo(THREE);
        assertThat(plan.toCancel()).isEmpty();
    }

    @Test
    void submitsNothingWhenEveryJobIsAlreadyRunningItsCurrentQuery() {
        Reconciliation plan = FlinkRunner.reconcile(
                THREE, List.of(running("1", PATIENTS), running("2", VISITS), running("3", ENCOUNTERS)));

        assertThat(plan.toSubmit()).isEmpty();
        assertThat(plan.toCancel()).isEmpty();
    }

    @Test
    void submitsOnlyTheMissingJobsAfterAPartialRecovery() {
        Reconciliation plan = FlinkRunner.reconcile(THREE, List.of(running("1", PATIENTS), running("3", ENCOUNTERS)));

        assertThat(plan.toSubmit()).extracting(Job::name).containsExactly("streaming-visits");
        assertThat(plan.toCancel()).isEmpty();
    }

    @Test
    void replacesAJobWhoseQueryChanged() {
        Job patientsBefore = job("streaming-patients", "SELECT 0");
        ActiveJob outdated = running("1", patientsBefore);

        Reconciliation plan =
                FlinkRunner.reconcile(THREE, List.of(outdated, running("2", VISITS), running("3", ENCOUNTERS)));

        assertThat(plan.toCancel()).containsExactly(outdated);
        assertThat(plan.toSubmit()).containsExactly(PATIENTS);
    }

    @Test
    void replacesAJobSubmittedBeforeJobsCarriedAFingerprintOnce() {
        ActiveJob unfingerprinted = new ActiveJob("1", "streaming-patients");

        Reconciliation plan = FlinkRunner.reconcile(List.of(PATIENTS), List.of(unfingerprinted));

        assertThat(plan.toCancel()).containsExactly(unfingerprinted);
        assertThat(plan.toSubmit()).containsExactly(PATIENTS);

        // After the replacement the cluster reports the fingerprinted name, and nothing more happens.
        assertThat(FlinkRunner.reconcile(List.of(PATIENTS), List.of(running("2", PATIENTS)))
                        .toSubmit())
                .isEmpty();
    }

    @Test
    void neverTouchesJobsThatThisPipelineDoesNotOwn() {
        List<ActiveJob> others = List.of(
                new ActiveJob("9", "some-other-teams-job"),
                // A prefix of a table name is a different table, not an older version of it.
                new ActiveJob("8", "streaming-patients_archive@1234abcd"));

        Reconciliation plan = FlinkRunner.reconcile(THREE, others);

        assertThat(plan.toCancel()).isEmpty();
        assertThat(plan.toSubmit()).isEqualTo(THREE);
    }

    @Test
    void aRecoveringJobCountsAsRunningSoItIsNotSubmittedTwice() throws Exception {
        // A job restored from HA storage is CREATED, then INITIALIZING, then RUNNING. Treating any
        // of those as absent is what submitted a second copy of an already-recovering job.
        String json = """
                {"jobs":[
                  {"jid":"1","name":"streaming-patients@1a2b3c4d","state":"CREATED"},
                  {"jid":"2","name":"streaming-visits@1a2b3c4d","state":"INITIALIZING"},
                  {"jid":"3","name":"streaming-encounters@1a2b3c4d","state":"RESTARTING"}
                ]}""";

        assertThat(RestRunningJobs.activeJobs(json)).extracting(ActiveJob::id).containsExactlyInAnyOrder("1", "2", "3");
    }

    @Test
    void finishedAndFailedJobsAreNotRunningSoTheyCanBeSubmittedAgain() throws Exception {
        String json = """
                {"jobs":[
                  {"jid":"1","name":"streaming-patients","state":"FINISHED"},
                  {"jid":"2","name":"streaming-visits","state":"FAILED"},
                  {"jid":"3","name":"streaming-encounters","state":"CANCELED"},
                  {"jid":"4","name":"streaming-orders","state":"RUNNING"}
                ]}""";

        assertThat(RestRunningJobs.activeJobs(json)).containsExactly(new ActiveJob("4", "streaming-orders"));
    }

    @Test
    void anEmptyClusterReportsNothingRunning() throws Exception {
        assertThat(RestRunningJobs.activeJobs("{\"jobs\":[]}")).isEmpty();
    }
}
