package com.ozonehis.analytics.pipeline;

/**
 * How a pipeline processes its input.
 *
 * <p>Deliberately independent of Flink's own {@code RuntimeExecutionMode}: pipelines are plain
 * descriptions, and the runtime translates this when it builds the table environment.
 */
public enum ExecutionMode {
    /** Runs continuously until cancelled, consuming change events as they arrive. */
    STREAMING,
    /** Reads a bounded input and terminates. */
    BATCH
}
