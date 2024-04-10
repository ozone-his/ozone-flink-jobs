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
import org.testcontainers.containers.MySQLContainer;

public class MySQLTestDatabase extends BaseTestDatabase {

    public static final MySQLContainer CONTAINER = new MySQLContainer<>("mysql:8.2.0");

    @Override
    public JdbcDatabaseContainer<?> getDbContainer() {
        return CONTAINER;
    }
}
