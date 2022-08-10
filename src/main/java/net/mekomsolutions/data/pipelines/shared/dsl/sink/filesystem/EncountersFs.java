package net.mekomsolutions.data.pipelines.shared.dsl.sink.filesystem;

import net.mekomsolutions.data.pipelines.shared.dsl.TableSQLDSL;
import net.mekomsolutions.data.pipelines.utils.ConnectorUtils;

import java.util.Map;
import java.util.Objects;

/**
 * This creates a Flink <a href=
 * "https://ci.apache.org/projects/flink/flink-docs-master/docs/dev/table/sourcessinks/">Sink
 * table</a> For flattening OpenMRS Encounters
 */
public class EncountersFs implements TableSQLDSL {
	
	private Map<String, String> connectorOptions;
	
	public EncountersFs(Map<String, String> connectorOptions) {
		this.connectorOptions = connectorOptions;
	}
	
	/**
	 * @return encounters table DSL
	 */
	@Override
	public String getDSL() {
		return "CREATE TABLE `encounters` (\n" + "  `order_id` int,\n" + "  `patient_id` int,\n" + "  `order_type_id` int,\n"
		        + "  `order_type_name` VARCHAR,\n" + "  `order_type_uuid` VARCHAR,\n"
		        + "  `order_type_java_class_name` VARCHAR,\n" + "  `concept_id` int,\n" + "  `orderer` int,\n"
		        + "  `encounter_id` int,\n" + "  `encounter_datetime` TIMESTAMP,\n" + "  `encounter_type_name` VARCHAR,\n"
		        + "  `encounter_type_uuid` VARCHAR,\n" + "  `care_setting` int,\n" + "  `care_setting_name` VARCHAR,\n"
		        + "  `care_setting_type` VARCHAR,\n" + "  `care_setting_uuid` VARCHAR,\n" + "  `instructions` VARCHAR,\n"
		        + "  `date_activated` TIMESTAMP,\n" + "  `auto_expire_date` TIMESTAMP,\n" + "  `date_stopped` TIMESTAMP,\n"
		        + "  `order_reason` int,\n" + "  `order_reason_non_coded` VARCHAR,\n" + "  `date_created` TIMESTAMP,\n"
		        + "  `creator` int,\n" + "  `voided_by` int,\n" + "  `date_voided` TIMESTAMP,\n"
		        + "  `void_reason` VARCHAR,\n" + "  `accession_number` VARCHAR,\n" + "  `uuid` VARCHAR,\n"
		        + "  `order_number` VARCHAR,\n" + "  `previous_order_id` int,\n" + "  `order_action` VARCHAR,\n"
		        + "  `comment_to_fulfiller` VARCHAR,\n" + "  `scheduled_date` TIMESTAMP,\n" + "  `order_group_id` int,\n"
		        + "  `sort_weight` DOUBLE,\n" + "  `encounter_voided` BOOLEAN,\n" + "  `voided` BOOLEAN,\n"
		        + "  `order_type_retired` BOOLEAN,\n" + "  `encounter_type_retired` BOOLEAN,\n"
		        + "  `care_setting_retired` BOOLEAN\n" + ")\n" + "WITH (\n"
		        + ConnectorUtils.propertyJoiner(",", "=").apply(this.connectorOptions) + ")";
	}
	
}
