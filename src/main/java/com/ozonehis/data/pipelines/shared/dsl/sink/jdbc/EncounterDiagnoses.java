package com.ozonehis.data.pipelines.shared.dsl.sink.jdbc;

import com.ozonehis.data.pipelines.shared.dsl.TableSQLDSL;
import com.ozonehis.data.pipelines.utils.ConnectorUtils;

import java.util.Map;
import java.util.Objects;

/**
 * This creates a Flink <a href=
 * "https://ci.apache.org/projects/flink/flink-docs-master/docs/dev/table/sourcessinks/">Sink
 * table</a> For flattening OpenMRS Encounter Diagnoses
 */
public class EncounterDiagnoses implements TableSQLDSL {
	
	private Map<String, String> connectorOptions;
	
	public EncounterDiagnoses(Map<String, String> connectorOptions) {
		if (!Objects.equals(connectorOptions.get("connector"), "filesystem")) {
			connectorOptions.put("table-name", "encounter_diagnoses");
		} else {
			connectorOptions.put("path", "/tmp/analytics/encounter_diagnoses");
		}
		this.connectorOptions = connectorOptions;
	}
	
	/**
	 * @return encounter_diagnoses table DSL
	 */
	@Override
	public String getDSL() {
		return "CREATE TABLE `encounter_diagnoses` (\n" + "  `diagnosis_id` BIGINT PRIMARY KEY,\n"
		        + "  `diagnosis_coded` BIGINT,\n" + "  `diagnosis_non_coded` VARCHAR,\n"
		        + "  `diagnosis_coded_name` BIGINT,\n" + "  `encounter_id` BIGINT,\n" + "  `patient_id` BIGINT,\n"
		        + "  `certainty` VARCHAR,\n" + "  `rank` BIGINT,\n" + "  `uuid` VARCHAR,\n" + "  `creator` BIGINT,\n"
		        + "  `date_created` TIMESTAMP,\n" + "  `voided` BOOLEAN,\n" + "  `voided_by` BIGINT,\n"
		        + "  `date_voided` TIMESTAMP,\n" + "  `void_reason` VARCHAR\n" + ")\n" + "WITH (\n"
		        + ConnectorUtils.propertyJoiner(",", "=").apply(this.connectorOptions) + ")";
	}
	
}
