package com.ozonehis.analytics.config;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;
import java.util.Optional;

/**
 * The ETL pipeline definition: where data is read from, how it is flattened, and where it is
 * written.
 *
 * <p>This describes <em>what</em> the ETL computes. It is deliberately separate from the Flink
 * cluster's own {@code config.yaml}, which owns <em>how</em> the pipeline runs (parallelism,
 * checkpointing, state backend, S3).
 *
 * <p>Every record validates itself on construction, so a malformed file fails at startup with a
 * precise message rather than part-way through a running job.
 */
@JsonIgnoreProperties(ignoreUnknown = false)
public record AnalyticsConfig(List<Catalog> catalogs, Sources sources, Sinks sinks) {

    public AnalyticsConfig {
        catalogs = Validation.requireNonEmpty(catalogs, "catalogs");
        sources = sources == null ? Sources.empty() : sources;
        sinks = sinks == null ? Sinks.empty() : sinks;
        Validation.requireUniqueBy(catalogs, Catalog::name, "catalogs[].name");
    }

    /** @return the catalog registered under {@code name} */
    public Catalog catalog(String name) {
        return catalogs.stream()
                .filter(c -> c.name().equals(name))
                .findFirst()
                .orElseThrow(() -> new ConfigException("No catalog named '" + name + "'. Configured catalogs: "
                        + catalogs.stream().map(Catalog::name).toList()));
    }

    /**
     * A database registered with Flink as a catalog, letting queries address its tables directly
     * instead of redeclaring them.
     */
    @JsonIgnoreProperties(ignoreUnknown = false)
    public record Catalog(
            String name, Dialect dialect, String defaultDatabase, String baseUrl, String username, String password) {

        public Catalog {
            Validation.requireText(name, "catalogs[].name");
            Validation.requireNonNull(dialect, "catalogs[].dialect");
            Validation.requireText(defaultDatabase, "catalogs[].defaultDatabase");
            Validation.requireText(baseUrl, "catalogs[].baseUrl");
            Validation.requireText(username, "catalogs[].username");
            // A blank password is unusual but legitimate (e.g. trust auth), so it is not rejected.
            password = password == null ? "" : password;
        }
    }

    /** The systems change data is extracted from. */
    @JsonIgnoreProperties(ignoreUnknown = false)
    public record Sources(List<KafkaSource> kafka, List<JdbcSource> jdbc) {

        public Sources {
            kafka = Validation.nullToEmpty(kafka);
            jdbc = Validation.nullToEmpty(jdbc);
        }

        static Sources empty() {
            return new Sources(List.of(), List.of());
        }
    }

    /**
     * Debezium change events on Kafka. One entry per source system; each {@code .sql} file under
     * {@code tableDefinitions} maps to the topic {@code <topicPrefix>.<fileName>}.
     */
    @JsonIgnoreProperties(ignoreUnknown = false)
    public record KafkaSource(String topicPrefix, String bootstrapServers, String tableDefinitions) {

        public KafkaSource {
            Validation.requireText(topicPrefix, "sources.kafka[].topicPrefix");
            Validation.requireText(bootstrapServers, "sources.kafka[].bootstrapServers");
            Validation.requireText(tableDefinitions, "sources.kafka[].tableDefinitions");
        }
    }

    /** A source database read directly over JDBC, used by the batch pipeline. */
    @JsonIgnoreProperties(ignoreUnknown = false)
    public record JdbcSource(String url, String username, String password, String tableDefinitions) {

        public JdbcSource {
            Validation.requireText(url, "sources.jdbc[].url");
            Validation.requireText(username, "sources.jdbc[].username");
            Validation.requireText(tableDefinitions, "sources.jdbc[].tableDefinitions");
            password = password == null ? "" : password;
        }
    }

    /** Where flattened data is written. */
    @JsonIgnoreProperties(ignoreUnknown = false)
    public record Sinks(List<TableSink> tables, List<FileSink> files) {

        public Sinks {
            tables = Validation.nullToEmpty(tables);
            files = Validation.nullToEmpty(files);
        }

        static Sinks empty() {
            return new Sinks(List.of(), List.of());
        }
    }

    /**
     * Flattened tables written into a catalog database. Each {@code .sql} file under {@code
     * queries} is a SELECT whose file name is the destination table.
     */
    @JsonIgnoreProperties(ignoreUnknown = false)
    public record TableSink(String catalog, String database, String queries) {

        public TableSink {
            Validation.requireText(catalog, "sinks.tables[].catalog");
            Validation.requireText(database, "sinks.tables[].database");
            Validation.requireText(queries, "sinks.tables[].queries");
        }
    }

    /** Flattened data exported to files for shipping to a central warehouse. */
    @JsonIgnoreProperties(ignoreUnknown = false)
    public record FileSink(String tableDefinitions, String queries, String outputPath, String tag, FileFormat format) {

        public FileSink {
            Validation.requireText(tableDefinitions, "sinks.files[].tableDefinitions");
            Validation.requireText(queries, "sinks.files[].queries");
            Validation.requireText(outputPath, "sinks.files[].outputPath");
            Validation.requireText(tag, "sinks.files[].tag");
            format = format == null ? FileFormat.PARQUET : format;
        }
    }

    /** JDBC dialects with a Flink catalog implementation. */
    public enum Dialect {
        POSTGRESQL,
        MYSQL
    }

    /** Export file formats supported by the Flink filesystem connector. */
    public enum FileFormat {
        PARQUET,
        CSV;

        /** @return the connector's name for this format, which is lower case */
        public String connectorValue() {
            return name().toLowerCase(java.util.Locale.ROOT);
        }
    }

    /** @return the single configured catalog, when a pipeline requires exactly one */
    public Optional<Catalog> singleCatalog() {
        return catalogs.size() == 1 ? Optional.of(catalogs.get(0)) : Optional.empty();
    }
}
