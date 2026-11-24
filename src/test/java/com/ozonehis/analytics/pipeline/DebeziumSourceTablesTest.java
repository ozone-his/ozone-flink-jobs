package com.ozonehis.analytics.pipeline;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.ozonehis.analytics.runtime.JobFingerprint;
import com.ozonehis.analytics.sql.ConnectorOptions;
import com.ozonehis.analytics.sql.SqlScript;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class DebeziumSourceTablesTest {

    private static final SqlScript PERSON = new SqlScript("person", """
            CREATE TABLE `person` (
              `person_id` int,
              `birthdate` DATE,
              `dead` BOOLEAN,
              `date_created` TIMESTAMP,
              PRIMARY KEY (`person_id`) NOT ENFORCED
            )""");

    private static final ConnectorOptions KAFKA = ConnectorOptions.of("kafka").set("topic", "emr.openmrs.person");

    private final DebeziumSourceTables tables = new DebeziumSourceTables();

    @Test
    void readsDatesAndTimesAsTextAndConvertsThemInAViewNamedAfterTheTable() {
        List<String> definitions = tables.definitions(PERSON, KAFKA);

        assertThat(definitions).hasSize(2);
        assertThat(definitions.get(0))
                .startsWith("CREATE TABLE `person$debezium`")
                .contains("`birthdate` STRING", "`date_created` STRING")
                // Columns Flink already reads correctly keep their type.
                .contains("`person_id` INT NOT NULL", "`dead` BOOLEAN")
                .contains("PRIMARY KEY (`person_id`) NOT ENFORCED", "'topic' = 'emr.openmrs.person'");
        assertThat(definitions.get(1))
                .startsWith("CREATE TEMPORARY VIEW `person`")
                .contains("CAST(DEBEZIUM_TEMPORAL(`birthdate`, 'DATE') AS DATE) AS `birthdate`")
                .contains("CAST(DEBEZIUM_TEMPORAL(`date_created`, 'TIMESTAMP') AS TIMESTAMP(6)) AS `date_created`")
                .contains("FROM `person$debezium`");
    }

    @Test
    void readsADefinitionHoweverItIsLaidOut() {
        SqlScript untidy = new SqlScript("person", """
                -- People, patients included.
                /* CREATE TABLE `not_this_one` (`id` int) */

                create   table
                    if not exists person(person_id int,birthdate date
                  , primary key (person_id) not enforced)  -- trailing note""");

        List<String> definitions = tables.definitions(untidy, KAFKA);

        assertThat(definitions).hasSize(2);
        assertThat(definitions.get(0))
                .startsWith("CREATE TABLE `person$debezium`")
                .contains("`birthdate` STRING", "PRIMARY KEY (`person_id`) NOT ENFORCED");
        assertThat(definitions.get(1)).startsWith("CREATE TEMPORARY VIEW `person`");
    }

    @Test
    void namesTheFileWhenADefinitionCannotBeRead() {
        SqlScript qualified = new SqlScript("person", "CREATE TABLE `default_database`.`person` (`birthdate` DATE)");

        assertThatThrownBy(() -> tables.definitions(qualified, KAFKA))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("'person'");
    }

    @Test
    void aTableWithoutDatesOrTimesIsRegisteredAsWritten() {
        SqlScript patient = new SqlScript("patient", "CREATE TABLE `patient` (`patient_id` int, `creator` int)");

        assertThat(tables.definitions(patient, KAFKA))
                .singleElement()
                .asString()
                .startsWith("CREATE TABLE `patient`");
    }

    @Test
    void aJobsFingerprintCoversOnlyTheTablesItsQueryReads() {
        SqlScript visit = new SqlScript("visit", "CREATE TABLE `visit` (`visit_id` int, `date_started` TIMESTAMP)");
        List<String> person = tables.definitions(PERSON, KAFKA);
        List<String> both = new ArrayList<>(person);
        both.addAll(tables.definitions(visit, KAFKA));
        String query = "INSERT INTO sink SELECT person_id FROM person";

        // Registering visit, which the query never reads, changes nothing.
        assertThat(JobFingerprint.of(new Job("streaming-x", both, query)))
                .isEqualTo(JobFingerprint.of(new Job("streaming-x", person, query)));
        // A change to the raw table behind person, here its topic, does.
        List<String> moved =
                tables.definitions(PERSON, ConnectorOptions.of("kafka").set("topic", "other.person"));
        assertThat(JobFingerprint.of(new Job("streaming-x", moved, query)))
                .isNotEqualTo(JobFingerprint.of(new Job("streaming-x", person, query)));
    }
}
