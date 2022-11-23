package com.ozonehis.data.pipelines.export;

import com.ozonehis.data.pipelines.shared.dsl.TableDSLFactory;
import com.ozonehis.data.pipelines.utils.CommonUtils;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.configuration.ConfigConstants;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.configuration.MemorySize;
import org.apache.flink.configuration.TaskManagerOptions;
import org.apache.flink.table.api.EnvironmentSettings;
import org.apache.flink.table.api.StatementSet;
import org.apache.flink.table.api.TableEnvironment;
import org.apache.flink.table.api.config.TableConfigOptions;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class BatchParquetExport {
	
	public static void main(String[] args) throws Exception {
		EnvironmentSettings settings = EnvironmentSettings.newInstance().inBatchMode().build();
		TableEnvironment tEnv = TableEnvironment.create(settings);
		Configuration synchronousConfig = new Configuration();
		synchronousConfig.setBoolean(TableConfigOptions.TABLE_DML_SYNC, true);
		
		tEnv.getConfig().addConfiguration(synchronousConfig);
		final ParameterTool parameterTool = ParameterTool.fromArgs(args);
		Map<String, String> postgresConnectorOptions = Stream
		        .of(new String[][] { { "connector", "jdbc" }, { "url", parameterTool.get("jdbc-url", "") },
		                { "username", parameterTool.get("jdbc-username", "") },
		                { "password", parameterTool.get("jdbc-password", "") }, { "sink.buffer-flush.max-rows", "1000" },
		                { "sink.buffer-flush.interval", "1s" }, { "scan.fetch-size", "2000" } })
		        .collect(Collectors.toMap(data -> data[0], data -> data[1]));
		
		final Map<String, String> fileSystemConnectorOptions = Stream
		        .of(new String[][] { { "connector", "filesystem" }, { "format", "parquet" },
		                { "sink.rolling-policy.file-size", "10MB" }, { "path", parameterTool.get("output-dir", "/tmp/") }, })
		        .collect(Collectors.toMap(data -> data[0], data -> data[1]));
		
		String[] sourceTables = { "patients", "visits", "concepts", "observations", "_orders", "encounters",
		        "patient_programs", "appointments", "_conditions", "encounter_diagnoses" };
		
		CommonUtils.setupTables(tEnv, sourceTables, postgresConnectorOptions);
		
		String outPutPath = parameterTool.get("output-dir", "/tmp/").replaceAll("([^/])$", "$1/");
		String locationTag = parameterTool.get("location-tag", "");
		String date = DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(LocalDateTime.now());
		
		TableDSLFactory tableDSLFactorySink = new TableDSLFactory(fileSystemConnectorOptions);
		
		// create tables on the file system
		Stream.of(sourceTables).forEach(table -> {
			String tableFs = table + "_fs";
			fileSystemConnectorOptions.put("path", outPutPath + "/" + table + "/" + locationTag + "/" + date);
			tEnv.executeSql(tableDSLFactorySink.getTable(tableFs).getDSL());
		});
		// fill tables with data from query related dsl queries
		Stream.of(sourceTables).forEach(table -> {
			String tableFs = table + "_fs";
			tEnv.executeSql("INSERT into " + tableFs + " SELECT t.*  from " + table + " t");
		});
	}
}
