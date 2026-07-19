package com.ozonehis.analytics.pipeline;

import java.util.List;

/**
 * A description of an ETL pipeline as SQL.
 *
 * <p>Implementations are pure: they read configuration and SQL files and return statements. They
 * never touch Flink and never execute anything, which means the SQL a pipeline generates can be
 * asserted on directly in a unit test, with no cluster and no containers.
 *
 * <p>Executing a pipeline is {@link com.ozonehis.analytics.runtime.FlinkRunner}'s job.
 */
public sealed interface Pipeline permits StreamingFlattenPipeline, BatchFlattenPipeline, FileExportPipeline {

    /** @return a short, stable name used as a prefix for each job's name and for logging */
    String name();

    /** @return whether this pipeline runs continuously or terminates */
    ExecutionMode mode();

    /**
     * The pipeline broken into independently submittable {@link Job}s — one per INSERT — so that
     * each runs as its own Flink job and a failure in one does not disturb the rest.
     *
     * @return one job per destination, each carrying the tables it needs and its single INSERT
     */
    List<Job> jobs();
}
