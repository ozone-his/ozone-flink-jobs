package com.ozonehis.data.pipelines.shared.dsl.source;

import com.ozonehis.data.pipelines.shared.dsl.TableSQLDSL;
import com.ozonehis.data.pipelines.utils.ConnectorUtils;

import java.util.Map;
import java.util.Objects;

/**
 * This class represents a Flink <a href=
 * "https://ci.apache.org/projects/flink/flink-docs-master/docs/dev/table/sourcessinks/">Source
 * table</a> Mapping to OpenMRS CareSetting class
 */
public class CareSetting implements TableSQLDSL {
	
	private Map<String, String> connectorOptions;
	
	public CareSetting(Map<String, String> connectorOptions) {
		
		if (Objects.equals(connectorOptions.get("connector"), "kafka")) {
			connectorOptions.put("topic", "openmrs.openmrs.care_setting");
		} else {
			connectorOptions.put("table-name", "care_setting");
		}
		this.connectorOptions = connectorOptions;
	}
	
	/**
	 * @return care_setting source table DSL
	 */
	@Override
	public String getDSL() {
		return "CREATE TABLE `care_setting` (\n" + "  `care_setting_id` int primary key,\n" + "  `name` VARCHAR,\n"
		        + "  `description` VARCHAR,\n" + "  `care_setting_type` VARCHAR,\n" + "  `creator` int,\n"
		        + "  `date_created` TIMESTAMP,\n" + "  `retired` BOOLEAN,\n" + "  `retired_by` int,\n"
		        + "  `date_retired` TIMESTAMP,\n" + "  `retire_reason` VARCHAR,\n" + "  `changed_by` int,\n"
		        + "  `date_changed` TIMESTAMP,\n" + "  `uuid` VARCHAR\n" + ")\n" + " WITH (\n"
		        + ConnectorUtils.propertyJoiner(",", "=").apply(connectorOptions) + ")";
	}
}
