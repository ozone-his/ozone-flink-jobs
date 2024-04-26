package com.ozonehis.data.pipelines;

import static org.openmrs.util.OpenmrsConstants.KEY_OPENMRS_APPLICATION_DATA_DIRECTORY;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;

public abstract class BaseOpenmrsJobTest extends BaseJobTest {

    private static final String LIQUIBASE_ANALYTICS = "liquibase/analytics/changelogs/0001-init.xml";

    @BeforeAll
    public static void beforeAll() {
        System.setProperty(KEY_OPENMRS_APPLICATION_DATA_DIRECTORY, testDir);
    }

    @AfterAll
    public static void afterAll() {
        System.clearProperty(KEY_OPENMRS_APPLICATION_DATA_DIRECTORY);
    }

    @Override
    protected String getDockerComposeFile() {
        return "docker-compose-openmrs.yml";
    }

    @Override
    protected String getSourceDbServiceName() {
        return "mysql";
    }

    @Override
    protected int getSourceDbExposedPort() {
        return 3306;
    }

    @Override
    protected String getSourceDbProtocol() {
        return "mysql";
    }

    @Override
    protected String getSourceDbName() {
        return DB_NAME_OPENMRS;
    }

    @Override
    protected String getTableDefinitionsPath() {
        return getResourcePath("dsl/flattening/tables/openmrs");
    }

    @Override
    protected String getSourceDbUser() {
        return USER_OPENMRS_DB;
    }

    @Override
    protected String getSourceDbPassword() {
        return PASSWORD_OPENMRS_DB;
    }

    @Override
    protected String getAnalyticsLiquibaseFile() {
        return LIQUIBASE_ANALYTICS;
    }
}
