package com.ozonehis.analytics.sql;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class SqlScriptsTest {

    @Test
    void loadsScriptsSortedByNameSoTheJobGraphIsReproducible(@TempDir Path dir) throws IOException {
        Files.writeString(dir.resolve("visits.sql"), "SELECT 2");
        Files.writeString(dir.resolve("patients.sql"), "SELECT 1");
        Files.writeString(dir.resolve("encounters.sql"), "SELECT 3");

        assertThat(SqlScripts.loadAll(dir))
                .extracting(SqlScript::name)
                .containsExactly("encounters", "patients", "visits");
    }

    @Test
    void namesScriptAfterFileWithoutExtensionAndKeepsContent(@TempDir Path dir) throws IOException {
        Files.writeString(dir.resolve("patients.sql"), "SELECT patient_id FROM patient");

        List<SqlScript> scripts = SqlScripts.loadAll(dir);

        assertThat(scripts).singleElement().satisfies(script -> {
            assertThat(script.name()).isEqualTo("patients");
            assertThat(script.content()).isEqualTo("SELECT patient_id FROM patient");
        });
    }

    @Test
    void ignoresNonSqlFiles(@TempDir Path dir) throws IOException {
        Files.writeString(dir.resolve("patients.sql"), "SELECT 1");
        Files.writeString(dir.resolve("README.md"), "not sql");

        assertThat(SqlScripts.loadAll(dir)).extracting(SqlScript::name).containsExactly("patients");
    }

    @Test
    void failsWhenDirectoryIsMissingRatherThanSilentlyProducingNothing(@TempDir Path dir) {
        Path missing = dir.resolve("nope");

        assertThatThrownBy(() -> SqlScripts.loadAll(missing))
                .isInstanceOf(SqlScriptException.class)
                .hasMessageContaining("SQL directory not found");
    }

    @Test
    void failsWhenDirectoryHasNoScripts(@TempDir Path dir) {
        assertThatThrownBy(() -> SqlScripts.loadAll(dir))
                .isInstanceOf(SqlScriptException.class)
                .hasMessageContaining("No .sql files found");
    }
}
