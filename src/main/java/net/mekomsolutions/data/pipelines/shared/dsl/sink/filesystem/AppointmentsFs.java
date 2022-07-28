package net.mekomsolutions.data.pipelines.shared.dsl.sink.filesystem;

import net.mekomsolutions.data.pipelines.shared.dsl.TableSQLDSL;
import net.mekomsolutions.data.pipelines.utils.ConnectorUtils;

import java.util.Map;
import java.util.Objects;

/**
 * This creates a Flink <a href="https://ci.apache.org/projects/flink/flink-docs-master/docs/dev/table/sourcessinks/">Sink table</a>
 * For flattening OpenMRS Module Bahmni Appointments Appointments 
 */
public class AppointmentsFs implements TableSQLDSL {
    private Map<String, String> connectorOptions;
    public AppointmentsFs(Map<String, String> connectorOptions) {
        this.connectorOptions = connectorOptions;
    }

    /**
     * @return appointments table DSL
     */
    @Override
    public String getDSL() {
        return "CREATE TABLE `appointments` (\n" + 
        		"  `patient_appointment_id` int,\n" + 
        		"  `patient_id` int,\n" + 
        		"  `appointment_number` VARCHAR,\n" + 
        		"  `start_date_time` TIMESTAMP,\n" + 
        		"  `location_id` int,\n" + 
        		"  `end_date_time` TIMESTAMP,\n" + 
        		"  `appointment_service_id` int,\n" + 
        		"  `appointment_service_type_id` int,\n" + 
        		"  `status` VARCHAR,\n" + 
        		"  `appointment_kind` VARCHAR,\n" + 
        		"  `comments` VARCHAR,\n" + 
        		"  `related_appointment_id` int,\n" + 
        		"  `creator` int,\n" + 
        		"  `date_created` TIMESTAMP,\n" + 
        		"  `changed_by` int,\n" + 
        		"  `date_changed` TIMESTAMP,\n" + 
        		"  `voided` BOOLEAN,\n" + 
        		"  `voided_by` int,\n" + 
        		"  `date_voided` TIMESTAMP,\n" + 
        		"  `void_reason` VARCHAR,\n" + 
        		"  `uuid` VARCHAR,\n" + 
        		"  `appointment_service_name` VARCHAR,\n" + 
        		"  `appointment_service_description` VARCHAR,\n" + 
        		"  `appointment_service_voided` BOOLEAN,\n" + 
        		"  `appointment_service_uuid` VARCHAR,\n" + 
        		"  `appointment_service_color` VARCHAR,\n" + 
        		"  `appointment_service_start_time` TIMESTAMP,\n" + 
        		"  `appointment_service_end_time` TIMESTAMP,\n" + 
        		"  `appointment_service_speciality_id` int,\n" + 
        		"  `appointment_service_max_appointments_limit` int,\n" + 
        		"  `appointment_service_duration_mins` int,\n" + 
        		"  `appointment_service_initial_appointment_status` VARCHAR,\n" + 
        		"  `appointment_service_type_name` VARCHAR,\n" + 
        		"  `appointment_service_type_duration_mins` int,\n" + 
        		"  `appointment_service_type_voided` BOOLEAN,\n" + 
        		"  `appointment_service_type_uuid` VARCHAR,\n" + 
        		"  `patient_appointment_provider` int,\n" + 
        		"  `patient_appointment_provider_response` VARCHAR\n" + 
        		")\n" +
                "WITH (\n" +
                    ConnectorUtils.propertyJoiner(",","=").apply(this.connectorOptions) +
                ")";
    }


}
