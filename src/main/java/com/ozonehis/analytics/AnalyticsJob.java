package com.ozonehis.analytics;

import com.ozonehis.analytics.config.AnalyticsConfig;
import com.ozonehis.analytics.config.ConfigLoader;
import com.ozonehis.analytics.pipeline.BatchFlattenPipeline;
import com.ozonehis.analytics.pipeline.FileExportPipeline;
import com.ozonehis.analytics.pipeline.Pipeline;
import com.ozonehis.analytics.pipeline.StreamingFlattenPipeline;
import com.ozonehis.analytics.runtime.FlinkRunner;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Locale;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Entry point for the Ozone analytics ETL pipelines.
 *
 * <p>Usage: {@code AnalyticsJob <pipeline>} where {@code pipeline} is one of {@code
 * streaming-flatten}, {@code batch-flatten} or {@code file-export}. Each published image passes its
 * own pipeline, so which one an image runs is fixed at build time.
 *
 * <p>Failures are deliberately allowed to propagate out of {@code main}: the JVM then exits
 * non-zero and Flink reports the job as failed. Catching and logging here would let a failed ETL
 * exit successfully, which is indistinguishable from a clean run to anything watching it.
 */
public final class AnalyticsJob {

    private static final Logger LOG = LoggerFactory.getLogger(AnalyticsJob.class);

    /** Environment variable naming the pipeline configuration file. */
    private static final String CONFIG_PATH_ENV = "ANALYTICS_CONFIG_FILE";

    private static final String DEFAULT_CONFIG_PATH = "/etc/analytics/config.yaml";

    private AnalyticsJob() {}

    public static void main(String[] args) throws Exception {
        if (args.length != 1) {
            throw new IllegalArgumentException("Expected exactly one argument naming the pipeline to run, one of "
                    + names() + ", but got " + Arrays.toString(args));
        }

        Path configPath = Path.of(resolve(CONFIG_PATH_ENV, DEFAULT_CONFIG_PATH));
        LOG.info("Loading analytics configuration from {}", configPath);
        AnalyticsConfig config = new ConfigLoader().load(configPath);

        new FlinkRunner(config).run(pipeline(args[0], config));
    }

    /** Resolves a pipeline by name, preferring a system property over the environment. */
    private static Pipeline pipeline(String name, AnalyticsConfig config) {
        return switch (name.toLowerCase(Locale.ROOT)) {
            case "streaming-flatten" -> new StreamingFlattenPipeline(config);
            case "batch-flatten" -> new BatchFlattenPipeline(config);
            case "file-export" -> new FileExportPipeline(config);
            default -> throw new IllegalArgumentException(
                    "Unknown pipeline '" + name + "'. Expected one of " + names());
        };
    }

    private static String names() {
        return java.util.List.of("streaming-flatten", "batch-flatten", "file-export").stream()
                .collect(Collectors.joining(", "));
    }

    /** A system property wins over the environment, which makes local overrides easy. */
    private static String resolve(String key, String defaultValue) {
        String fromProperty = System.getProperty(key);
        if (fromProperty != null && !fromProperty.isBlank()) {
            return fromProperty;
        }
        String fromEnv = System.getenv(key);
        return fromEnv == null || fromEnv.isBlank() ? defaultValue : fromEnv;
    }
}
