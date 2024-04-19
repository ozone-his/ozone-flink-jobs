package com.ozonehis.data.pipelines.batch;

import static com.ozonehis.data.pipelines.Constants.PROP_ANALYTICS_CONFIG_FILE_PATH;
import static com.ozonehis.data.pipelines.Constants.PROP_FLINK_REST_PORT;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.ozonehis.data.pipelines.BaseJob;
import com.ozonehis.data.pipelines.BaseOpenmrsJobTest;
import com.ozonehis.data.pipelines.BaseTestDatabase;
import com.ozonehis.data.pipelines.Patient;
import com.ozonehis.data.pipelines.TestUtils;
import com.ozonehis.data.pipelines.config.AppConfiguration;
import com.ozonehis.data.pipelines.config.FileSinkConfig;
import com.ozonehis.data.pipelines.config.JdbcCatalogConfig;
import com.ozonehis.data.pipelines.config.JdbcSinkConfig;
import com.ozonehis.data.pipelines.config.JdbcSourceConfig;
import com.ozonehis.data.pipelines.export.ExportJob;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import org.apache.flink.runtime.minicluster.MiniCluster;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class PatientBatchJobTest extends BaseOpenmrsJobTest {

    private static AppConfiguration config;

    private static final String EXPORT_DIR = "export";

    private static MiniCluster cluster;

    @BeforeAll
    public static void setupClass() throws IOException {
        setupConfig();
    }

    private static void setupConfig() throws IOException {
        final String catalogName = "analytics";
        config = new AppConfiguration();
        JdbcCatalogConfig catalog = new JdbcCatalogConfig();
        catalog.setName(catalogName);
        catalog.setDefaultDatabase(BaseTestDatabase.DB_NAME_ANALYTICS);
        catalog.setBaseUrl(
                ANALYTICS_DB.getJdbcUrl().substring(0, ANALYTICS_DB.getJdbcUrl().lastIndexOf("/")));
        catalog.setUsername(BaseTestDatabase.USER_ANALYTICS_DB);
        catalog.setPassword(BaseTestDatabase.PASSWORD_ANALYTICS_DB);
        config.setJdbcCatalogs(List.of(catalog));
        JdbcSourceConfig source = new JdbcSourceConfig();
        source.setDatabaseUrl(OPENMRS_DB.getJdbcUrl());
        source.setUsername(BaseTestDatabase.USER_OPENMRS_DB);
        source.setPassword(BaseTestDatabase.PASSWORD_OPENMRS_DB);
        source.setTableDefinitionsPath(PatientBatchJobTest.class
                .getClassLoader()
                .getResource("dsl/flattening/tables/openmrs")
                .getPath());
        config.setJdbcSources(List.of(source));
        JdbcSinkConfig jdbcSinkCfg = new JdbcSinkConfig();
        jdbcSinkCfg.setJdbcCatalog(catalogName);
        jdbcSinkCfg.setDatabaseName(BaseTestDatabase.DB_NAME_ANALYTICS);
        jdbcSinkCfg.setQueryPath(PatientBatchJobTest.class
                .getClassLoader()
                .getResource("dsl/flattening/queries")
                .getPath());
        config.setJdbcSinks(List.of(jdbcSinkCfg));
        FileSinkConfig fileSinkCfg = new FileSinkConfig();
        fileSinkCfg.setDestinationTableDefinitionsPath(PatientBatchJobTest.class
                .getClassLoader()
                .getResource("dsl/export/tables")
                .getPath());
        fileSinkCfg.setFormat("json");
        fileSinkCfg.setExportOutPutTag("h1");
        fileSinkCfg.setQueryPath(PatientBatchJobTest.class
                .getClassLoader()
                .getResource("dsl/export/queries")
                .getPath());
        fileSinkCfg.setExportOutputPath(testDir + "/" + EXPORT_DIR);
        config.setFileSinks(List.of(fileSinkCfg));
        final String configFile = testDir + "/config.yaml";
        MAPPER.writeValue(new FileOutputStream(configFile), config);
        System.setProperty(PROP_ANALYTICS_CONFIG_FILE_PATH, configFile);
        System.setProperty(PROP_FLINK_REST_PORT, TestUtils.getAvailablePort().toString());
    }

    @AfterAll
    public static void tearDownClass() {
        System.clearProperty(PROP_ANALYTICS_CONFIG_FILE_PATH);
        System.clearProperty(PROP_FLINK_REST_PORT);
    }

    @AfterEach
    public void setup() throws Exception {
        clearAnalyticsDb();
        if (cluster != null) {
            cluster.close();
        }
    }

    private void prepareJob(BaseJob job) throws Exception {
        job.initConfig();
        cluster = job.startCluster();
    }

    @Test
    public void execute_shouldLoadAllPatientsFromOpenmrsDbToAnalyticsDb() throws Exception {
        addOpenmrsTestData("initial.sql");
        addOpenmrsTestData("patient.sql");
        final int count = TestUtils.getRows("patient", getOpenmrsDbConnection()).size();
        BatchJob job = new BatchJob();
        prepareJob(job);

        job.execute();
        // TODO Wait for job to complete, possibly use a JobListener
        Thread.sleep(5000);

        // TODO check each row data
        assertEquals(
                count, TestUtils.getRows("patients", getAnalyticsDbConnection()).size());
    }

    @Test
    public void execute_shouldExportAllPatientsFromAnalyticsDbToAFile() throws Exception {
        addAnalyticsTestData("patient.sql");
        final int expectedCount = 2;
        assertEquals(
                expectedCount,
                TestUtils.getRows("patients", getAnalyticsDbConnection()).size());
        ExportJob job = new ExportJob();
        prepareJob(job);

        job.execute();
        // TODO Wait for job to complete, possibly use a JobListener
        Thread.sleep(5000);

        final String outputDir = testDir + "/" + EXPORT_DIR + "/patients/h1";
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
