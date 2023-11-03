package com.ozonehis.data.pipelines.utils;

import java.util.Collection;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import org.apache.flink.configuration.Configuration;
import org.apache.flink.configuration.MemorySize;
import org.apache.flink.configuration.TaskManagerOptions;
import org.apache.flink.runtime.client.JobStatusMessage;
import org.apache.flink.runtime.minicluster.MiniCluster;
import org.apache.flink.runtime.minicluster.MiniClusterConfiguration;
import org.apache.flink.streaming.api.environment.RemoteStreamEnvironment;

public class Environment {
	
	public static String getEnv(String key, String defaultValue) {
		String value = System.getenv(key);
		if (value == null) {
			return defaultValue;
		}
		return value;
	}
	
	public static MiniCluster initMiniClusterWithEnv() throws Exception {
		return initMiniClusterWithEnv(true);
	}
	
	public static MiniCluster initMiniClusterWithEnv(Boolean isStreaming) throws Exception {
		Configuration flinkConfig = new Configuration();
		flinkConfig.set(TaskManagerOptions.NETWORK_MEMORY_MIN, MemorySize.parse("64m"));
		flinkConfig.set(TaskManagerOptions.NETWORK_MEMORY_MAX, MemorySize.parse("64m"));
		
		flinkConfig.setString("restart-strategy.type", "exponential-delay");
		flinkConfig.setString("execution.checkpointing.mode", "EXACTLY_ONCE");
		flinkConfig.setString("execution.checkpointing.interval", "10min");
		flinkConfig.setString("execution.checkpointing.timeout", "10min");
		flinkConfig.setString("execution.checkpointing.unaligned.enabled", "true");
		flinkConfig.setString("execution.checkpointing.tolerable-failed-checkpoints", "50");
		flinkConfig.setString("table.dynamic-table-options.enabled", "true");
		flinkConfig.setString("table.exec.resource.default-parallelism", "1");
		flinkConfig.setString("state.backend.type", "rocksdb");
		flinkConfig.setString("state.backend.incremental", "true");
		flinkConfig.setString("state.checkpoints.dir", "file:///tmp/flink/checkpoints/");
		flinkConfig.setString("state.savepoints.dir", "file:///tmp/flink/savepoints/");
		flinkConfig.setInteger("state.checkpoints.num-retained", 4);
		flinkConfig.setString("taskmanager.network.numberOfBuffers", "20");
		flinkConfig.setString("io.tmp.dirs", "/tmp/temp");
		if (isStreaming) {
			flinkConfig.setString("high-availability", "ZOOKEEPER");
			flinkConfig.setString("high-availability.storageDir", "/tmp/flink/ha");
			flinkConfig.setString("high-availability.zookeeper.quorum", getEnv("ZOOKEEPER_URL", "zookeeper:2181"));
		}
		MiniClusterConfiguration clusterConfig = new MiniClusterConfiguration.Builder().setNumTaskManagers(1)
		        .setNumSlotsPerTaskManager(20).setConfiguration(flinkConfig).build();
		MiniCluster cluster = new MiniCluster(clusterConfig);
		return cluster;
	}
	
	public static void exitOnComplete(MiniCluster cluster) {
		Runnable exitOnCompleteRunnable = new Runnable() {
			
			public void run() {
				try {
					Collection<JobStatusMessage> jobs = cluster.listJobs().get();
					if (jobs.size() == 0) {
						System.exit(0);
					}
					Boolean[] jobStatuses = jobs.stream().map(job -> job.getJobState().isGloballyTerminalState())
					        .toArray(Boolean[]::new);
					if (Stream.of(jobStatuses).allMatch(Boolean::valueOf)) {
						System.out.println("All jobs are completed. Exiting...");
						System.exit(0);
					}
				}
				catch (Exception e) {
					e.printStackTrace();
				}
			}
		};
		ScheduledExecutorService exec = Executors.newScheduledThreadPool(1);
		exec.scheduleAtFixedRate(exitOnCompleteRunnable, 0, 1, TimeUnit.MINUTES);
	}
}
