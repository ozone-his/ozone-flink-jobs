package net.mekomsolutions.data.pipelines.shared.dsl;

import net.mekomsolutions.data.pipelines.shared.dsl.sink.filesystem.AppointmentsFs;
import net.mekomsolutions.data.pipelines.shared.dsl.sink.filesystem.ConceptsFs;
import net.mekomsolutions.data.pipelines.shared.dsl.sink.filesystem.EncounterDiagnosesFs;
import net.mekomsolutions.data.pipelines.shared.dsl.sink.filesystem.EncountersFs;
import net.mekomsolutions.data.pipelines.shared.dsl.sink.filesystem.FlattenedConditionsFs;
import net.mekomsolutions.data.pipelines.shared.dsl.sink.filesystem.FlattenedOrdersFs;
import net.mekomsolutions.data.pipelines.shared.dsl.sink.filesystem.ObservationsFs;
import net.mekomsolutions.data.pipelines.shared.dsl.sink.filesystem.PatientProgramsFs;
import net.mekomsolutions.data.pipelines.shared.dsl.sink.filesystem.PatientsFs;
import net.mekomsolutions.data.pipelines.shared.dsl.sink.filesystem.VisitsFs;
import net.mekomsolutions.data.pipelines.shared.dsl.sink.jdbc.Appointments;
import net.mekomsolutions.data.pipelines.shared.dsl.sink.jdbc.Concepts;
import net.mekomsolutions.data.pipelines.shared.dsl.sink.jdbc.EncounterDiagnoses;
import net.mekomsolutions.data.pipelines.shared.dsl.sink.jdbc.Encounters;
import net.mekomsolutions.data.pipelines.shared.dsl.sink.jdbc.FlattenedConditions;
import net.mekomsolutions.data.pipelines.shared.dsl.sink.jdbc.FlattenedOrders;
import net.mekomsolutions.data.pipelines.shared.dsl.sink.jdbc.Observations;
import net.mekomsolutions.data.pipelines.shared.dsl.sink.jdbc.PatientPrograms;
import net.mekomsolutions.data.pipelines.shared.dsl.sink.jdbc.Patients;
import net.mekomsolutions.data.pipelines.shared.dsl.sink.jdbc.Visits;
import net.mekomsolutions.data.pipelines.shared.dsl.source.*;
import net.mekomsolutions.data.pipelines.streaming.StreamingETLJob;
import org.apache.logging.slf4j.Log4jLoggerFactory;
import org.slf4j.Logger;

import java.util.Map;

/**
 *  The class provides a factory method for getting Source/Sink Dynamic Table DSL for use in {@link StreamingETLJob ETLJob.class}
 */
public class TableDSLFactory {
    private static final Logger LOG = new Log4jLoggerFactory().getLogger(TableDSLFactory.class.getName());
    private Map<String, String> connectorOptions;
    public TableDSLFactory(Map<String, String> connectorOptions) {
        this.connectorOptions = connectorOptions;
    }

