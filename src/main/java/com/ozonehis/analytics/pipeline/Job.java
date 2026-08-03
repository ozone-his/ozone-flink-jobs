package com.ozonehis.analytics.pipeline;

import java.util.List;
import java.util.Objects;

/**
 * One independently submittable unit of a pipeline: the tables to register and the single INSERT
 * that drives them.
 *
 * <p>Each job becomes its own Flink job, so a failure in one neither fails nor restarts the others.
 * That isolation is the whole point of modeling a pipeline as many jobs rather than one: the cost
 * is that jobs no longer share a source scan, and each holds its own slots for the life of the job.
 *
 * @param name a short, stable, unique name used for the Flink job name and for logging
 * @param tableDefinitions the {@code CREATE TABLE ... WITH (...)} statements to register first
 * @param insertStatement the single {@code INSERT INTO ... SELECT ...} that moves the data
 */
public record Job(String name, List<String> tableDefinitions, String insertStatement) {

    public Job {
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(insertStatement, "insertStatement");
        tableDefinitions = List.copyOf(tableDefinitions);
    }
}
