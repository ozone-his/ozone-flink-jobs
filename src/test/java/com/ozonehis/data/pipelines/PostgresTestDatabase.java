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

import org.testcontainers.containers.JdbcDatabaseContainer;
import org.testcontainers.containers.PostgreSQLContainer;

public class PostgresTestDatabase extends BaseTestDatabase {

    public static final PostgreSQLContainer CONTAINER = new PostgreSQLContainer<>("postgres:15-alpine");

    @Override
    public JdbcDatabaseContainer<?> getDbContainer() {
        return CONTAINER;
    }
}
