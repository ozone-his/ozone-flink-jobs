package com.ozonehis.data.pipelines.utils;

import com.ozonehis.data.pipelines.shared.dsl.TableDSLFactory;
import com.ozonehis.data.pipelines.streaming.StreamingETLJob;
import org.apache.flink.table.api.TableEnvironment;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.stream.Collectors;

public class CommonUtils {
	
	public static String[] SOURCE_TABLES = { "person", "person_name", "person_address", "patient", "patient_identifier",
	        "patient_identifier_type", "visit", "visit_type", "concept", "concept_name", "concept_reference_map",
	        "concept_reference_term", "concept_reference_source", "obs", "encounter", "encounter_type", "location",
	        "care_setting", "order_type", "orders", "appointment_service", "appointment_service_type", "form",
	        "patient_appointment", "patient_appointment_provider", "patient_program", "program", "conditions",
	        "encounter_diagnosis" };;
	
	public static String SINK_TABLES[] = { "visits", "patients", "concepts", "observations", "_orders", "encounters",
	        "patient_programs", "appointments", "_conditions", "encounter_diagnoses" };
	
	public static void setupTables(TableEnvironment tableEnv, String[] tables, Map<String, String> connectorOptions) {
		
		TableDSLFactory tableDSLFactory = new TableDSLFactory(connectorOptions);
		for (String tableName : tables) {
			tableEnv.executeSql(tableDSLFactory.getTable(tableName).getDSL());
		}
	}
	
	public static void setupSourceTables(TableEnvironment tableEnv, String[] tables, Map<String, String> connectorOptions) {
		TableDSLFactory tableDSLFactory = new TableDSLFactory(connectorOptions);
		for (String tableName : tables) {
			tableEnv.executeSql(tableDSLFactory.getTable(tableName).getDSL());
		}
	}
	
	public static void setupSinkTables(TableEnvironment tableEnv, String[] tables, Map<String, String> connectorOptions) {
		TableDSLFactory tableDSLFactory = new TableDSLFactory(connectorOptions);
		for (String tableName : tables) {
			tableEnv.executeSql(tableDSLFactory.getTable(tableName).getDSL());
		}
	}
	
	public static String getResourceFileAsString(String fileName) throws IOException {
		try (InputStream is = StreamingETLJob.class.getResourceAsStream("/" + fileName)) {
			if (is == null)
				return null;
			try (InputStreamReader isr = new InputStreamReader(is); BufferedReader reader = new BufferedReader(isr)) {
				return reader.lines().collect(Collectors.joining(System.lineSeparator()));
			}
		}
	}
}
