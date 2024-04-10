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

import java.util.stream.Stream;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.lifecycle.Startables;

public class TestDatabase {

    public static final String TEST_USER = "mysql-user";

    public static final String TEST_PASSWORD = "mysql-pass";

    public static final String TEST_DB_NAME = "mysql-flink";

    public static final String ENTRY_POINT_PATH = "/docker-entrypoint-initdb.d/";

    public static final MySQLContainer MYSQL_CONTAINER = new MySQLContainer<>("mysql:8.2.0");

    public String getJdbcUrl() {
        return MYSQL_CONTAINER.getJdbcUrl();
    }

    public void start() {
        MYSQL_CONTAINER.withDatabaseName(TEST_DB_NAME);
        MYSQL_CONTAINER.withUsername(TEST_USER);
        MYSQL_CONTAINER.withPassword(TEST_PASSWORD);
        // MYSQL_CONTAINER.withCopyFileToContainer(forClasspathResource("data.sql"), ENTRY_POINT_PATH + "/data.sql");

        Startables.deepStart(Stream.of(MYSQL_CONTAINER)).join();
    }

    public void shutdown() {
        MYSQL_CONTAINER.stop();
    }
}
