package com.ozonehis.analytics.runtime;

import static org.assertj.core.api.Assertions.assertThat;

import com.ozonehis.analytics.pipeline.Job;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

/**
 * Resubmitting a job the cluster is already running duplicates it, so the runner submits only what
 * is missing. Both halves of that decision are pure and asserted on here with no cluster.
 */
class ReconciliationTest {

    private static Job job(String name) {
        return new Job(name, List.of("CREATE TABLE t (id INT)"), "INSERT INTO t SELECT 1");
    }

    private static final List<Job> THREE =
            List.of(job("streaming-patients"), job("streaming-visits"), job("streaming-encounters"));

    @Test
    void submitsEverythingWhenTheClusterIsEmpty() {
        assertThat(FlinkRunner.notAlreadyRunning(THREE, Set.of())).hasSize(3).isEqualTo(THREE);
    }

    @Test
    void submitsNothingWhenEveryJobIsAlreadyRunning() {
        Set<String> active = Set.of("streaming-patients", "streaming-visits", "streaming-encounters");

        assertThat(FlinkRunner.notAlreadyRunning(THREE, active)).isEmpty();
    }

    @Test
    void submitsOnlyTheMissingJobsAfterAPartialRecovery() {
        Set<String> active = Set.of("streaming-patients", "streaming-encounters");

        assertThat(FlinkRunner.notAlreadyRunning(THREE, active))
                .extracting(Job::name)
                .containsExactly("streaming-visits");
    }

    @Test
    void ignoresJobsOnTheClusterThatThisPipelineDoesNotOwn() {
        Set<String> active = Set.of("some-other-teams-job");

        assertThat(FlinkRunner.notAlreadyRunning(THREE, active)).isEqualTo(THREE);
    }

    @Test
    void aRecoveringJobCountsAsRunningSoItIsNotSubmittedTwice() throws Exception {
        // A job restored from HA storage is CREATED, then INITIALIZING, then RUNNING. Treating any
        // of those as absent is what submitted a second copy of an already-recovering job.
        String json = """
                {"jobs":[
                  {"jid":"1","name":"streaming-patients","state":"CREATED"},
                  {"jid":"2","name":"streaming-visits","state":"INITIALIZING"},
                  {"jid":"3","name":"streaming-encounters","state":"RESTARTING"}
                ]}""";

        assertThat(RestRunningJobs.activeNames(json))
                .containsExactlyInAnyOrder("streaming-patients", "streaming-visits", "streaming-encounters");
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

        assertThat(RestRunningJobs.activeNames(json)).containsExactly("streaming-orders");
    }

    @Test
    void anEmptyClusterReportsNothingRunning() throws Exception {
        assertThat(RestRunningJobs.activeNames("{\"jobs\":[]}")).isEmpty();
    }
}
