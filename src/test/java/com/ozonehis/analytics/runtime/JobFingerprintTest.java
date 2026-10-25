package com.ozonehis.analytics.runtime;

import static org.assertj.core.api.Assertions.assertThat;

import com.ozonehis.analytics.pipeline.Job;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * A fingerprint change replaces a job, and a replaced job rebuilds its whole table. So it must
 * change for anything that changes what the job computes, and for nothing else.
 */
class JobFingerprintTest {

    private static final String PATIENT =
            "CREATE TABLE `patient` (`patient_id` int) WITH ('topic' = 'emr.openmrs.patient')";
    private static final String LOCATION =
            "CREATE TABLE `location` (`location_id` int) WITH ('topic' = 'emr.openmrs.location')";
    private static final String CONCEPT =
            "CREATE TABLE `concept` (`concept_id` int) WITH ('topic' = 'emr.openmrs.concept')";

    private static final String QUERY = """
            INSERT INTO `analytics`.`analytics`.`patients`
            -- one row per patient
            SELECT /*+ STATE_TTL('patient' = '0') */ patient.patient_id
            FROM patient /* the table */""";

    private static String fingerprint(String query, String... tables) {
        return JobFingerprint.of(new Job("streaming-patients", List.of(tables), query));
    }

    @Test
    void isShortHexAndStable() {
        assertThat(fingerprint(QUERY, PATIENT)).matches("[0-9a-f]{8}").isEqualTo(fingerprint(QUERY, PATIENT));
    }

    @Test
    void changesWhenTheQueryChanges() {
        assertThat(fingerprint(QUERY.replace("patient.patient_id", "patient.patient_id, 1"), PATIENT))
                .isNotEqualTo(fingerprint(QUERY, PATIENT));
    }

    @Test
    void changesWhenAnOptimizerHintChanges() {
        assertThat(fingerprint(QUERY.replace("'0'", "'1h'"), PATIENT)).isNotEqualTo(fingerprint(QUERY, PATIENT));
    }

    @Test
    void changesWhenATableTheQueryReadsChanges() {
        assertThat(fingerprint(QUERY, PATIENT.replace("int)", "int, `voided` boolean)")))
                .isNotEqualTo(fingerprint(QUERY, PATIENT));
    }

    @Test
    void ignoresCommentsAndSpacing() {
        String reformatted = """
                INSERT INTO `analytics`.`analytics`.`patients`   SELECT /*+ STATE_TTL('patient' = '0') */
                    patient.patient_id   -- reworded comment
                FROM   patient""";

        assertThat(fingerprint(reformatted, PATIENT)).isEqualTo(fingerprint(QUERY, PATIENT));
    }

    @Test
    void ignoresTablesTheQueryDoesNotMention() {
        // Every job registers every source table. A change to one this job never reads must not
        // replace it.
        assertThat(fingerprint(QUERY, PATIENT, CONCEPT))
                .isEqualTo(fingerprint(QUERY, PATIENT, CONCEPT.replace("int)", "int, `uuid` char)")))
                .isEqualTo(fingerprint(QUERY, PATIENT));
    }

    @Test
    void readsATableMentionedAnywhereInTheQuery() {
        String withLocation = QUERY + " LEFT JOIN location ON location.location_id = patient.patient_id";

        assertThat(fingerprint(withLocation, PATIENT, LOCATION))
                .isNotEqualTo(fingerprint(withLocation, PATIENT, LOCATION.replace("int)", "int, `name` varchar)")));
    }

    @Test
    void keepsStringLiteralsExactly() {
        assertThat(JobFingerprint.normalize("SELECT ' -- not a comment  ', 'it''s'  -- a comment\nFROM t"))
                .isEqualTo("SELECT ' -- not a comment  ', 'it''s' FROM t");
    }
}
