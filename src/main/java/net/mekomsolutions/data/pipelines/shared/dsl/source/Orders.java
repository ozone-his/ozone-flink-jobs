package net.mekomsolutions.data.pipelines.shared.dsl.source;

import net.mekomsolutions.data.pipelines.shared.dsl.TableSQLDSL;
import net.mekomsolutions.data.pipelines.utils.ConnectorUtils;

import java.util.Map;
import java.util.Objects;

/**
 * This class represents a Flink <a href="https://ci.apache.org/projects/flink/flink-docs-master/docs/dev/table/sourcessinks/">Source table</a>
 * Mapping to OpenMRS Orders class
 */
public class Orders implements TableSQLDSL {
    private Map<String, String> connectorOptions;
    public Orders(Map<String, String> connectorOptions) {

        if(Objects.equals(connectorOptions.get("connector"), "kafka")){
            connectorOptions.put("topic","openmrs.openmrs.orders");
        }else{
            connectorOptions.put("table-name","orders");
        }
        this.connectorOptions = connectorOptions;
    }

    /**
     * @return orders source table DSL
     */
    @Override
    public String getDSL() {
        return "CREATE TABLE `orders` (\n" + 
        		"  `order_id` int primary key,\n" + 
        		"  `order_type_id` int,\n" + 
        		"  `concept_id` int,\n" + 
        		"  `orderer` int,\n" + 
        		"  `encounter_id` int,\n" + 
        		"  `instructions` VARCHAR,\n" + 
        		"  `date_activated` TIMESTAMP,\n" + 
        		"  `auto_expire_date` TIMESTAMP,\n" + 
        		"  `date_stopped` TIMESTAMP,\n" + 
        		"  `order_reason` int,\n" + 
        		"  `order_reason_non_coded` VARCHAR,\n" + 
        		"  `creator` int,\n" + 
        		"  `date_created` TIMESTAMP,\n" + 
        		"  `voided` BOOLEAN,\n" + 
        		"  `voided_by` int,\n" + 
        		"  `date_voided` TIMESTAMP,\n" + 
        		"  `void_reason` VARCHAR,\n" + 
        		"  `patient_id` int,\n" + 
        		"  `accession_number` VARCHAR,\n" + 
        		"  `uuid` char(38) NOT NULL,\n" + 
        		"  `urgency` VARCHAR,\n" + 
        		"  `order_number` VARCHAR,\n" + 
        		"  `previous_order_id` int,\n" + 
        		"  `order_action` VARCHAR,\n" + 
        		"  `comment_to_fulfiller` VARCHAR,\n" + 
        		"  `care_setting` int,\n" + 
        		"  `scheduled_date` TIMESTAMP,\n" + 
        		"  `order_group_id` int,\n" + 
        		"  `sort_weight` DOUBLE,\n" + 
        		"  `fulfiller_comment` VARCHAR,\n" + 
        		"  `fulfiller_status` VARCHAR,\n" + 
        		"  `form_namespace_and_path` VARCHAR\n" + 
        		")\n" +
                " WITH (\n" +
                    ConnectorUtils.propertyJoiner(",","=").apply(connectorOptions) +
                ")";
    }
}
