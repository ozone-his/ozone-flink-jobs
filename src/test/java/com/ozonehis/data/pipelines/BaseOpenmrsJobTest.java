package com.ozonehis.data.pipelines;

import static org.openmrs.util.OpenmrsConstants.KEY_OPENMRS_APPLICATION_DATA_DIRECTORY;

import com.ozonehis.data.pipelines.batch.PatientBatchOpenmrsJobTest;
import liquibase.exception.LiquibaseException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;

public abstract class BaseOpenmrsJobTest extends BaseJobTest {

    public static final String USER_OPENMRS_DB = "test-user";

    public static final String PASSWORD_OPENMRS_DB = "test-password";

    public static final String DB_NAME_OPENMRS = "test-openmrs-db";

    private static final String ROOT = "org/openmrs/liquibase/";

    private static final String LIQUIBASE_SCHEMA_2_5 = ROOT + "snapshots/schema-only/liquibase-schema-only-2.5.x.xml";

    private static final String LIQUIBASE_UPDATE_2_6 = ROOT + "updates/liquibase-update-to-latest-2.6.x.xml";

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
    protected String getTableDefinitionsPath() {
        return PatientBatchOpenmrsJobTest.class
                .getClassLoader()
                .getResource("dsl/flattening/tables/openmrs")
                .getPath();
    }

    @Override
    protected BaseTestDatabase getSourceDb() {
        return new MySQLTestDatabase();
    }

    @Override
    protected String getSourceDbName() {
        return DB_NAME_OPENMRS;
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

    @Override
    protected void createSourceSchema() {
        try {
            updateDatabase(getLiquibase(LIQUIBASE_SCHEMA_2_5, getSourceDbConnection()));
            updateDatabase(getLiquibase(LIQUIBASE_UPDATE_2_6, getSourceDbConnection()));
        } catch (LiquibaseException e) {
            throw new RuntimeException(e);
        }
    }
}
