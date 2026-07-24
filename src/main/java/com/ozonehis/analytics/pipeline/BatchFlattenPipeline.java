package com.ozonehis.analytics.pipeline;

import com.ozonehis.analytics.config.AnalyticsConfig;
import com.ozonehis.analytics.sql.ConnectorOptions;
import com.ozonehis.analytics.sql.SqlScript;
import com.ozonehis.analytics.sql.SqlScripts;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Flattens the source databases in one pass by reading them directly over JDBC, bypassing Kafka.
 *
 * <p>Used to backfill the analytics database, where replaying the entire change log through the
 * streaming pipeline would be needlessly slow.
 */
public final class BatchFlattenPipeline implements Pipeline {

    private final AnalyticsConfig config;

    public BatchFlattenPipeline(AnalyticsConfig config) {
        this.config = Objects.requireNonNull(config, "config");
    }

    @Override
    public String name() {
        return "batch";
    }

    @Override
    public ExecutionMode mode() {
        return ExecutionMode.BATCH;
    }

    @Override
    public List<Job> jobs() {
        // JDBC sources carry no consumer-group state, so every job registers the same definitions;
        // a job reads only the tables its query touches.
        List<String> sourceTables = sourceTables();
        List<Job> jobs = new ArrayList<>();
        for (FlattenStatements.Insert insert : FlattenStatements.forSinks(config)) {
            jobs.add(new Job(name() + "-" + insert.destination(), sourceTables, insert.sql()));
        }
        return List.copyOf(jobs);
    }

    private List<String> sourceTables() {
        List<String> definitions = new ArrayList<>();
        for (AnalyticsConfig.JdbcSource source : config.sources().jdbc()) {
            for (SqlScript table : SqlScripts.loadAll(Path.of(source.tableDefinitions()))) {
                definitions.add(options(source, table.name()).appendTo(table.content()));
            }
        }
        return List.copyOf(definitions);
    }

    private ConnectorOptions options(AnalyticsConfig.JdbcSource source, String table) {
        return ConnectorOptions.of("jdbc")
                .set("url", source.url())
                .set("table-name", table)
                .set("username", source.username())
                .set("password", source.password());
    }
}
