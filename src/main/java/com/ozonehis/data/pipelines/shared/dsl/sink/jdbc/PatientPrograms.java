package com.ozonehis.data.pipelines.shared.dsl.sink.jdbc;

import com.ozonehis.data.pipelines.shared.dsl.TableSQLDSL;
import com.ozonehis.data.pipelines.utils.ConnectorUtils;

import java.util.Map;
import java.util.Objects;

/**
 * This creates a Flink <a href=
 * "https://ci.apache.org/projects/flink/flink-docs-master/docs/dev/table/sourcessinks/">Sink
 * table</a> For flattening OpenMRS Patient Programs
 */
public class PatientPrograms implements TableSQLDSL {
	
	private Map<String, String> connectorOptions;
	
	public PatientPrograms(Map<String, String> connectorOptions) {
		if (!Objects.equals(connectorOptions.get("connector"), "filesystem")) {
			connectorOptions.put("table-name", "patient_programs");
		} else {
			connectorOptions.put("path", "/tmp/analytics/patient_programs");
		}
		this.connectorOptions = connectorOptions;
	}
	
	/**
	 * @return patient_programs table DSL
	 */
	@Override
	public String getDSL() {
		return "CREATE TABLE `patient_programs` (\n" + "  `patient_program_id` BIGINT PRIMARY KEY,\n"
		        + "  `patient_id` BIGINT,\n" + "  `program_id` BIGINT,\n" + "  `date_enrolled` TIMESTAMP,\n"
		        + "  `date_completed` TIMESTAMP,\n" + "  `location_id` BIGINT,\n" + "  `outcome_concept_id` BIGINT,\n"
		        + "  `creator` BIGINT,\n" + "  `date_created` TIMESTAMP,\n" + "  `changed_by` BIGINT,\n"
		        + "  `date_changed` TIMESTAMP,\n" + "  `voided` BOOLEAN,\n" + "  `voided_by` BIGINT,\n"
		        + "  `date_voided` TIMESTAMP,\n" + "  `void_reason` VARCHAR,\n" + "  `uuid` VARCHAR,\n"
		        + "  `program_retired` BOOLEAN,\n" + "  `program_name` VARCHAR,\n" + "  `program_description` VARCHAR,\n"
		        + "  `program_uuid` VARCHAR,\n" + "  `program_concept_id` BIGINT,\n" + "  `concept_name` VARCHAR,\n"
		        + "  `concept_uuid` VARCHAR,\n" + "  `program_outcomes_concept_id` BIGINT,\n"
		        + "  `outcomes_concept_name` VARCHAR,\n" + "  `outcomes_concept_uuid` VARCHAR\n" + ")\n" + "WITH (\n"
		        + ConnectorUtils.propertyJoiner(",", "=").apply(this.connectorOptions) + ")";
	}
	
}
