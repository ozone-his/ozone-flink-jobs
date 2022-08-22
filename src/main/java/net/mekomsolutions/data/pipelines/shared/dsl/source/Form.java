package net.mekomsolutions.data.pipelines.shared.dsl.source;

import net.mekomsolutions.data.pipelines.shared.dsl.TableSQLDSL;
import net.mekomsolutions.data.pipelines.utils.ConnectorUtils;

import java.util.Map;
import java.util.Objects;

/**
 * This class represents a Flink <a href=
 * "https://ci.apache.org/projects/flink/flink-docs-master/docs/dev/table/sourcessinks/">Source
 * table</a> Mapping to OpenMRS Form class
 */
public class Form implements TableSQLDSL {
	
	private Map<String, String> connectorOptions;
	
	public Form(Map<String, String> connectorOptions) {
		
		if (Objects.equals(connectorOptions.get("connector"), "kafka")) {
			connectorOptions.put("topic", "openmrs.openmrs.form");
		} else {
			connectorOptions.put("table-name", "form");
		}
		this.connectorOptions = connectorOptions;
	}
	
	/**
	 * @return form source table DSL
	 */
	@Override
	public String getDSL() {
		return "CREATE TABLE `form` (\n" + "  `form_id` int primary key,\n" + "  `name` VARCHAR,\n"
		        + "  `version` VARCHAR,\n" + "  `build` int,\n" + "  `published` BOOLEAN,\n" + "  `xslt` VARCHAR,\n"
		        + "  `template` VARCHAR,\n" + "  `description` VARCHAR,\n" + "  `encounter_type` int,\n"
		        + "  `creator` int,\n" + "  `date_created` TIMESTAMP,\n" + "  `changed_by` int,\n"
		        + "  `date_changed` TIMESTAMP,\n" + "  `retired` BOOLEAN,\n" + "  `retired_by` int,\n"
		        + "  `date_retired` TIMESTAMP,\n" + "  `retired_reason` VARCHAR,\n" + "  `uuid` VARCHAR\n" + ")\n"
		        + " WITH (\n" + ConnectorUtils.propertyJoiner(",", "=").apply(connectorOptions) + ")";
	}
}
