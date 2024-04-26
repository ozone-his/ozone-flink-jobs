package com.ozonehis.data.pipelines;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.ozonehis.data.pipelines.batch.BatchJob;
import org.junit.jupiter.api.Test;

public class PatientJobTest extends BaseOpenmrsJobTest {

    @Override
    protected String getTestFilename() {
        return "patients";
    }

    @Test
    public void execute_shouldLoadAllPatientsFromOpenmrsDbToAnalyticsDb() throws Exception {
        addTestDataToSourceDb("openmrs/patient.sql");
        final int expectedCount = 2;
        final int count = TestUtils.getRows("patient", getSourceDbConnection()).size();
        assertEquals(expectedCount, count);
        BatchJob job = new BatchJob();
        initJobAndStartCluster(job);

        job.execute();
        // TODO Wait for job to complete, possibly use a JobListener
        Thread.sleep(5000);

        // TODO check each row data
        assertEquals(
                count, TestUtils.getRows("patients", getAnalyticsDbConnection()).size());
    }
}
