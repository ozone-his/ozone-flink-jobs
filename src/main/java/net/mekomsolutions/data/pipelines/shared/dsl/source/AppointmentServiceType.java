package net.mekomsolutions.data.pipelines.shared.dsl.source;

import net.mekomsolutions.data.pipelines.shared.dsl.TableSQLDSL;
import net.mekomsolutions.data.pipelines.utils.ConnectorUtils;

import java.util.Map;
import java.util.Objects;

/**
 * This class represents a Flink <a href=
 * "https://ci.apache.org/projects/flink/flink-docs-master/docs/dev/table/sourcessinks/">Source
 * table</a> Mapping to OpenMRS Module Bahmni Appointments AppointmentServiceType class
 */
public class AppointmentServiceType implements TableSQLDSL {
	
	private Map<String, String> connectorOptions;
	
	public AppointmentServiceType(Map<String, String> connectorOptions) {
		
		if (Objects.equals(connectorOptions.get("connector"), "kafka")) {
			connectorOptions.put("topic", "openmrs.openmrs.appointment_service_type");
		} else {
			connectorOptions.put("table-name", "appointment_service_type");
		}
		this.connectorOptions = connectorOptions;
	}
	
	/**
	 * @return appointment_service_type source table DSL
	 */
	@Override
	public String getDSL() {
		return "CREATE TABLE `appointment_service_type` (\n" + "  `appointment_service_type_id` int primary key,\n"
		        + "  `appointment_service_id` int,\n" + "  `name` VARCHAR,\n" + "  `duration_mins` int,\n"
		        + "  `date_created` TIMESTAMP,\n" + "  `creator` int,\n" + "  `date_changed` TIMESTAMP,\n"
		        + "  `changed_by` int,\n" + "  `voided` BOOLEAN,\n" + "  `voided_by` int,\n" + "  `date_voided` TIMESTAMP,\n"
		        + "  `void_reason` VARCHAR,\n" + "  `uuid` VARCHAR\n" + ")\n" + " WITH (\n"
		        + ConnectorUtils.propertyJoiner(",", "=").apply(connectorOptions) + ")";
	}
}
