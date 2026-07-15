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
        return "batch-flatten";
    }

    @Override
    public ExecutionMode mode() {
        return ExecutionMode.BATCH;
    }

    @Override
    public List<String> tableDefinitions() {
        List<String> definitions = new ArrayList<>();
        for (AnalyticsConfig.JdbcSource source : config.sources().jdbc()) {
            for (SqlScript table : SqlScripts.loadAll(Path.of(source.tableDefinitions()))) {
                definitions.add(options(source, table.name()).appendTo(table.content()));
            }
        }
        return List.copyOf(definitions);
    }

    @Override
    public List<String> insertStatements() {
        return FlattenStatements.forSinks(config);
    }

    private ConnectorOptions options(AnalyticsConfig.JdbcSource source, String table) {
        return ConnectorOptions.of("jdbc")
                .set("url", source.url())
                .set("table-name", table)
                .set("username", source.username())
                .set("password", source.password());
    }
}
