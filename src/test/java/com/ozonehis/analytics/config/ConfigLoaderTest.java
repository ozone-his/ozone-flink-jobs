package com.ozonehis.analytics.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class ConfigLoaderTest {

    private static final String MINIMAL =
            """
            catalogs:
              - name: ozone
                dialect: postgresql
                defaultDatabase: analytics
                baseUrl: jdbc:postgresql://localhost:5432
                username: analytics
                password: secret
            """;

    private final ConfigLoader loader = new ConfigLoader();

    @Test
    void parsesCatalogAndDefaultsAbsentSourcesAndSinksToEmpty() {
        AnalyticsConfig config = loader.parse(MINIMAL, "test");

        assertThat(config.catalogs()).singleElement().satisfies(catalog -> {
            assertThat(catalog.name()).isEqualTo("ozone");
            assertThat(catalog.dialect()).isEqualTo(AnalyticsConfig.Dialect.POSTGRESQL);
        });
        assertThat(config.sources().kafka()).isEmpty();
        assertThat(config.sinks().tables()).isEmpty();
    }

    @Test
    void acceptsDialectRegardlessOfCase() {
        AnalyticsConfig config = loader.parse(MINIMAL.replace("postgresql", "PostgreSQL"), "test");

        assertThat(config.catalogs().get(0).dialect()).isEqualTo(AnalyticsConfig.Dialect.POSTGRESQL);
    }

    @Test
    void substitutesEnvironmentPlaceholdersIncludingDefaults() {
        ConfigLoader loader = ConfigLoader.withVariables(Map.of("DB_USER", "from-env"));
        String yaml = MINIMAL.replace("username: analytics", "username: ${DB_USER}")
                .replace("password: secret", "password: ${DB_PASS:-fallback}");

        AnalyticsConfig config = loader.parse(yaml, "test");

        assertThat(config.catalogs().get(0).username()).isEqualTo("from-env");
        assertThat(config.catalogs().get(0).password()).isEqualTo("fallback");
    }

    /**
     * Substituting into the raw YAML text would let a value's contents change the document's
     * structure. These are the characters that break it.
     */
    @ParameterizedTest
    @ValueSource(strings = {"pa'ss", "pa\"ss", "pa: ss", "pa#ss", "pa\nss", "{a}", "[b]", "*c", "&d", "!!str"})
    void secretsContainingYamlSyntaxAreTakenLiterally(String secret) {
        ConfigLoader loader = ConfigLoader.withVariables(Map.of("DB_PASS", secret));
        String yaml = MINIMAL.replace("password: secret", "password: ${DB_PASS}");

        AnalyticsConfig config = loader.parse(yaml, "test");

        assertThat(config.catalogs().get(0).password()).isEqualTo(secret);
    }

    @Test
    void rejectsUnknownKeysRatherThanIgnoringATypo() {
        String yaml = MINIMAL + "unexpectedKey: oops\n";

        assertThatThrownBy(() -> loader.parse(yaml, "test"))
                .isInstanceOf(ConfigException.class)
                .hasMessageContaining("unexpectedKey");
    }

    @Test
    void reportsTheMissingFieldByName() {
        String yaml = MINIMAL.replace("    baseUrl: jdbc:postgresql://localhost:5432\n", "");

        assertThatThrownBy(() -> loader.parse(yaml, "test"))
                .isInstanceOf(ConfigException.class)
                .hasMessageContaining("catalogs[].baseUrl");
    }

    @Test
    void rejectsAnUnknownDialect() {
        assertThatThrownBy(() -> loader.parse(MINIMAL.replace("postgresql", "oracle"), "test"))
                .isInstanceOf(ConfigException.class);
    }

    @Test
    void rejectsDuplicateCatalogNames() {
        assertThatThrownBy(() -> loader.parse(MINIMAL + MINIMAL.replace("catalogs:\n", ""), "test"))
                .isInstanceOf(ConfigException.class)
                .hasMessageContaining("Duplicate");
    }

    @Test
    void requiresAtLeastOneCatalog() {
        assertThatThrownBy(() -> loader.parse("catalogs: []\n", "test"))
                .isInstanceOf(ConfigException.class)
                .hasMessageContaining("catalogs");
    }
}
