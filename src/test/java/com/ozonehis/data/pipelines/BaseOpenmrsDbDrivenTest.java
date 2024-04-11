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
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.testcontainers.lifecycle.Startables;

public abstract class BaseOpenmrsDbDrivenTest {

    private static final MySQLTestDatabase OPENMRS_DB = new MySQLTestDatabase();

    private static final PostgresTestDatabase ANALYTICS_DB = new PostgresTestDatabase();

    protected String getOpenmrsJdbcUrl() {
        return OPENMRS_DB.getJdbcUrl();
    }

    protected String getAnalyticsJdbcUrl() {
        return ANALYTICS_DB.getJdbcUrl();
    }

    @BeforeAll
    public static void beforeAll() {
        OPENMRS_DB.start(false);
        ANALYTICS_DB.start(false);
        Startables.deepStart(Stream.of(OPENMRS_DB.getDbContainer(), ANALYTICS_DB.getDbContainer()))
                .join();
    }

    @AfterAll
    public static void afterAll() {
        OPENMRS_DB.shutdown();
    }
}
