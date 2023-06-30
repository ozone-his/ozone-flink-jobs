package net.mekomsolutions.data.pipelines.shared.dsl.sink.jdbc;

import net.mekomsolutions.data.pipelines.shared.dsl.TableSQLDSL;
import net.mekomsolutions.data.pipelines.utils.ConnectorUtils;

import java.util.Map;
import java.util.Objects;

/**
 * This creates a Flink <a href=
 * "https://ci.apache.org/projects/flink/flink-docs-master/docs/dev/table/sourcessinks/">Sink
 * table</a> For flattening OpenMRS Module Bahmni Appointments Appointments
 */
public class Appointments implements TableSQLDSL {
	
	private Map<String, String> connectorOptions;
	
	public Appointments(Map<String, String> connectorOptions) {
		if (!Objects.equals(connectorOptions.get("connector"), "filesystem")) {
			connectorOptions.put("table-name", "appointments");
		} else {
			connectorOptions.put("path", "/tmp/analytics/appointments");
		}
		this.connectorOptions = connectorOptions;
	}
	
	/**
	 * @return appointments table DSL
	 */
	@Override
	public String getDSL() {
		return "CREATE TABLE `appointments` (\n" + "  `patient_appointment_id` INT PRIMARY KEY,\n"
		        + "  `patient_id` INT,\n" + "  `appointment_number` VARCHAR,\n" + "  `start_date_time` TIMESTAMP,\n"
		        + "  `location_id` INT,\n" + "  `end_date_time` TIMESTAMP,\n" + "  `appointment_service_id` INT,\n"
		        + "  `appointment_service_type_id` INT,\n" + "  `status` VARCHAR,\n" + "  `appointment_kind` VARCHAR,\n"
		        + "  `comments` VARCHAR,\n" + "  `related_appointment_id` INT,\n" + "  `creator` INT,\n"
		        + "  `date_created` TIMESTAMP,\n" + "  `changed_by` INT,\n" + "  `date_changed` TIMESTAMP,\n"
		        + "  `voided` BOOLEAN,\n" + "  `voided_by` INT,\n" + "  `date_voided` TIMESTAMP,\n"
		        + "  `void_reason` VARCHAR,\n" + "  `uuid` VARCHAR,\n" + "  `appointment_service_name` VARCHAR,\n"
		        + "  `appointment_service_description` VARCHAR,\n" + "  `appointment_service_voided` BOOLEAN,\n"
		        + "  `appointment_service_uuid` VARCHAR,\n" + "  `appointment_service_color` VARCHAR,\n"
		        + "  `appointment_service_start_time` TIMESTAMP,\n" + "  `appointment_service_end_time` TIMESTAMP,\n"
		        + "  `appointment_service_speciality_id` INT,\n"
		        + "  `appointment_service_max_appointments_limit` INT,\n"
		        + "  `appointment_service_duration_mins` INT,\n"
		        + "  `appointment_service_initial_appointment_status` VARCHAR,\n"
		        + "  `appointment_service_type_name` VARCHAR,\n" + "  `appointment_service_type_duration_mins` INT,\n"
		        + "  `appointment_service_type_voided` BOOLEAN,\n" + "  `appointment_service_type_uuid` VARCHAR,\n"
		        + "  `patient_appointment_provider` INT,\n" + "  `patient_appointment_provider_response` VARCHAR\n"
		        + ")\n" + "WITH (\n" + ConnectorUtils.propertyJoiner(",", "=").apply(this.connectorOptions) + ")";
	}
	
}
