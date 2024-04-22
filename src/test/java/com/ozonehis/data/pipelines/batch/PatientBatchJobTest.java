package com.ozonehis.data.pipelines.batch;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.ozonehis.data.pipelines.BaseOpenmrsJobTest;
import com.ozonehis.data.pipelines.Patient;
import com.ozonehis.data.pipelines.TestUtils;
import com.ozonehis.data.pipelines.export.ExportJob;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

public class PatientBatchJobTest extends BaseOpenmrsJobTest {

    @Test
    public void execute_shouldLoadAllPatientsFromOpenmrsDbToAnalyticsDb() throws Exception {
        addTestDataToSourceDb("openmrs/initial.sql");
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

    @Test
    public void execute_shouldExportAllPatientsFromAnalyticsDbToAFile() throws Exception {
        addTestDataToAnalyticsDb("patient.sql");
        final int expectedCount = 2;
        assertEquals(
                expectedCount,
                TestUtils.getRows("patients", getAnalyticsDbConnection()).size());
        ExportJob job = new ExportJob();
        initJobAndStartCluster(job);

        job.execute();
        // TODO Wait for job to complete, possibly use a JobListener
        Thread.sleep(5000);

        final String outputDir = exportDir + "/patients/h1";
        final JsonMapper mapper = new JsonMapper();
        List<Patient> patients = new ArrayList<>();
        Path outputPath = Files.list(
                        Files.list(Paths.get(outputDir)).findFirst().get())
                .findFirst()
                .get();
        try (MappingIterator<Patient> it = mapper.readerFor(Patient.class).readValues(Files.readString(outputPath))) {
            while (it.hasNextValue()) {
                patients.add(it.nextValue());
            }
        }

        assertEquals(expectedCount, patients.size());
        Patient patient = patients.get(0);
        assertEquals(1, patient.getPatientId());
        assertEquals("M", patient.getGender());
        assertEquals("2000-08-01", patient.getBirthdate());
        assertFalse(patient.isBirthdateEstimated());
        assertFalse(patient.isDead());
        assertNull(patient.getDeathDate());
        assertEquals("patient-uuid-1", patient.getPatientUuid());
        patient = patients.get(1);
        assertEquals(2, patient.getPatientId());
        assertEquals("F", patient.getGender());
        assertEquals("2001-05-31", patient.getBirthdate());
        assertTrue(patient.isBirthdateEstimated());
        assertTrue(patient.isDead());
        assertEquals("2022-05-18 00:00:00", patient.getDeathDate());
        assertEquals("patient-uuid-2", patient.getPatientUuid());
    }
}
