package net.mekomsolutions.data.pipelines.shared.dsl.sink.jdbc;

import net.mekomsolutions.data.pipelines.shared.dsl.TableSQLDSL;
import net.mekomsolutions.data.pipelines.utils.ConnectorUtils;

import java.util.Map;
import java.util.Objects;

/**
 * This creates a Flink <a href=
 * "https://ci.apache.org/projects/flink/flink-docs-master/docs/dev/table/sourcessinks/">Sink
 * table</a> For flattening OpenMRS Encounters
 */
public class Encounters implements TableSQLDSL {
	
	private Map<String, String> connectorOptions;
	
	public Encounters(Map<String, String> connectorOptions) {
		if (!Objects.equals(connectorOptions.get("connector"), "filesystem")) {
			connectorOptions.put("table-name", "encounters");
		} else {
			connectorOptions.put("path", "/tmp/analytics/encounters");
		}
		this.connectorOptions = connectorOptions;
	}
	
	/**
	 * @return encounters table DSL
	 */
	@Override
	public String getDSL() {
		return "CREATE TABLE `encounters` (\n" + "  `encounter_id` INT PRIMARY KEY,\n"
		        + "  `encounter_patient_id` INT,\n" + "  `encounter_type_id` INT,\n"
		        + "  `encounter_type_name` VARCHAR,\n" + "  `encounter_type_uuid` VARCHAR,\n"
		        + "  `encounter_type_retired` BOOLEAN,\n" + "  `encounter_datetime` TIMESTAMP,\n"
		        + "  `encounter_voided` BOOLEAN,\n" + "  `encounter_visit_id` INT,\n" + "  `visit_type_name` VARCHAR,\n"
		        + "  `visit_type_uuid` VARCHAR,\n" + "  `visit_type_description` VARCHAR,\n"
		        + "  `visit_date_started` TIMESTAMP,\n" + "  `visit_date_stopped` TIMESTAMP,\n"
		        + "  `visit_voided` BOOLEAN,\n" + "  `visit_type_retired` BOOLEAN,\n" + "  `encounter_form_id` INT,\n"
		        + "  `form_name` VARCHAR,\n" + "  `form_uuid` VARCHAR,\n" + "  `form_version` VARCHAR,\n"
		        + "  `form_published` BOOLEAN,\n" + "  `form_encounter_type` INT,\n" + "  `form_retired` BOOLEAN,\n"
		        + "  `encounter_location_id` INT,\n" + "  `location_name` VARCHAR,\n" + "  `location_uuid` VARCHAR,\n"
		        + "  `location_address1` VARCHAR,\n" + "  `location_retired` BOOLEAN,\n" + "  `encounter_creator` INT,\n"
		        + "  `encounter_date_created` TIMESTAMP,\n" + "  `encounter_voided_by` INT,\n"
		        + "  `encounter_date_voided` TIMESTAMP,\n" + "  `encounter_void_reason` VARCHAR,\n"
		        + "  `encounter_changed_by` INT,\n" + "  `encounter_date_changed` TIMESTAMP,\n"
		        + "  `encounter_uuid` VARCHAR,\n" + "  `encounter_type_description` VARCHAR,\n"
		        + "  `form_description` VARCHAR,\n" + "  `form_template` VARCHAR,\n" + "  `form_build` INT,\n"
		        + "  `location_description` VARCHAR,\n" + "  `location_address2` VARCHAR,\n"
		        + "  `location_city_village` VARCHAR,\n" + "  `location_state_province` VARCHAR,\n"
		        + "  `location_postal_code` VARCHAR,\n" + "  `location_country` VARCHAR,\n"
		        + "  `location_parent_location` INT,\n" + "  `location_county_district` VARCHAR\n" + ")\n" + "WITH (\n"
		        + ConnectorUtils.propertyJoiner(",", "=").apply(this.connectorOptions) + ")";
	}
	
}
