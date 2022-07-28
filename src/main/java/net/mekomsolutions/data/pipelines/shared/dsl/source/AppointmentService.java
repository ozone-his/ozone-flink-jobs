package net.mekomsolutions.data.pipelines.shared.dsl.source;

import net.mekomsolutions.data.pipelines.shared.dsl.TableSQLDSL;
import net.mekomsolutions.data.pipelines.utils.ConnectorUtils;

import java.util.Map;
import java.util.Objects;

/**
 * This class represents a Flink <a href="https://ci.apache.org/projects/flink/flink-docs-master/docs/dev/table/sourcessinks/">Source table</a>
 * Mapping to OpenMRS Module Bahmni Appointments AppointmentService class
 */
public class AppointmentService implements TableSQLDSL {
    private Map<String, String> connectorOptions;
    public AppointmentService(Map<String, String> connectorOptions) {

        if(Objects.equals(connectorOptions.get("connector"), "kafka")){
            connectorOptions.put("topic","openmrs.openmrs.appointment_service");
        }else{
            connectorOptions.put("table-name","appointment_service");
        }
        this.connectorOptions = connectorOptions;
    }

    /**
     * @return appointment_service source table DSL
     */
    @Override
    public String getDSL() {
        return "CREATE TABLE `appointment_service` (\n" + 
        		"  `appointment_service_id` int primary key,\n" + 
        		"  `name` VARCHAR,\n" + 
        		"  `description` VARCHAR,\n" + 
        		"  `start_time` TIMESTAMP,\n" + 
        		"  `end_time` TIMESTAMP,\n" + 
        		"  `location_id` int,\n" + 
        		"  `speciality_id` int,\n" + 
        		"  `max_appointments_limit` int,\n" + 
        		"  `duration_mins` int,\n" + 
        		"  `color` VARCHAR,\n" + 
        		"  `date_created` TIMESTAMP,\n" + 
        		"  `creator` int,\n" + 
        		"  `date_changed` TIMESTAMP,\n" + 
        		"  `changed_by` int,\n" + 
        		"  `voided` BOOLEAN,\n" + 
        		"  `voided_by` int,\n" + 
        		"  `date_voided` TIMESTAMP,\n" + 
        		"  `void_reason` VARCHAR,\n" + 
        		"  `uuid` VARCHAR,\n" + 
        		"  `initial_appointment_status` VARCHAR\n" + 
        		")\n" +
                " WITH (\n" +
                    ConnectorUtils.propertyJoiner(",","=").apply(connectorOptions) +
                ")";
    }
}
