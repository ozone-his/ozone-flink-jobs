package com.ozonehis.analytics.runtime;

import java.util.List;
import java.util.Objects;

/**
 * The jobs a cluster is already running, and a way to stop one.
 *
 * <p>Exists so that {@link FlinkRunner} can submit only the jobs that are missing, and replace the
 * ones running an outdated query, rather than submitting the whole pipeline again; and so that
 * decision can be tested without a cluster.
 */
public interface RunningJobs {

    /** A job on the cluster that has not reached a terminal state. */
    record ActiveJob(String id, String name) {

        public ActiveJob {
            Objects.requireNonNull(id, "id");
            Objects.requireNonNull(name, "name");
        }
    }

    /** A source that reports nothing running, so every job is submitted. */
    RunningJobs NONE = new RunningJobs() {
        @Override
        public List<ActiveJob> active() {
            return List.of();
        }

        @Override
        public void cancel(String id) {
            throw new IllegalStateException("No cluster to cancel job " + id + " on");
        }
    };

    /**
     * @return every job on the cluster that has not reached a terminal state
     * @throws Exception if the cluster cannot be reached; callers must not treat an unreachable
     *     cluster as "nothing is running", which would submit a duplicate of everything
     */
    List<ActiveJob> active() throws Exception;

    /**
     * Cancels a job and returns once the cluster no longer reports it as active, so its slots are
     * free for the job that replaces it.
     *
     * @throws Exception if the cluster refuses, cannot be reached, or the job is still active when
     *     the wait runs out
     */
    void cancel(String id) throws Exception;
}
