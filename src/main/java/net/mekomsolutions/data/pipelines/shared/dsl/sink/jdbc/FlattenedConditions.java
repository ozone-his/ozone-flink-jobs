package net.mekomsolutions.data.pipelines.shared.dsl.sink.jdbc;

import net.mekomsolutions.data.pipelines.shared.dsl.TableSQLDSL;
import net.mekomsolutions.data.pipelines.utils.ConnectorUtils;

import java.util.Map;
import java.util.Objects;

/**
 * This creates a Flink <a href=
 * "https://ci.apache.org/projects/flink/flink-docs-master/docs/dev/table/sourcessinks/">Sink
 * table</a> For flattening OpenMRS Patient Programs
 */
public class FlattenedConditions implements TableSQLDSL {
	
	private Map<String, String> connectorOptions;
	
	public FlattenedConditions(Map<String, String> connectorOptions) {
		if (!Objects.equals(connectorOptions.get("connector"), "filesystem")) {
			connectorOptions.put("table-name", "_conditions");
		} else {
			connectorOptions.put("path", "/tmp/analytics/_conditions");
		}
		this.connectorOptions = connectorOptions;
	}
	
	/**
	 * @return _conditions table DSL
	 */
	@Override
	public String getDSL() {
		return "CREATE TABLE `_conditions` (\n" + 
				"  `condition_id` int primary key,\n" + 
				"  `previous_condition_id` BIGINT,\n" + 
				"  `patient_id` BIGINT,\n" + 
				"  `status` VARCHAR,\n" + 
				"  `concept_id` BIGINT,\n" + 
				"  `condition_non_coded` VARCHAR,\n" + 
				"  `onset_date` TIMESTAMP,\n" + 
				"  `additional_detail` VARCHAR,\n" + 
				"  `end_date` TIMESTAMP,\n" + 
				"  `end_reason` BIGINT,\n" + 
				"  `creator` BIGINT,\n" + 
				"  `date_created` TIMESTAMP,\n" + 
				"  `voided` BOOLEAN,\n" + 
				"  `voided_by` BIGINT,\n" + 
				"  `date_voided` TIMESTAMP,\n" + 
				"  `void_reason` VARCHAR,\n" + 
				"  `uuid` VARCHAR\n" + 
				")\n" + "WITH (\n"
		        + ConnectorUtils.propertyJoiner(",", "=").apply(this.connectorOptions) + ")";
	}
	
}
