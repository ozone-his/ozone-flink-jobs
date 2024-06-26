package com.ozonehis.data.pipelines.utils;

import com.ozonehis.data.pipelines.Constants;
import java.util.Collection;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;
import org.apache.commons.lang3.StringUtils;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.configuration.JobManagerOptions;
import org.apache.flink.configuration.MemorySize;
import org.apache.flink.configuration.MetricOptions;
import org.apache.flink.configuration.RestOptions;
import org.apache.flink.configuration.TaskManagerOptions;
import org.apache.flink.runtime.client.JobStatusMessage;
import org.apache.flink.runtime.minicluster.MiniCluster;
import org.apache.flink.runtime.minicluster.MiniClusterConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Environment {

    private static Logger logger = LoggerFactory.getLogger(Environment.class);

    public static String getEnv(String key, String defaultValue) {
        String value = System.getenv(key);
        if (value == null) {
            return defaultValue;
        }
        return value;
    }

    public static MiniCluster initMiniClusterWithEnv(Boolean isStreaming) throws Exception {
        Configuration flinkConfig = new Configuration();
        String port = System.getProperty(Constants.PROP_FLINK_REST_PORT);
        if (StringUtils.isBlank(port)) {
            port = Environment.getEnv("FLINK_REST_PORT", "8081");
        }

        if (StringUtils.isNotBlank(port)) {
            flinkConfig.setInteger(RestOptions.PORT, Integer.valueOf(port));
        }
        flinkConfig.set(
                TaskManagerOptions.NETWORK_MEMORY_MIN,
                MemorySize.parse(System.getenv().getOrDefault("NETWORK_MEMORY_MIN", "500m")));
        flinkConfig.set(
                TaskManagerOptions.NETWORK_MEMORY_MAX,
                MemorySize.parse(System.getenv().getOrDefault("NETWORK_MEMORY_MAX", "500m")));
        flinkConfig.set(
                TaskManagerOptions.MANAGED_MEMORY_FRACTION,
                Float.parseFloat(System.getenv().getOrDefault("MANAGED_MEMORY_FRACTION", "0.7")));
        flinkConfig.set(
                TaskManagerOptions.MANAGED_MEMORY_SIZE,
                MemorySize.parse(System.getenv().getOrDefault("MANAGED_MEMORY_SIZE", "1g")));
        flinkConfig.set(
                TaskManagerOptions.TOTAL_PROCESS_MEMORY,
                MemorySize.parse(System.getenv().getOrDefault("TOTAL_PROCESS_MEMORY", "1g")));
        flinkConfig.set(
                JobManagerOptions.TOTAL_PROCESS_MEMORY,
                MemorySize.parse(System.getenv().getOrDefault("TOTAL_PROCESS_MEMORY", "1g")));
        flinkConfig.set(
                TaskManagerOptions.TASK_HEAP_MEMORY,
                MemorySize.parse(System.getenv().getOrDefault("TASK_HEAP_MEMORY", "1g")));
        flinkConfig.set(
                TaskManagerOptions.TASK_OFF_HEAP_MEMORY,
                MemorySize.parse(System.getenv().getOrDefault("TASK_OFF_HEAP_MEMORY", "1g")));
        flinkConfig.set(
                TaskManagerOptions.FRAMEWORK_HEAP_MEMORY,
                MemorySize.parse(System.getenv().getOrDefault("FRAMEWORK_HEAP_MEMORY", "1g")));
        flinkConfig.set(
                TaskManagerOptions.FRAMEWORK_OFF_HEAP_MEMORY,
                MemorySize.parse(System.getenv().getOrDefault("FRAMEWORK_OFF_HEAP_MEMORY", "1g")));

        flinkConfig.setString("restart-strategy.type", "exponential-delay");
        flinkConfig.setString("execution.checkpointing.mode", "EXACTLY_ONCE");
        flinkConfig.setString("execution.checkpointing.interval", "10min");
        flinkConfig.setString("execution.checkpointing.timeout", "10min");
        flinkConfig.setString("execution.checkpointing.unaligned.enabled", "true");
        flinkConfig.setString("execution.checkpointing.tolerable-failed-checkpoints", "50");
        flinkConfig.setString("table.dynamic-table-options.enabled", "true");
        flinkConfig.setString(
                "table.exec.resource.default-parallelism", System.getenv().getOrDefault("TASK_PARALLELISM", "1"));
        flinkConfig.setString("state.backend.type", "rocksdb");
        flinkConfig.setString("state.backend.incremental", "true");
        flinkConfig.setString("state.checkpoints.dir", "file:///tmp/flink/checkpoints/");
        flinkConfig.setString("state.savepoints.dir", "file:///tmp/flink/savepoints/");
        flinkConfig.setInteger("state.checkpoints.num-retained", 4);
        flinkConfig.setString("taskmanager.network.numberOfBuffers", "20");
        flinkConfig.setString("io.tmp.dirs", "/tmp/temp");
        if (isStreaming) {
            flinkConfig.setString("high-availability.type", "ZOOKEEPER");
            flinkConfig.setString("high-availability.storageDir", "/tmp/flink/ha");
            flinkConfig.setString("high-availability.zookeeper.quorum", getEnv("ZOOKEEPER_URL", "zookeeper:2181"));
        }
        flinkConfig.set(MetricOptions.SCOPE_NAMING_JM, "jobmanager");
        flinkConfig.set(MetricOptions.SCOPE_NAMING_TM, "taskmanager");
        MetricOptions.forReporter(flinkConfig, "prom")
                .set(
                        MetricOptions.REPORTER_FACTORY_CLASS,
                        "org.apache.flink.metrics.prometheus.PrometheusReporterFactory")
                .setString("port", "9250");
        MiniClusterConfiguration clusterConfig = new MiniClusterConfiguration.Builder()
                .setNumTaskManagers(1)
                .setNumSlotsPerTaskManager(Integer.parseInt(System.getenv().getOrDefault("TASK_MANAGER_SLOTS", "20")))
                .setConfiguration(flinkConfig)
                .build();
        MiniCluster cluster = new MiniCluster(clusterConfig);
        return cluster;
    }

    public static void exitOnCompletion(MiniCluster cluster) {
        Runnable exitOnCompleteRunnable = () -> {
            try {
                Collection<JobStatusMessage> jobs = cluster.listJobs().get();
                if (jobs.size() == 0) {
                    System.exit(0);
                }
                Boolean[] jobStatuses = jobs.stream()
                        .map(job -> job.getJobState().isGloballyTerminalState())
                        .toArray(Boolean[]::new);
                if (Stream.of(jobStatuses).allMatch(Boolean::valueOf)) {
                    logger.info("All jobs are completed. Exiting...");
                    System.exit(0);
                }
            } catch (Exception e) {
                logger.error("All jobs are completed. Exiting...", e);
            }
        };

        ScheduledExecutorService exec = Executors.newScheduledThreadPool(1);
        exec.scheduleAtFixedRate(exitOnCompleteRunnable, 0, 1, TimeUnit.MINUTES);
    }
}
