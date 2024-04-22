package com.ozonehis.data.pipelines;

import liquibase.exception.LiquibaseException;

public abstract class BaseOdooJobTest extends BaseJobTest {

    public static final String USER_ODOO_DB = "test-odoo-user";

    public static final String PASSWORD_ODOO_DB = "test-odoo-password";

    public static final String DB_NAME_ODOO = "test-odoo-db";

    private static final String LIQUIBASE_ANALYTICS = "liquibase/analytics/changelogs/0002-sales_order_tbl.xml";

    @Override
    protected String getTableDefinitionsPath() {
        return getResourcePath("dsl/flattening/tables/odoo");
    }

    @Override
    protected BaseTestDatabase getSourceDb() {
        return new PostgresTestDatabase();
    }

    @Override
    protected String getSourceDbName() {
        return DB_NAME_ODOO;
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

    @Override
    protected void createSourceSchema() {
        try {
            updateDatabase(getLiquibase("liquibase-odoo-schema.xml", getSourceDbConnection()));
        } catch (LiquibaseException e) {
            throw new RuntimeException(e);
        }
    }
}
