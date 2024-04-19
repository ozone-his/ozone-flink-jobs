package com.ozonehis.data.pipelines;

import org.testcontainers.containers.JdbcDatabaseContainer;
import org.testcontainers.containers.PostgreSQLContainer;

public class PostgresTestDatabase extends BaseTestDatabase {

    public final PostgreSQLContainer CONTAINER = new PostgreSQLContainer<>("postgres:13-alpine");

    @Override
    public JdbcDatabaseContainer<?> getDbContainer() {
        return CONTAINER;
    }
}
