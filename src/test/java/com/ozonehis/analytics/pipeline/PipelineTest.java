package com.ozonehis.analytics.pipeline;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.InstanceOfAssertFactories.STRING;

import com.ozonehis.analytics.config.AnalyticsConfig;
import com.ozonehis.analytics.config.ConfigException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Pipelines are pure SQL builders, so the SQL they generate is asserted on directly with no Flink
 * cluster and no containers.
 */
class PipelineTest {

    @TempDir
    Path dir;

    private Path tables;

    private Path queries;

    @BeforeEach
    void setUp() throws IOException {
        tables = Files.createDirectories(dir.resolve("tables"));
        queries = Files.createDirectories(dir.resolve("queries"));
        Files.writeString(tables.resolve("patient.sql"), "CREATE TABLE patient (patient_id INT)");
        Files.writeString(queries.resolve("patients.sql"), "SELECT patient_id FROM patient");
    }

    private AnalyticsConfig.Catalog catalog() {
        return new AnalyticsConfig.Catalog(
                "ozone",
                AnalyticsConfig.Dialect.POSTGRESQL,
                "analytics",
                "jdbc:postgresql://localhost:5432",
                "user",
                "pw");
    }

    private AnalyticsConfig config(AnalyticsConfig.Sources sources, AnalyticsConfig.Sinks sinks) {
        return new AnalyticsConfig(List.of(catalog()), sources, sinks);
    }

    private AnalyticsConfig.Sinks tableSink() {
        return new AnalyticsConfig.Sinks(
                List.of(new AnalyticsConfig.TableSink("ozone", "analytics", queries.toString())), List.of());
    }

    @Test
    void streamingBindsEachTableToItsDebeziumTopic() {
        AnalyticsConfig config = config(
                new AnalyticsConfig.Sources(
                        List.of(new AnalyticsConfig.KafkaSource("emr.openmrs", "kafka:9092", tables.toString())),
                        List.of()),
                tableSink());

        List<Job> jobs = new StreamingFlattenPipeline(config).jobs();

        assertThat(jobs).singleElement().extracting(Job::name).isEqualTo("streaming-flatten-patients");
        assertThat(jobs.get(0).tableDefinitions())
                .singleElement(STRING)
                .contains("'connector' = 'kafka'")
                // The topic is the prefix plus the table definition's file name.
                .contains("'topic' = 'emr.openmrs.patient'")
                // The group id is namespaced by the destination, so jobs sharing a topic keep
                // independent offsets.
                .contains("'properties.group.id' = 'patient-patients-group-id'")
                .contains("'value.format' = 'debezium-json'");
    }

    @Test
    void flatteningQueryBecomesOneJobInsertingIntoTheTableNamedAfterTheFile() {
        AnalyticsConfig config = config(new AnalyticsConfig.Sources(List.of(), List.of()), tableSink());

        assertThat(new StreamingFlattenPipeline(config).jobs())
                .singleElement()
                .extracting(Job::insertStatement)
                .isEqualTo("INSERT INTO `ozone`.`analytics`.`patients`\nSELECT patient_id FROM patient");
    }

    @Test
    void streamingAndBatchProduceIdenticalInsertsAndDifferOnlyInSource() {
        AnalyticsConfig streamingConfig = config(
                new AnalyticsConfig.Sources(
                        List.of(new AnalyticsConfig.KafkaSource("emr.openmrs", "kafka:9092", tables.toString())),
                        List.of()),
                tableSink());
        AnalyticsConfig batchConfig = config(
                new AnalyticsConfig.Sources(
                        List.of(),
                        List.of(new AnalyticsConfig.JdbcSource(
                                "jdbc:mysql://localhost:3306/openmrs", "root", "pw", tables.toString()))),
                tableSink());

        assertThat(inserts(new BatchFlattenPipeline(batchConfig)))
                .isEqualTo(inserts(new StreamingFlattenPipeline(streamingConfig)));
        assertThat(new BatchFlattenPipeline(batchConfig).jobs())
                .singleElement()
                .satisfies(job -> assertThat(job.tableDefinitions())
                        .singleElement(STRING)
                        .contains("'connector' = 'jdbc'")
                        .contains("'table-name' = 'patient'"));
    }

