package com.ozonehis.data.pipelines.shared.dsl.source;

import com.ozonehis.data.pipelines.shared.dsl.TableSQLDSL;
import com.ozonehis.data.pipelines.utils.ConnectorUtils;

import java.util.Map;
import java.util.Objects;

/**
 * This class represents a Flink <a href=
 * "https://ci.apache.org/projects/flink/flink-docs-master/docs/dev/table/sourcessinks/">Source
 * table</a> Mapping to OpenMRS Conditions class
 */
public class Conditions implements TableSQLDSL {
	
	private Map<String, String> connectorOptions;
	
	public Conditions(Map<String, String> connectorOptions) {
		
		if (Objects.equals(connectorOptions.get("connector"), "kafka")) {
			connectorOptions.put("topic", "openmrs.openmrs.conditions");
		} else {
			connectorOptions.put("table-name", "conditions");
		}
		this.connectorOptions = connectorOptions;
	}
	
	/**
	 * @return conditions source table DSL
	 */
	@Override
	public String getDSL() {
		return "CREATE TABLE `conditions` (\n" + "  `condition_id` int primary key,\n" + "  `additional_detail` VARCHAR,\n"
		        + "  `previous_version` int,\n" + "  `condition_coded` int,\n" + "  `condition_non_coded` VARCHAR,\n"
		        + "  `condition_coded_name` int,\n" + "  `clinical_status` VARCHAR,\n" + "  `verification_status` VARCHAR,\n"
		        + "  `onset_date` TIMESTAMP,\n" + "  `date_created` TIMESTAMP,\n" + "  `voided` BOOLEAN,\n"
		        + "  `date_voided` TIMESTAMP,\n" + "  `void_reason` VARCHAR,\n" + "  `uuid` VARCHAR,\n"
		        + "  `creator` int,\n" + "  `voided_by` int,\n" + "  `changed_by` int,\n" + "  `patient_id` int,\n"
		        + "  `end_date` TIMESTAMP,\n" + "  `date_changed` TIMESTAMP,\n" + "  `encounter_id` int\n" + ")\n"
		        + " WITH (\n" + ConnectorUtils.propertyJoiner(",", "=").apply(connectorOptions) + ")";
	}
}
