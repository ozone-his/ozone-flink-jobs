/*
 * Copyright (C) Amiyul LLC - All Rights Reserved
 *
 * This source code is protected under international copyright law. All rights
 * reserved and protected by the copyright holder.
 *
 * This file is confidential and only available to authorized individuals with the
 * permission of the copyright holder. If you encounter this file and do not have
 * permission, please contact the copyright holder and delete this file.
 */
package com.ozonehis.data.pipelines.batch;

import static com.ozonehis.data.pipelines.Constants.PROP_ANALYTICS_CONFIG_FILE_PATH;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.ozonehis.data.pipelines.BaseOpenmrsJobTest;
import com.ozonehis.data.pipelines.BaseTestDatabase;
import com.ozonehis.data.pipelines.TestUtils;
import com.ozonehis.data.pipelines.config.AppConfiguration;
import com.ozonehis.data.pipelines.config.JdbcCatalogConfig;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class PatientBatchJobTest extends BaseOpenmrsJobTest {

    private static final String TABLE = "patient";

    private BatchJob job = new BatchJob();

    private static AppConfiguration config;

    @BeforeAll
    public static void setupClass() throws IOException {
        config = new AppConfiguration();
        JdbcCatalogConfig catalog = new JdbcCatalogConfig();
        catalog.setName("analytics");
        catalog.setDefaultDatabase("analytics");
        catalog.setBaseUrl(ANALYTICS_DB.getJdbcUrl());
        catalog.setUsername(BaseTestDatabase.USER);
        catalog.setPassword(BaseTestDatabase.PASSWORD);
        config.setJdbcCatalogs(List.of(catalog));
        final String configFile = testDir + "/config.yaml";
        MAPPER.writeValue(new FileOutputStream(configFile), config);
        System.setProperty(PROP_ANALYTICS_CONFIG_FILE_PATH, configFile);
    }

    @AfterAll
    public static void tearDownClass() {
        System.clearProperty(PROP_ANALYTICS_CONFIG_FILE_PATH);
    }

    @Test
    public void execute_shouldLoadAllPatientsFromOpenmrsToAnalyticsDb() {
        addOpenmrsTestData("initial.sql");
        addOpenmrsTestData("patient.sql");
        final int count = TestUtils.getRows(TABLE, getOpenmrsDbConnection()).size();
        // TODO Check table does not exist

        job.execute();

        assertEquals(count, TestUtils.getRows(TABLE, getAnalyticsDbConnection()).size());
    }
}
