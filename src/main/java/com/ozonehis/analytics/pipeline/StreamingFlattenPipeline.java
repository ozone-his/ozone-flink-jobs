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
        return "streaming";
    }

    @Override
    public ExecutionMode mode() {
        return ExecutionMode.STREAMING;
    }

    @Override
    public List<Job> jobs() {
        List<Job> jobs = new ArrayList<>();
        for (FlattenStatements.Insert insert : FlattenStatements.forSinks(config)) {
            jobs.add(new Job(name() + "-" + insert.destination(), sourceTables(insert.destination()), insert.sql()));
        }
        return List.copyOf(jobs);
    }

    /**
     * Every Kafka source table, defined afresh for one job. A job registers all of them but only
     * the tables its query reads become running Kafka sources, so the extra definitions cost
     * nothing. The consumer group is namespaced by the destination so that two jobs reading the
     * same source topic keep independent offsets instead of clobbering one shared group.
     */
    private List<String> sourceTables(String destination) {
        List<String> definitions = new ArrayList<>();
        for (AnalyticsConfig.KafkaSource source : config.sources().kafka()) {
            for (SqlScript table : SqlScripts.loadAll(Path.of(source.tableDefinitions()))) {
                definitions.add(options(source, table.name(), destination).appendTo(table.content()));
            }
        }
        return List.copyOf(definitions);
    }

    private ConnectorOptions options(AnalyticsConfig.KafkaSource source, String table, String destination) {
        return ConnectorOptions.of("kafka")
                .set("topic", source.topicPrefix() + "." + table)
                .set("properties.bootstrap.servers", source.bootstrapServers())
                // A stable group id per (table, destination) lets each job resume from its own
                // committed offsets instead of replaying the topic after a restart, and keeps two
                // jobs reading the same topic from committing over each other.
                .set("properties.group.id", table + "-" + destination + "-group-id")
                .set("scan.startup.mode", "group-offsets")
                // Applies only when the group has no committed offsets yet, i.e. first start.
                .set("properties.auto.offset.reset", "earliest")
                .set("value.format", "debezium-json")
                .set("value.debezium-json.ignore-parse-errors", "true");
    }
}
