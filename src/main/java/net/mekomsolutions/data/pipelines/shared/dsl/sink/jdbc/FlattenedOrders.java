package net.mekomsolutions.data.pipelines.shared.dsl.sink.jdbc;

import net.mekomsolutions.data.pipelines.shared.dsl.TableSQLDSL;
import net.mekomsolutions.data.pipelines.utils.ConnectorUtils;

import java.util.Map;
import java.util.Objects;

/**
 * This creates a Flink <a href=
 * "https://ci.apache.org/projects/flink/flink-docs-master/docs/dev/table/sourcessinks/">Sink
 * table</a> For flattening OpenMRS Orders
 */
public class FlattenedOrders implements TableSQLDSL {
	
	private Map<String, String> connectorOptions;
	
	public FlattenedOrders(Map<String, String> connectorOptions) {
		if (!Objects.equals(connectorOptions.get("connector"), "filesystem")) {
			connectorOptions.put("table-name", "_orders");
		} else {
			connectorOptions.put("path", "/tmp/analytics/_orders");
		}
		this.connectorOptions = connectorOptions;
	}
	
	/**
	 * @return _orders table DSL
	 */
	@Override
	public String getDSL() {
		return "CREATE TABLE `_orders` (\n" + "    `order_id` BIGINT PRIMARY KEY,\n" + "    `patient_id` BIGINT,\n"
		        + "    `order_type_id` BIGINT,\n" + "    `order_type_name` VARCHAR,\n" + "    `order_type_uuid` VARCHAR,\n"
		        + "    `order_type_java_class_name` VARCHAR,\n" + "    `concept_id` BIGINT,\n" + "    `orderer` BIGINT,\n"
		        + "    `encounter_id` BIGINT,\n" + "    `encounter_datetime` TIMESTAMP,\n"
		        + "    `encounter_type_name` VARCHAR,\n" + "    `encounter_type_uuid` VARCHAR,\n"
		        + "    `care_setting` BIGINT,\n" + "    `care_setting_name` VARCHAR,\n" + "    `care_setting_type` VARCHAR,\n"
		        + "    `care_setting_uuid` VARCHAR,\n" + "    `instructions` VARCHAR,\n"
		        + "    `date_activated` TIMESTAMP,\n" + "    `auto_expire_date` TIMESTAMP,\n"
		        + "    `date_stopped` TIMESTAMP,\n" + "    `order_reason` BIGINT,\n" + "    `order_reason_non_coded` VARCHAR,\n"
		        + "    `date_created` TIMESTAMP,\n" + "    `creator` BIGINT,\n" + "    `voided_by` BIGINT,\n"
		        + "    `date_voided` TIMESTAMP,\n" + "    `void_reason` VARCHAR,\n" + "    `accession_number` VARCHAR,\n"
		        + "    `uuid` VARCHAR,\n" + "    `order_number` VARCHAR,\n" + "    `previous_order_id` BIGINT,\n"
		        + "    `order_action` VARCHAR,\n" + "    `comment_to_fulfiller` VARCHAR,\n"
		        + "    `scheduled_date` TIMESTAMP,\n" + "    `order_group_id` BIGINT,\n" + "    `sort_weight` DOUBLE,\n"
		        + "    `encounter_voided` BOOLEAN,\n" + "    `voided` BOOLEAN,\n" + "    `order_type_retired` BOOLEAN,\n"
		        + "    `encounter_type_retired` BOOLEAN,\n" + "    `care_setting_retired` BOOLEAN\n" + ")\n" + "WITH (\n"
		        + ConnectorUtils.propertyJoiner(",", "=").apply(this.connectorOptions) + ")";
	}
	
}
