package com.ozonehis.data.pipelines.shared.dsl.source;

import com.ozonehis.data.pipelines.shared.dsl.TableSQLDSL;
import com.ozonehis.data.pipelines.utils.ConnectorUtils;

import java.util.Map;
import java.util.Objects;

/**
 * This class represents a Flink <a href=
 * "https://ci.apache.org/projects/flink/flink-docs-master/docs/dev/table/sourcessinks/">Source
 * table</a> Mapping to OpenMRS Diagnosis class
 */
public class EncounterDiagnosis implements TableSQLDSL {
	
	private Map<String, String> connectorOptions;
	
	public EncounterDiagnosis(Map<String, String> connectorOptions) {
		
		if (Objects.equals(connectorOptions.get("connector"), "kafka")) {
			connectorOptions.put("topic", "openmrs.openmrs.encounter_diagnosis");
		} else {
			connectorOptions.put("table-name", "encounter_diagnosis");
		}
		this.connectorOptions = connectorOptions;
	}
	
	/**
	 * @return encounter_diagnosis source table DSL
	 */
	@Override
	public String getDSL() {
		return "CREATE TABLE `encounter_diagnosis` (\n" + "  `diagnosis_id` int primary key,\n"
		        + "  `diagnosis_coded` int,\n" + "  `diagnosis_non_coded` VARCHAR,\n" + "  `diagnosis_coded_name` int,\n"
		        + "  `encounter_id` int,\n" + "  `patient_id` int,\n" + "  `condition_id` int,\n"
		        + "  `certainty` VARCHAR,\n" + "  `rank` int,\n" + "  `uuid` VARCHAR,\n" + "  `creator` int,\n"
		        + "  `date_created` TIMESTAMP,\n" + "  `changed_by` int,\n" + "  `date_changed` TIMESTAMP,\n"
		        + "  `voided` BOOLEAN,\n" + "  `voided_by` int,\n" + "  `date_voided` TIMESTAMP,\n"
		        + "  `void_reason` VARCHAR\n" + ")\n" + " WITH (\n"
		        + ConnectorUtils.propertyJoiner(",", "=").apply(connectorOptions) + ")";
	}
}
