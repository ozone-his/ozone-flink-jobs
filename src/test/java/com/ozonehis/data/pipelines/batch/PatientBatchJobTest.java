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

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.ozonehis.data.pipelines.BaseOpenmrsJobTest;
import com.ozonehis.data.pipelines.TestUtils;
import org.junit.jupiter.api.Test;

public class PatientBatchJobTest extends BaseOpenmrsJobTest {

    private static final String TABLE = "patient";

    private BatchJob job = new BatchJob();

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