    private static List<String> inserts(Pipeline pipeline) {
        return pipeline.jobs().stream().map(Job::insertStatement).toList();
    }

    @Test
    void batchTerminatesWhileStreamingRunsContinuously() {
        AnalyticsConfig cfg = config(new AnalyticsConfig.Sources(List.of(), List.of()), tableSink());

        assertThat(new StreamingFlattenPipeline(cfg).mode()).isEqualTo(ExecutionMode.STREAMING);
        assertThat(new BatchFlattenPipeline(cfg).mode()).isEqualTo(ExecutionMode.BATCH);
        assertThat(new FileExportPipeline(cfg).mode()).isEqualTo(ExecutionMode.BATCH);
    }

    @Test
    void sinkNamingAnUnconfiguredCatalogFailsFast() {
        AnalyticsConfig cfg = new AnalyticsConfig(
                List.of(catalog()),
                new AnalyticsConfig.Sources(List.of(), List.of()),
                new AnalyticsConfig.Sinks(
                        List.of(new AnalyticsConfig.TableSink("typo", "analytics", queries.toString())), List.of()));

        assertThatThrownBy(() -> new StreamingFlattenPipeline(cfg).jobs())
                .isInstanceOf(ConfigException.class)
                .hasMessageContaining("No catalog named 'typo'");
    }

    @Test
    void exportWritesEachRunToItsOwnTimestampedPath() throws IOException {
        Files.writeString(
                queries.resolve("patients.sql"),
                "INSERT INTO patients SELECT * FROM {ANALYTICS_CATALOG}.analytics.patients");
        AnalyticsConfig cfg = new AnalyticsConfig(
                List.of(catalog()),
                new AnalyticsConfig.Sources(List.of(), List.of()),
                new AnalyticsConfig.Sinks(
                        List.of(),
                        List.of(new AnalyticsConfig.FileSink(
                                tables.toString(),
                                queries.toString(),
                                "/parquet",
                                "h1",
                                AnalyticsConfig.FileFormat.PARQUET))));
        Clock fixed = Clock.fixed(Instant.parse("2026-07-15T08:30:00Z"), ZoneOffset.UTC);

        List<Job> jobs = new FileExportPipeline(cfg, fixed).jobs();

        assertThat(jobs).singleElement().extracting(Job::name).isEqualTo("file-export-patients");
        assertThat(jobs.get(0).tableDefinitions())
                .singleElement(STRING)
                // Timestamp is colon-free so the path is valid on any filesystem.
                .contains("'path' = '/parquet/patient/h1/20260715T083000'")
                .contains("'format' = 'parquet'");
        // The placeholder is resolved to the configured catalog.
        assertThat(jobs.get(0).insertStatement())
                .isEqualTo("INSERT INTO patients SELECT * FROM ozone.analytics.patients");
    }

    @Test
    void exportRefusesToGuessBetweenMultipleCatalogs() {
        AnalyticsConfig cfg = new AnalyticsConfig(
                List.of(
                        catalog(),
                        new AnalyticsConfig.Catalog(
                                "other", AnalyticsConfig.Dialect.MYSQL, "db", "jdbc:mysql://h:3306", "u", "p")),
                new AnalyticsConfig.Sources(List.of(), List.of()),
                new AnalyticsConfig.Sinks(
                        List.of(),
                        List.of(new AnalyticsConfig.FileSink(
                                tables.toString(), queries.toString(), "/parquet", "h1", null))));

        assertThatThrownBy(() -> new FileExportPipeline(cfg).jobs())
                .isInstanceOf(ConfigException.class)
                .hasMessageContaining("exactly one");
    }
}
