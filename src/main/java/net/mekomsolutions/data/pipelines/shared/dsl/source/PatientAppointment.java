package net.mekomsolutions.data.pipelines.shared.dsl.source;

import net.mekomsolutions.data.pipelines.shared.dsl.TableSQLDSL;
import net.mekomsolutions.data.pipelines.utils.ConnectorUtils;

import java.util.Map;
import java.util.Objects;

/**
 * This class represents a Flink <a href=
 * "https://ci.apache.org/projects/flink/flink-docs-master/docs/dev/table/sourcessinks/">Source
 * table</a> Mapping to OpenMRS Module Bahmni Appointments Appointment class
 */
public class PatientAppointment implements TableSQLDSL {
	
	private Map<String, String> connectorOptions;
	
	public PatientAppointment(Map<String, String> connectorOptions) {
		
		if (Objects.equals(connectorOptions.get("connector"), "kafka")) {
			connectorOptions.put("topic", "openmrs.openmrs.patient_appointment");
		} else {
			connectorOptions.put("table-name", "patient_appointment");
		}
		this.connectorOptions = connectorOptions;
	}
	
	/**
	 * @return patient_appointment source table DSL
	 */
	@Override
	public String getDSL() {
		return "CREATE TABLE `patient_appointment` (\n" + "  `patient_appointment_id` int primary key,\n"
		        + "  `provider_id` int,\n" + "  `appointment_number` VARCHAR,\n" + "  `patient_id` int,\n"
		        + "  `start_date_time` TIMESTAMP,\n" + "  `end_date_time` TIMESTAMP,\n" + "  `appointment_service_id` int,\n"
		        + "  `appointment_service_type_id` int,\n" + "  `status` VARCHAR,\n" + "  `location_id` int,\n"
		        + "  `appointment_kind` VARCHAR,\n" + "  `comments` VARCHAR,\n" + "  `uuid` VARCHAR,\n"
		        + "  `date_created` TIMESTAMP,\n" + "  `creator` int,\n" + "  `date_changed` TIMESTAMP,\n"
		        + "  `changed_by` int,\n" + "  `voided` BOOLEAN,\n" + "  `voided_by` int,\n" + "  `date_voided` TIMESTAMP,\n"
		        + "  `void_reason` VARCHAR,\n" + "  `related_appointment_id` int,\n" + "  `tele_health_video_link` VARCHAR\n"
		        + ")\n" + " WITH (\n" + ConnectorUtils.propertyJoiner(",", "=").apply(connectorOptions) + ")";
	}
}
