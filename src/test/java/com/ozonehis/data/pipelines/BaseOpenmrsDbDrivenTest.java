/*
 * Copyright (C) Amiyul LLC - All Rights Reserved
 *
 * This source code is protected under international copyright law. All rights
 * reserved and protected by the copyright holder. However, the generated
 * bytecode from this source code is free for use.
 *
 * This file is confidential and only available to authorized individuals with the
 * permission of the copyright holder. If you encounter this file and do not have
 * permission, please contact the copyright holder and delete this file.
 */
package com.ozonehis.data.pipelines;

import static org.openmrs.util.OpenmrsConstants.KEY_OPENMRS_APPLICATION_DATA_DIRECTORY;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import java.io.IOException;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.stream.Stream;
import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.LiquibaseException;
import liquibase.resource.ClassLoaderResourceAccessor;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.testcontainers.lifecycle.Startables;

public abstract class BaseOpenmrsDbDrivenTest {

    private static final String ROOT = "org/openmrs/liquibase/";

    private static final String LIQUIBASE_SCHEMA_2_5 = ROOT + "snapshots/schema-only/liquibase-schema-only-2.5.x.xml";

    private static final String LIQUIBASE_UPDATE_2_6 = ROOT + "updates/liquibase-update-to-latest-2.6.x.xml";

    private static final String TEST_DIR = "flink-test-dir";

    private static final MySQLTestDatabase OPENMRS_DB = new MySQLTestDatabase();

    protected static final PostgresTestDatabase ANALYTICS_DB = new PostgresTestDatabase();

    private static Connection openmrsConnection;

    private static Connection analyticsConnection;

    protected static String testDir;

    protected static ObjectMapper MAPPER = new ObjectMapper(new YAMLFactory());

    private static Connection connectToOpenmrsDbConn() {
        if (openmrsConnection == null) {
            try {
                openmrsConnection =
                        DriverManager.getConnection(OPENMRS_DB.getJdbcUrl(), OPENMRS_DB.USER, OPENMRS_DB.PASSWORD);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }

        return openmrsConnection;
    }

    protected Connection getOpenmrsDbConnection() {
        return connectToOpenmrsDbConn();
    }

    protected Connection getAnalyticsDbConnection() {
        if (analyticsConnection == null) {
            try {
                analyticsConnection = DriverManager.getConnection(
                        ANALYTICS_DB.getJdbcUrl(), ANALYTICS_DB.USER, ANALYTICS_DB.PASSWORD);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }

        return analyticsConnection;
    }

    @BeforeAll
    public static void beforeAll() {
        try {
            testDir = Files.createTempDirectory(TEST_DIR).toFile().getAbsolutePath();
            System.setProperty(KEY_OPENMRS_APPLICATION_DATA_DIRECTORY, testDir);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        OPENMRS_DB.start(false);
        ANALYTICS_DB.start(false);
        Startables.deepStart(Stream.of(OPENMRS_DB.getDbContainer(), ANALYTICS_DB.getDbContainer()))
                .join();
        createOpenmrsSchema();
    }

    @AfterAll
    public static void afterAll() {
        if (openmrsConnection != null) {
            try {
                openmrsConnection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        System.clearProperty(KEY_OPENMRS_APPLICATION_DATA_DIRECTORY);
        OPENMRS_DB.shutdown();
        ANALYTICS_DB.shutdown();
    }

    private static void createOpenmrsSchema() {
        try {
            updateDatabase(getLiquibase(LIQUIBASE_SCHEMA_2_5, connectToOpenmrsDbConn()));
            updateDatabase(getLiquibase(LIQUIBASE_UPDATE_2_6, connectToOpenmrsDbConn()));
        } catch (LiquibaseException e) {
            throw new RuntimeException(e);
        }
    }

    private static Liquibase getLiquibase(String file, Connection connection) throws LiquibaseException {
        Database db = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(connection));
        return new Liquibase(file, new ClassLoaderResourceAccessor(BaseOpenmrsDbDrivenTest.class.getClassLoader()), db);
    }

    private static void updateDatabase(Liquibase liquibase) throws LiquibaseException {
        liquibase.update((String) null);
        liquibase.getDatabase().getConnection().commit();
    }

    protected void addOpenmrsTestData(String file) {
        TestUtils.executeScript("openmrs/" + file, connectToOpenmrsDbConn());
    }
}
