package net.mekomsolutions.data.pipelines.shared.dsl.source;

import net.mekomsolutions.data.pipelines.shared.dsl.TableSQLDSL;
import net.mekomsolutions.data.pipelines.utils.ConnectorUtils;

import java.util.Map;
import java.util.Objects;

/**
 * This class represents a Flink <a href=
 * "https://ci.apache.org/projects/flink/flink-docs-master/docs/dev/table/sourcessinks/">Source
 * table</a> Mapping to OpenMRS Module Bahmni Appointments AppointmentProvider class
 */
public class PatientAppointmentProvider implements TableSQLDSL {
	
	private Map<String, String> connectorOptions;
	
	public PatientAppointmentProvider(Map<String, String> connectorOptions) {
		
		if (Objects.equals(connectorOptions.get("connector"), "kafka")) {
			connectorOptions.put("topic", "openmrs.openmrs.patient_appointment_provider");
		} else {
			connectorOptions.put("table-name", "patient_appointment_provider");
		}
		this.connectorOptions = connectorOptions;
	}
	
	/**
	 * @return patient_appointment_provider source table DSL
	 */
	@Override
	public String getDSL() {
		return "CREATE TABLE `patient_appointment_provider` (\n" + "  `patient_appointment_provider_id` int primary key,\n"
		        + "  `patient_appointment_id` int,\n" + "  `provider_id` int,\n" + "  `response` VARCHAR,\n"
		        + "  `comments` VARCHAR,\n" + "  `date_created` TIMESTAMP,\n" + "  `creator` int,\n"
		        + "  `date_changed` TIMESTAMP,\n" + "  `changed_by` int,\n" + "  `voided` BOOLEAN,\n"
		        + "  `voided_by` int,\n" + "  `date_voided` TIMESTAMP,\n" + "  `void_reason` VARCHAR,\n"
		        + "  `uuid` VARCHAR\n" + ")\n" + " WITH (\n"
		        + ConnectorUtils.propertyJoiner(",", "=").apply(connectorOptions) + ")";
	}
}
