package com.ozonehis.analytics.pipeline;

import com.ozonehis.analytics.config.AnalyticsConfig;
import com.ozonehis.analytics.config.ConfigException;
import com.ozonehis.analytics.sql.ConnectorOptions;
import com.ozonehis.analytics.sql.SqlScript;
import com.ozonehis.analytics.sql.SqlScripts;
import java.nio.file.Path;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Exports the flattened analytics tables to Parquet or CSV so they can be shipped to a central
 * warehouse.
 *
 * <p>Unlike the flattening pipelines, the export queries are already complete INSERT statements;
 * they only need the analytics catalog name substituted in.
 */
public final class FileExportPipeline implements Pipeline {

    /**
     * Placeholder the export queries use in place of a hard-coded catalog name, so the same query
     * works against any deployment's catalog.
     */
    public static final String CATALOG_PLACEHOLDER = "{ANALYTICS_CATALOG}";

    /**
     * Compact, sortable and safe to embed in a path. ISO-8601 is not: it separates time components
     * with colons, which are illegal in Windows paths and awkward to handle in S3 tooling.
     */
    private static final DateTimeFormatter RUN_TIMESTAMP = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss");

    private final AnalyticsConfig config;

    private final Clock clock;

    public FileExportPipeline(AnalyticsConfig config) {
        this(config, Clock.systemDefaultZone());
    }

    /**
     * @param clock supplies the timestamp that partitions each export run. Injected so the
     *     generated SQL is deterministic under test.
     */
    public FileExportPipeline(AnalyticsConfig config, Clock clock) {
        this.config = Objects.requireNonNull(config, "config");
        this.clock = Objects.requireNonNull(clock, "clock");
    }

    @Override
    public String name() {
        return "export";
    }

    @Override
    public ExecutionMode mode() {
        return ExecutionMode.BATCH;
    }

    @Override
    public List<Job> jobs() {
        String catalog = config.singleCatalog()
                .orElseThrow(() -> new ConfigException("The export pipeline resolves " + CATALOG_PLACEHOLDER
                        + " against a single catalog, but " + config.catalogs().size()
                        + " are configured. Configure exactly one."))
                .name();
        // One timestamp for the whole run, so every table this run exports lands under the same
        // directory rather than scattering across the seconds each job happened to start in.
        String timestamp = RUN_TIMESTAMP.format(LocalDateTime.now(clock));

        List<Job> jobs = new ArrayList<>();
        for (AnalyticsConfig.FileSink sink : config.sinks().files()) {
            List<String> definitions = new ArrayList<>();
            for (SqlScript table : SqlScripts.loadAll(Path.of(sink.tableDefinitions()))) {
                definitions.add(options(sink, table.name(), timestamp).appendTo(table.content()));
            }
            for (SqlScript query : SqlScripts.loadAll(Path.of(sink.queries()))) {
                jobs.add(new Job(
                        name() + "-" + query.name(),
                        definitions,
                        query.content().replace(CATALOG_PLACEHOLDER, catalog)));
            }
        }
        return List.copyOf(jobs);
    }

    private ConnectorOptions options(AnalyticsConfig.FileSink sink, String table, String timestamp) {
        // Each run writes to its own timestamped directory so exports never overwrite each other.
        String path = String.join("/", sink.outputPath(), table, sink.tag(), timestamp);
        return ConnectorOptions.of("filesystem")
                .set("path", path)
                .set("format", sink.format().connectorValue())
                .set("sink.rolling-policy.file-size", "10MB");
    }
}
