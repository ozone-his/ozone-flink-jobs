package net.mekomsolutions.data.pipelines.shared.dsl.sink.jdbc;

import net.mekomsolutions.data.pipelines.shared.dsl.TableSQLDSL;
import net.mekomsolutions.data.pipelines.utils.ConnectorUtils;

import java.util.Map;
import java.util.Objects;

/**
 * This creates a Flink <a href="https://ci.apache.org/projects/flink/flink-docs-master/docs/dev/table/sourcessinks/">Sink table</a>
 * For flattening OpenMRS Patient Programs
 */
public class FlattenedConditions  implements TableSQLDSL {
    private Map<String, String> connectorOptions;
    public FlattenedConditions(Map<String, String> connectorOptions) {
        if(!Objects.equals(connectorOptions.get("connector"), "filesystem")){
            connectorOptions.put("table-name","_conditions");
        }else{
            connectorOptions.put("path","/tmp/analytics/_conditions");
        }
        this.connectorOptions = connectorOptions;
    }

    /**
     * @return _conditions table DSL
     */
    @Override
    public String getDSL() {
        return "CREATE TABLE `_conditions` (\n" + 
        		"  `condition_id` int primary key,\n" + 
        		"  `additional_detail` VARCHAR,\n" + 
        		"  `previous_version` int,\n" + 
        		"  `condition_coded` int,\n" + 
        		"  `condition_non_coded` VARCHAR,\n" + 
        		"  `condition_coded_name` int,\n" + 
        		"  `clinical_status` VARCHAR,\n" + 
        		"  `verification_status` VARCHAR,\n" + 
        		"  `onset_date` TIMESTAMP,\n" + 
        		"  `date_created` TIMESTAMP,\n" + 
        		"  `voided` BOOLEAN,\n" + 
        		"  `date_voided` TIMESTAMP,\n" + 
        		"  `void_reason` VARCHAR,\n" + 
        		"  `uuid` VARCHAR,\n" + 
        		"  `creator` int,\n" + 
        		"  `voided_by` int,\n" + 
        		"  `changed_by` int,\n" + 
        		"  `patient_id` int,\n" + 
        		"  `end_date` TIMESTAMP,\n" + 
        		"  `date_changed` TIMESTAMP,\n" + 
        		"  `encounter_id` int\n" + 
        		")\n" +
                "WITH (\n" +
                    ConnectorUtils.propertyJoiner(",","=").apply(this.connectorOptions) +
                ")";
    }


}
