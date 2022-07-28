package net.mekomsolutions.data.pipelines.shared.dsl.source;

import net.mekomsolutions.data.pipelines.shared.dsl.TableSQLDSL;
import net.mekomsolutions.data.pipelines.utils.ConnectorUtils;

import java.util.Map;
import java.util.Objects;

/**
 * This class represents a Flink <a href="https://ci.apache.org/projects/flink/flink-docs-master/docs/dev/table/sourcessinks/">Source table</a>
 * Mapping to OpenMRS Program class
 */
public class Program implements TableSQLDSL {
    private Map<String, String> connectorOptions;
    public Program(Map<String, String> connectorOptions) {

        if(Objects.equals(connectorOptions.get("connector"), "kafka")){
            connectorOptions.put("topic","openmrs.openmrs.program");
        }else{
            connectorOptions.put("table-name","program");
        }
        this.connectorOptions = connectorOptions;
    }

    /**
     * @return program source table DSL
     */
    @Override
    public String getDSL() {
        return "CREATE TABLE `program` (\n" + 
        		"  `program_id` int primary key,\n" + 
        		"  `concept_id` int,\n" + 
        		"  `outcomes_concept_id` int,\n" + 
        		"  `creator` int,\n" + 
        		"  `date_created` TIMESTAMP,\n" + 
        		"  `changed_by` int,\n" + 
        		"  `date_changed` TIMESTAMP,\n" + 
        		"  `retired` BOOLEAN,\n" + 
        		"  `name` VARCHAR,\n" + 
        		"  `description` VARCHAR,\n" + 
        		"  `uuid` VARCHAR\n" + 
        		")\n" +
                " WITH (\n" +
                    ConnectorUtils.propertyJoiner(",","=").apply(connectorOptions) +
                ")";
    }
}
