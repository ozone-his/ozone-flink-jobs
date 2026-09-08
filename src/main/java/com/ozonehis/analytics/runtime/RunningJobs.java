package com.ozonehis.analytics.runtime;

import java.util.Set;

/**
 * The names of the jobs a cluster is already running.
 *
 * <p>Exists so that {@link FlinkRunner} can submit only the jobs that are missing rather than the
 * whole pipeline, and so that decision can be tested without a cluster.
 */
@FunctionalInterface
public interface RunningJobs {

    /** A source that reports nothing running, so every job is submitted. */
    RunningJobs NONE = Set::of;

    /**
     * @return the names of every job on the cluster that has not reached a terminal state
     * @throws Exception if the cluster cannot be reached; callers must not treat an unreachable
     *     cluster as "nothing is running", which would submit a duplicate of everything
     */
    Set<String> names() throws Exception;
}
