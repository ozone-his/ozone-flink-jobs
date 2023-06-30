package net.mekomsolutions.data.pipelines.shared.dsl.sink.filesystem;

import net.mekomsolutions.data.pipelines.shared.dsl.TableSQLDSL;
import net.mekomsolutions.data.pipelines.utils.ConnectorUtils;

import java.util.Map;
import java.util.Objects;

/**
 * This creates a Flink <a href=
 * "https://ci.apache.org/projects/flink/flink-docs-master/docs/dev/table/sourcessinks/">Sink
 * table</a> For flattening OpenMRS Patient Programs
 */
public class FlattenedConditionsFs implements TableSQLDSL {
	
	private Map<String, String> connectorOptions;
	
	public FlattenedConditionsFs(Map<String, String> connectorOptions) {
		this.connectorOptions = connectorOptions;
	}
	
	/**
	 * @return _conditions table DSL
	 */
	@Override
	public String getDSL() {
		return "CREATE TABLE `_conditions_fs` (\n" + "  `condition_id` int primary key,\n"
		        + "  `previous_condition_id` INT,\n" + "  `patient_id` INT,\n" + "  `status` VARCHAR,\n"
		        + "  `concept_id` INT,\n" + "  `condition_non_coded` VARCHAR,\n" + "  `onset_date` TIMESTAMP,\n"
		        + "  `additional_detail` VARCHAR,\n" + "  `end_date` TIMESTAMP,\n" + "  `end_reason` INT,\n"
		        + "  `creator` INT,\n" + "  `date_created` TIMESTAMP,\n" + "  `voided` BOOLEAN,\n"
		        + "  `voided_by` INT,\n" + "  `date_voided` TIMESTAMP,\n" + "  `void_reason` VARCHAR,\n"
		        + "  `uuid` VARCHAR\n" + ")\n" + "WITH (\n"
		        + ConnectorUtils.propertyJoiner(",", "=").apply(this.connectorOptions) + ")";
	}
	
}
