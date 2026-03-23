package com.ozonehis.data.pipelines;

public abstract class BaseOdooJobTest extends BaseJobTest {

    private static final String LIQUIBASE_ANALYTICS = "liquibase/analytics/changelogs/0002-sales_order_tbl.xml";

    /**
     * Creates the minimal Odoo source-DB schema needed for tests.
     * This avoids having to start a full Odoo application just to get the DDL tables.
     */
    @Override
    protected void initSourceSchema() {
        TestUtils.executeScript("odoo/create_source_schema.sql", getSourceDbConnection());
    }

    @Override
    protected String getSourceSystemName() {
        return "odoo";
    }

    @Override
    protected String getDockerComposeFile() {
        return "docker-compose-odoo.yml";
    }

    @Override
    protected String getSourceDbServiceName() {
        return "postgresql";
    }

    @Override
    protected int getSourceDbExposedPort() {
        return 5432;
    }

    @Override
    protected String getSourceDbProtocol() {
        return "postgresql";
    }

    @Override
    protected String getSourceDbName() {
        return DB_NAME_ODOO;
    }

    @Override
    protected String getTableDefinitionsPath() {
        return getResourcePath("dsl/flattening/tables/odoo");
    }

    @Override
    protected String getSourceDbUser() {
        return USER_ODOO_DB;
    }

    @Override
    protected String getSourceDbPassword() {
        return PASSWORD_ODOO_DB;
    }

    @Override
    protected String getAnalyticsLiquibaseFile() {
        return LIQUIBASE_ANALYTICS;
    }
}
