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
import org.testcontainers.containers.JdbcDatabaseContainer;
import org.testcontainers.lifecycle.Startables;

public abstract class BaseTestDatabase {

    public static final String TEST_USER = "test-user";

    public static final String TEST_PASSWORD = "test-pass";

    public static final String TEST_DB_NAME = "test-db";

    public void start() {
        getDbContainer().withDatabaseName(TEST_DB_NAME);
        getDbContainer().withUsername(TEST_USER);
        getDbContainer().withPassword(TEST_PASSWORD);
        Startables.deepStart(Stream.of(getDbContainer())).join();
    }

    public String getJdbcUrl() {
        return getDbContainer().getJdbcUrl();
    }

    public void shutdown() {
        getDbContainer().stop();
    }

    public abstract JdbcDatabaseContainer<?> getDbContainer();
}
