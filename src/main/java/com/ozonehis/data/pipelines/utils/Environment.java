package com.ozonehis.data.pipelines.utils;

import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

public class Environment {
	
	public static String getEnv(String key, String defaultValue) {
		String value = System.getenv(key);
		if (value == null) {
			return defaultValue;
		}
		return value;
	}
	
	public static StreamExecutionEnvironment getExecutionEnvironment() {
		StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
		if (System.console() != null) {
			Configuration config = new Configuration();
			config.setString("restart-strategy.type", "exponential-delay");
			// set the checkpoint mode to EXACTLY_ONCE
			config.setString("execution.checkpointing.mode", "EXACTLY_ONCE");
			config.setString("execution.checkpointing.interval", "10min");
			config.setString("execution.checkpointing.timeout", "10min");
			config.setString("execution.checkpointing.unaligned.enabled", "true");
			config.setString("execution.checkpointing.tolerable-failed-checkpoints", "400");
			config.setString("table.dynamic-table-options.enabled", "true");
			config.setString("state.backend.type", "hashmap");
			config.setString("state.backend.incremental", "true");
			config.setString("state.checkpoints.dir", "file:///tmp/checkpoints/");
			config.setString("state.savepoints.dir", "file:///tmp/savepoints/");
			config.setString("taskmanager.network.numberOfBuffers", "20");
			config.setString("table.exec.resource.default-parallelism", "3");
			//config.setBoolean(ConfigConstants.LOCAL_START_WEBSERVER, true);
			env = StreamExecutionEnvironment.createLocalEnvironmentWithWebUI(config);
			env.setParallelism(3);
		}
		return env;
	}
}
