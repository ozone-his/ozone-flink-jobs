package net.mekomsolutions.data.pipelines.shared.dsl.source;

import net.mekomsolutions.data.pipelines.shared.dsl.TableSQLDSL;
import net.mekomsolutions.data.pipelines.utils.ConnectorUtils;

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
		return "CREATE TABLE `conditions` (\n" + "  `condition_id` int primary key,\n"
		        + "  `previous_condition_id` BIGINT,\n" + "  `patient_id` BIGINT,\n" + "  `status` VARCHAR,\n"
		        + "  `concept_id` BIGINT,\n" + "  `condition_non_coded` VARCHAR,\n" + "  `onset_date` TIMESTAMP,\n"
		        + "  `additional_detail` VARCHAR,\n" + "  `end_date` TIMESTAMP,\n" + "  `end_reason` BIGINT,\n"
		        + "  `creator` BIGINT,\n" + "  `date_created` TIMESTAMP,\n" + "  `voided` BOOLEAN,\n"
		        + "  `voided_by` BIGINT,\n" + "  `date_voided` TIMESTAMP,\n" + "  `void_reason` VARCHAR,\n"
		        + "  `uuid` VARCHAR\n" + ")\n" + " WITH (\n"
		        + ConnectorUtils.propertyJoiner(",", "=").apply(connectorOptions) + ")";
	}
}
