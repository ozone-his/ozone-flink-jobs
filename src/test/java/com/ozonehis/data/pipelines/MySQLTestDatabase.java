package com.ozonehis.data.pipelines;

import org.testcontainers.containers.JdbcDatabaseContainer;
import org.testcontainers.containers.MySQLContainer;

public class MySQLTestDatabase extends BaseTestDatabase {

    public static final MySQLContainer CONTAINER = new MySQLContainer<>("mysql:8.2.0");

    @Override
    public JdbcDatabaseContainer<?> getDbContainer() {
        return CONTAINER;
    }
}
