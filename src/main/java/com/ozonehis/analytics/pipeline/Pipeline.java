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

    /** @return a short, stable name used for the Flink job name and for logging */
    String name();

    /** @return whether this pipeline runs continuously or terminates */
    ExecutionMode mode();

    /** @return the {@code CREATE TABLE ... WITH (...)} statements to register before inserting */
    List<String> tableDefinitions();

    /** @return the {@code INSERT INTO ... SELECT ...} statements that move the data */
    List<String> insertStatements();
}
