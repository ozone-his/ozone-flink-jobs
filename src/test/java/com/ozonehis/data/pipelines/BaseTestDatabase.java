package com.ozonehis.data.pipelines;

import java.util.stream.Stream;
import org.testcontainers.containers.JdbcDatabaseContainer;
import org.testcontainers.lifecycle.Startables;

public abstract class BaseTestDatabase {

    public static final String USER_ANALYTICS_DB = "test-analytics-user";

    public static final String PASSWORD_ANALYTICS_DB = "test-analytics-password";

    public static final String DB_NAME_ANALYTICS = "analytics";

    public void start(boolean wait, String dbName, String user, String password) {
        getDbContainer().withDatabaseName(dbName);
        getDbContainer().withUsername(user);
        getDbContainer().withPassword(password);
        if (wait) {
            Startables.deepStart(Stream.of(getDbContainer())).join();
        }
    }

    public String getJdbcUrl() {
        return getDbContainer().getJdbcUrl();
    }

    public void shutdown() {
        getDbContainer().stop();
    }

    public abstract JdbcDatabaseContainer<?> getDbContainer();
}
