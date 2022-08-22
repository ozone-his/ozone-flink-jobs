package net.mekomsolutions.data.pipelines.shared.dsl.source;

import net.mekomsolutions.data.pipelines.shared.dsl.TableSQLDSL;
import net.mekomsolutions.data.pipelines.utils.ConnectorUtils;

import java.util.Map;
import java.util.Objects;

/**
 * This class represents a Flink <a href=
 * "https://ci.apache.org/projects/flink/flink-docs-master/docs/dev/table/sourcessinks/">Source
 * table</a> Mapping to OpenMRS PatientProgram class
 */
public class PatientProgram implements TableSQLDSL {
	
	private Map<String, String> connectorOptions;
	
	public PatientProgram(Map<String, String> connectorOptions) {
		
		if (Objects.equals(connectorOptions.get("connector"), "kafka")) {
			connectorOptions.put("topic", "openmrs.openmrs.patient_program");
		} else {
			connectorOptions.put("table-name", "patient_program");
		}
		this.connectorOptions = connectorOptions;
	}
	
	/**
	 * @return patient_program source table DSL
	 */
	@Override
	public String getDSL() {
		return "CREATE TABLE `patient_program` (\n" + "  `patient_program_id` int primary key,\n" + "  `patient_id` int,\n"
		        + "  `program_id` int,\n" + "  `date_enrolled` TIMESTAMP,\n" + "  `date_completed` TIMESTAMP,\n"
		        + "  `location_id` int,\n" + "  `outcome_concept_id` int,\n" + "  `creator` int,\n"
		        + "  `date_created` TIMESTAMP,\n" + "  `changed_by` int,\n" + "  `date_changed` TIMESTAMP,\n"
		        + "  `voided` BOOLEAN,\n" + "  `voided_by` int,\n" + "  `date_voided` TIMESTAMP,\n"
		        + "  `void_reason` VARCHAR,\n" + "  `uuid` VARCHAR\n" + ")\n" + " WITH (\n"
		        + ConnectorUtils.propertyJoiner(",", "=").apply(connectorOptions) + ")";
	}
}
