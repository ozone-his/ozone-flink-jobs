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
public class PatientProgramsFs implements TableSQLDSL {
	
	private Map<String, String> connectorOptions;
	
	public PatientProgramsFs(Map<String, String> connectorOptions) {
		this.connectorOptions = connectorOptions;
	}
	
	/**
	 * @return patient_programs table DSL
	 */
	@Override
	public String getDSL() {
		return "CREATE TABLE `patient_programs_fs` (\n" + "  `patient_program_id` int,\n" + "  `patient_id` int,\n"
		        + "  `program_id` int,\n" + "  `date_enrolled` TIMESTAMP,\n" + "  `date_completed` TIMESTAMP,\n"
		        + "  `location_id` int,\n" + "  `outcome_concept_id` int,\n" + "  `creator` int,\n"
		        + "  `date_created` TIMESTAMP,\n" + "  `changed_by` int,\n" + "  `date_changed` TIMESTAMP,\n"
		        + "  `voided` BOOLEAN,\n" + "  `voided_by` int,\n" + "  `date_voided` TIMESTAMP,\n"
		        + "  `void_reason` VARCHAR,\n" + "  `uuid` VARCHAR,\n" + "  `program_retired` BOOLEAN,\n"
		        + "  `program_name` VARCHAR,\n" + "  `program_description` VARCHAR,\n" + "  `program_uuid` VARCHAR,\n"
		        + "  `program_concept_id` int,\n" + "  `concept_name` VARCHAR,\n" + "  `concept_uuid` VARCHAR,\n"
		        + "  `program_outcomes_concept_id` int,\n" + "  `outcomes_concept_name` VARCHAR,\n"
		        + "  `outcomes_concept_uuid` VARCHAR\n" + ")\n" + "WITH (\n"
		        + ConnectorUtils.propertyJoiner(",", "=").apply(this.connectorOptions) + ")";
	}
	
}
