package net.mekomsolutions.data.pipelines.shared.dsl.sink.filesystem;

import net.mekomsolutions.data.pipelines.shared.dsl.TableSQLDSL;
import net.mekomsolutions.data.pipelines.utils.ConnectorUtils;

import java.util.Map;
import java.util.Objects;

/**
 * This creates a Flink <a href=
 * "https://ci.apache.org/projects/flink/flink-docs-master/docs/dev/table/sourcessinks/">Sink
 * table</a> For flattening OpenMRS Encounter Diagnoses
 */
public class EncounterDiagnosesFs implements TableSQLDSL {
	
	private Map<String, String> connectorOptions;
	
	public EncounterDiagnosesFs(Map<String, String> connectorOptions) {
		this.connectorOptions = connectorOptions;
	}
	
	/**
	 * @return encounter_diagnoses table DSL
	 */
	@Override
	public String getDSL() {
		return "CREATE TABLE `encounter_diagnoses_fs` (\n" + "  `diagnosis_id` BIGINT PRIMARY KEY,\n"
		        + "  `diagnosis_coded` BIGINT,\n" + "  `diagnosis_non_coded` VARCHAR,\n"
		        + "  `diagnosis_coded_name` BIGINT,\n" + "  `encounter_id` BIGINT,\n" + "  `patient_id` BIGINT,\n"
		        + "  `certainty` VARCHAR,\n" + "  `rank` BIGINT,\n" + "  `uuid` VARCHAR,\n" + "  `creator` BIGINT,\n"
		        + "  `date_created` TIMESTAMP,\n" + "  `voided` BOOLEAN,\n" + "  `voided_by` BIGINT,\n"
		        + "  `date_voided` TIMESTAMP,\n" + "  `void_reason` VARCHAR\n" + ")\n" + "WITH (\n"
		        + ConnectorUtils.propertyJoiner(",", "=").apply(this.connectorOptions) + ")";
	}
	
}
