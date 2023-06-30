package net.mekomsolutions.data.pipelines.shared.dsl.sink.filesystem;

import net.mekomsolutions.data.pipelines.shared.dsl.TableSQLDSL;
import net.mekomsolutions.data.pipelines.utils.ConnectorUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

/**
 * This creates a Flink <a href=
 * "https://ci.apache.org/projects/flink/flink-docs-master/docs/dev/table/sourcessinks/">Sink
 * table</a> For flattening OpenMRS Orders
 */
public class FlattenedOrdersFs implements TableSQLDSL {
	
	private Map<String, String> connectorOptions;
	
	public FlattenedOrdersFs(Map<String, String> connectorOptions) {
		this.connectorOptions = connectorOptions;
	}
	
	/**
	 * @return _orders table DSL
	 */
	@Override
	public String getDSL() {
		return "CREATE TABLE `_orders_fs` (\n" + "    `order_id` INT PRIMARY KEY,\n" + "    `patient_id` INT,\n"
		        + "    `order_type_id` INT,\n" + "    `order_type_name` VARCHAR,\n" + "    `order_type_uuid` VARCHAR,\n"
		        + "    `order_type_java_class_name` VARCHAR,\n" + "    `concept_id` INT,\n" + "    `orderer` INT,\n"
		        + "    `encounter_id` INT,\n" + "    `encounter_datetime` TIMESTAMP,\n"
		        + "    `encounter_type_name` VARCHAR,\n" + "    `encounter_type_uuid` VARCHAR,\n"
		        + "    `care_setting` INT,\n" + "    `care_setting_name` VARCHAR,\n"
		        + "    `care_setting_type` VARCHAR,\n" + "    `care_setting_uuid` VARCHAR,\n"
		        + "    `instructions` VARCHAR,\n" + "    `date_activated` TIMESTAMP,\n"
		        + "    `auto_expire_date` TIMESTAMP,\n" + "    `date_stopped` TIMESTAMP,\n" + "    `order_reason` INT,\n"
		        + "    `order_reason_non_coded` VARCHAR,\n" + "    `date_created` TIMESTAMP,\n" + "    `creator` INT,\n"
		        + "    `voided_by` INT,\n" + "    `date_voided` TIMESTAMP,\n" + "    `void_reason` VARCHAR,\n"
		        + "    `accession_number` VARCHAR,\n" + "    `uuid` VARCHAR,\n" + "    `order_number` VARCHAR,\n"
		        + "    `previous_order_id` INT,\n" + "    `order_action` VARCHAR,\n"
		        + "    `comment_to_fulfiller` VARCHAR,\n" + "    `scheduled_date` TIMESTAMP,\n"
		        + "    `order_group_id` INT,\n" + "    `sort_weight` DOUBLE,\n" + "    `encounter_voided` BOOLEAN,\n"
		        + "    `voided` BOOLEAN,\n" + "    `order_type_retired` BOOLEAN,\n"
		        + "    `encounter_type_retired` BOOLEAN,\n" + "    `care_setting_retired` BOOLEAN\n" + ")\n" + "WITH (\n"
		        + ConnectorUtils.propertyJoiner(",", "=").apply(this.connectorOptions) + ")";
	}
	
}