    /**
     * @param tableName
     * @return Table DSL for the requested table
     */
    public TableSQLDSL getTable(String tableName){
        TableSQLDSL tableSQLDSL = null;
        switch (tableName) {
            case "concept":
                tableSQLDSL = new Concept(this.connectorOptions);
                break;
            case "concept_name":
                tableSQLDSL = new ConceptName(this.connectorOptions);
                break;
            case "concept_reference_map":
                tableSQLDSL = new ConceptReferenceMap(this.connectorOptions);
                break;
            case "concept_reference_source":
                tableSQLDSL = new ConceptReferenceSource(this.connectorOptions);
                break;
            case "concept_reference_term":
                tableSQLDSL = new ConceptReferenceTerm(this.connectorOptions);
                break;
            case "encounter":
                tableSQLDSL = new Encounter(this.connectorOptions);
                break;
            case "encounter_type":
                tableSQLDSL = new EncounterType(this.connectorOptions);
                break;
            case "location":
                tableSQLDSL = new Location(this.connectorOptions);
                break;
            case "person":
                tableSQLDSL = new Person(this.connectorOptions);
                break;
            case "person_name":
                tableSQLDSL = new PersonName(this.connectorOptions);
                break;
            case "person_address":
                tableSQLDSL = new PersonAddress(this.connectorOptions);
                break;
            case "patient":
                tableSQLDSL = new Patient(this.connectorOptions);
                break;
            case "patient_identifier":
                tableSQLDSL = new PatientIdentifier(this.connectorOptions);
                break;
            case "patient_identifier_type":
                tableSQLDSL = new PatientIdentifierType(this.connectorOptions);
                break;
            case "visit":
                tableSQLDSL = new Visit(this.connectorOptions);
                break;
            case "visit_type":
                tableSQLDSL = new VisitType(this.connectorOptions);
                break;
            case "obs":
                tableSQLDSL = new Obs(this.connectorOptions);
                break;
            case "visits":
                tableSQLDSL = new Visits(this.connectorOptions);
                break;
            case "observations":
                tableSQLDSL = new Observations(this.connectorOptions);
                break;
            case "concepts":
                tableSQLDSL = new Concepts(this.connectorOptions);
                break;
            case "patients":
                tableSQLDSL = new Patients(this.connectorOptions);
                break;
            case "care_setting":
                tableSQLDSL = new CareSetting(this.connectorOptions);
                break;
            case "order_type":
                tableSQLDSL = new OrderType(this.connectorOptions);
                break;
            case "orders":
                tableSQLDSL = new Orders(this.connectorOptions);
                break;
            case "flattened_orders":
                tableSQLDSL = new FlattenedOrders(this.connectorOptions);
                break;
            case "flattened_orders_fs":
                tableSQLDSL = new FlattenedOrdersFs(this.connectorOptions);
                break;
            case "observations_fs":
                tableSQLDSL = new ObservationsFs(this.connectorOptions);
                break;
            case "concepts_fs":
                tableSQLDSL = new ConceptsFs(this.connectorOptions);
                break;
            case "patients_fs":
                tableSQLDSL = new PatientsFs(this.connectorOptions);
                break;
            case "visits_fs":
                tableSQLDSL = new VisitsFs(this.connectorOptions);
                break;
            case "form":
                tableSQLDSL = new Form(this.connectorOptions);
                break;
            case "encounters":
                tableSQLDSL = new Encounters(this.connectorOptions);
                break;
            case "encounters_fs":
                tableSQLDSL = new EncountersFs(this.connectorOptions);
                break;
            case "appointment_service":
                tableSQLDSL = new AppointmentService(this.connectorOptions);
                break;
            case "appointment_service_type":
                tableSQLDSL = new AppointmentServiceType(this.connectorOptions);
                break;
            case "patient_appointment":
                tableSQLDSL = new PatientAppointment(this.connectorOptions);
                break;
            case "patient_appointment_provider":
                tableSQLDSL = new PatientAppointmentProvider(this.connectorOptions);
                break;
            case "appointments":
                tableSQLDSL = new Appointments(this.connectorOptions);
                break;
            case "appointments_fs":
                tableSQLDSL = new AppointmentsFs(this.connectorOptions);
                break;
            case "patient_program":
                tableSQLDSL = new PatientProgram(this.connectorOptions);
                break;
            case "program":
                tableSQLDSL = new Program(this.connectorOptions);
                break;
            case "patient_programs":
                tableSQLDSL = new PatientPrograms(this.connectorOptions);
                break;
            case "patient_programs_fs":
                tableSQLDSL = new PatientProgramsFs(this.connectorOptions);
                break;
            case "conditions":
                tableSQLDSL = new Conditions(this.connectorOptions);
                break;
            case "flattened_conditions":
                tableSQLDSL = new FlattenedConditions(this.connectorOptions);
                break;
            case "flattened_conditions_fs":
                tableSQLDSL = new FlattenedConditionsFs(this.connectorOptions);
                break;
            case "encounter_diagnosis":
                tableSQLDSL = new EncounterDiagnosis(this.connectorOptions);
                break;
            case "encounter_diagnoses":
                tableSQLDSL = new EncounterDiagnoses(this.connectorOptions);
                break;
            case "encounter_diagnosis_fs":
                tableSQLDSL = new EncounterDiagnosesFs(this.connectorOptions);
                break;
            default:
                LOG.warn("Table DSL not found");
        }
        return tableSQLDSL;
    }

}
