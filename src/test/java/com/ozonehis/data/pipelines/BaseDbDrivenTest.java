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

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;

public abstract class BaseDbDrivenTest {

    private static final TestDatabase TEST_DB = new TestDatabase();

    protected String getJdbcUrl() {
        return TEST_DB.getJdbcUrl();
    }

    @BeforeAll
    public static void beforeAll() {
        TEST_DB.start();
    }

    @AfterAll
    public static void afterAll() {
        TEST_DB.shutdown();
    }
}
