package com.ozonehis.data.pipelines.config;

public class JdbcSinkConfig {

    private String jdbcCatalog;

    public String getJdbcCatalog() {
        return jdbcCatalog;
    }

    public void setJdbcCatalog(String jdbcCatalog) {
        this.jdbcCatalog = jdbcCatalog;
    }

    private String databaseName;

    public String getDatabaseName() {
        return databaseName;
    }

    public void setDatabaseName(String databaseName) {
        this.databaseName = databaseName;
    }

    private String queryPath;

    public String getQueryPath() {
        return queryPath;
    }

    public void setQueryPath(String queryPath) {
        this.queryPath = queryPath;
    }
}
