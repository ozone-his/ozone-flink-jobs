package com.ozonehis.analytics.runtime;

import com.ozonehis.analytics.config.AnalyticsConfig;
import org.apache.flink.connector.jdbc.core.database.catalog.JdbcCatalog;
import org.apache.flink.connector.jdbc.mysql.database.catalog.MySqlCatalog;
import org.apache.flink.connector.jdbc.postgres.database.catalog.PostgresCatalog;

/**
 * Builds Flink catalogs from configuration.
 *
 * <p>Before JDBC connector 4.x a single {@code JdbcCatalog} class resolved the dialect from the
 * URL. The connector is now split per database and {@code JdbcCatalog} is an interface, so the
 * implementation is selected here from the configured dialect.
 */
final class Catalogs {

    private Catalogs() {}

    static JdbcCatalog create(AnalyticsConfig.Catalog catalog, ClassLoader classLoader) {
        return switch (catalog.dialect()) {
            case POSTGRESQL -> new PostgresCatalog(
                    classLoader,
                    catalog.name(),
                    catalog.defaultDatabase(),
                    catalog.username(),
                    catalog.password(),
                    catalog.baseUrl());
            case MYSQL -> new MySqlCatalog(
                    classLoader,
                    catalog.name(),
                    catalog.defaultDatabase(),
                    catalog.username(),
                    catalog.password(),
                    catalog.baseUrl());
        };
    }
}
