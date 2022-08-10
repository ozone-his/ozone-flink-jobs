package net.mekomsolutions.data.pipelines.export;

import net.mekomsolutions.data.pipelines.shared.dsl.TableDSLFactory;
import net.mekomsolutions.data.pipelines.utils.CommonUtils;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.table.api.EnvironmentSettings;
import org.apache.flink.table.api.TableEnvironment;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class BatchParquetExport {
	
	public static void main(String[] args) throws Exception {
		EnvironmentSettings settings = EnvironmentSettings.newInstance().inBatchMode().build();
		TableEnvironment tEnv = TableEnvironment.create(settings);
		final ParameterTool parameterTool = ParameterTool.fromArgs(args);
		Map<String, String> postgresConnectorOptions = Stream
		        .of(new String[][] { { "connector", "jdbc" }, { "url", parameterTool.get("jdbc-url", "") },
		                { "username", parameterTool.get("jdbc-username", "") },
		                { "password", parameterTool.get("jdbc-password", "") }, { "sink.buffer-flush.max-rows", "1000" },
		                { "sink.buffer-flush.interval", "1s" } })
		        .collect(Collectors.toMap(data -> data[0], data -> data[1]));
		
		final Map<String, String> fileSystemConnectorOptions = Stream
		        .of(new String[][] { { "connector", "filesystem" }, { "format", "parquet" },
		                { "path", parameterTool.get("output-dir", "/tmp/") }, })
		        .collect(Collectors.toMap(data -> data[0], data -> data[1]));
		
		String[] sourceTables = { "visits", "patients", "concepts", "observations", "_orders", "encounters",
		        "patient_programs", "appointments", "_conditions", "encounter_diagnoses" };
		
		CommonUtils.setupTables(tEnv, sourceTables, postgresConnectorOptions);
		
		String outPutPath = parameterTool.get("output-dir", "/tmp/").replaceAll("([^/])$", "$1/");
		String locationTag = parameterTool.get("location-tag", "");
		String date = DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(LocalDateTime.now());
		
		TableDSLFactory tableDSLFactorySink = new TableDSLFactory(fileSystemConnectorOptions);
		Stream.of(sourceTables).forEach(table -> {
			String tableFs = table + "_fs";
			fileSystemConnectorOptions.put("path", outPutPath + "/" + table + "/" + locationTag + "/" + date);
			
			tEnv.executeSql(tableDSLFactorySink.getTable(tableFs).getDSL());
			tEnv.executeSql("INSERT into " + tableFs + " SELECT t.*  from " + table + " t");
		});
	}
}
