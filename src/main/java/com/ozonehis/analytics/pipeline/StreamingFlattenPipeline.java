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
 * Consumes the Debezium change events Kafka Connect publishes and continuously flattens them into
 * the analytics database.
 */
public final class StreamingFlattenPipeline implements Pipeline {

    private final AnalyticsConfig config;

    public StreamingFlattenPipeline(AnalyticsConfig config) {
        this.config = Objects.requireNonNull(config, "config");
    }

    @Override
    public String name() {
        return "streaming-flatten";
    }

    @Override
    public ExecutionMode mode() {
        return ExecutionMode.STREAMING;
    }

    @Override
    public List<String> tableDefinitions() {
        List<String> definitions = new ArrayList<>();
        for (AnalyticsConfig.KafkaSource source : config.sources().kafka()) {
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

    private ConnectorOptions options(AnalyticsConfig.KafkaSource source, String table) {
        return ConnectorOptions.of("kafka")
                .set("topic", source.topicPrefix() + "." + table)
                .set("properties.bootstrap.servers", source.bootstrapServers())
                // A stable group id per table lets the job resume from its committed offsets
                // instead of replaying the topic after every restart.
                .set("properties.group.id", table + "-group-id")
                .set("scan.startup.mode", "group-offsets")
                // Applies only when the group has no committed offsets yet, i.e. first start.
                .set("properties.auto.offset.reset", "earliest")
                .set("value.format", "debezium-json")
                .set("value.debezium-json.ignore-parse-errors", "true");
    }
}
