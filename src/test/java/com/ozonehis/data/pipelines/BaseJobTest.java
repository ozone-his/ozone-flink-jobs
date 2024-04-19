package com.ozonehis.data.pipelines;

import static com.ozonehis.data.pipelines.Constants.PROP_ANALYTICS_CONFIG_FILE_PATH;
import static com.ozonehis.data.pipelines.Constants.PROP_FLINK_REST_PORT;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.ozonehis.data.pipelines.batch.PatientBatchJobTest;
import com.ozonehis.data.pipelines.config.AppConfiguration;
import com.ozonehis.data.pipelines.config.FileSinkConfig;
import com.ozonehis.data.pipelines.config.JdbcCatalogConfig;
import com.ozonehis.data.pipelines.config.JdbcSinkConfig;
import com.ozonehis.data.pipelines.config.JdbcSourceConfig;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.LiquibaseException;
import liquibase.resource.ClassLoaderResourceAccessor;
import org.apache.commons.io.FileUtils;
import org.apache.flink.runtime.minicluster.MiniCluster;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.testcontainers.lifecycle.Startables;

public abstract class BaseJobTest {

    private static final String DELETE = "DELETE FROM ";

    private static final String DISABLE_KEYS = "SET FOREIGN_KEY_CHECKS=0";

    private static final String ENABLE_KEYS = "SET FOREIGN_KEY_CHECKS=1";

    private static ObjectMapper MAPPER = new ObjectMapper(new YAMLFactory());

    private static final String TEST_DIR = "flink-test-dir";

    private static final String EXPORT_DIR_NAME = "export";

    private static AppConfiguration config;

    private static BaseTestDatabase sourceDb;

    private static Connection sourceConnection;

    protected static PostgresTestDatabase analyticsDb;

    private static Connection analyticsConnection;

    protected static String testDir;

    protected static String exportDir;

    private static MiniCluster cluster;

    @BeforeAll
    public static void beforeAllSuper() {
        try {
            testDir = Files.createTempDirectory(TEST_DIR).toFile().getAbsolutePath();
            exportDir = testDir + "/" + EXPORT_DIR_NAME;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @AfterAll
    public static void afterAllSuper() throws IOException {
        if (sourceConnection != null) {
            try {
                sourceConnection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        if (sourceDb != null) {
            sourceDb.shutdown();
            sourceDb = null;
        }

        System.clearProperty(PROP_ANALYTICS_CONFIG_FILE_PATH);
        System.clearProperty(PROP_FLINK_REST_PORT);
        analyticsDb.shutdown();
        FileUtils.forceDelete(new File(testDir));
    }

    @BeforeEach
    public void beforeSuper() throws IOException {
        if (analyticsDb == null) {
            analyticsDb = new PostgresTestDatabase();
            analyticsDb.start(
                    false,
                    BaseTestDatabase.DB_NAME_ANALYTICS,
                    BaseTestDatabase.USER_ANALYTICS_DB,
                    BaseTestDatabase.PASSWORD_ANALYTICS_DB);
            sourceDb = getSourceDb();
            sourceDb.start(false, getSourceDbName(), getSourceDbUser(), getSourceDbPassword());
            Startables.deepStart(Stream.of(sourceDb.getDbContainer(), analyticsDb.getDbContainer()))
                    .join();
            createAnalyticsSchema();
            createSourceSchema();
            setupConfig();
        }
    }

    @AfterEach
    public void setup() throws Exception {
        clearAnalyticsDb();
        if (cluster != null) {
            cluster.close();
        }
    }

    protected void initJobAndStartCluster(BaseJob job) throws Exception {
        job.initConfig();
        cluster = job.startCluster();
    }

    private void setupConfig() throws IOException {
        final String catalogName = "analytics";
        config = new AppConfiguration();
        JdbcCatalogConfig catalog = new JdbcCatalogConfig();
        catalog.setName(catalogName);
        catalog.setDefaultDatabase(BaseTestDatabase.DB_NAME_ANALYTICS);
        catalog.setBaseUrl(
                analyticsDb.getJdbcUrl().substring(0, analyticsDb.getJdbcUrl().lastIndexOf("/")));
        catalog.setUsername(BaseTestDatabase.USER_ANALYTICS_DB);
        catalog.setPassword(BaseTestDatabase.PASSWORD_ANALYTICS_DB);
        config.setJdbcCatalogs(List.of(catalog));
        JdbcSourceConfig source = new JdbcSourceConfig();
        source.setDatabaseUrl(sourceDb.getJdbcUrl());
        source.setUsername(getSourceDbUser());
        source.setPassword(getSourceDbPassword());
        source.setTableDefinitionsPath(getTableDefinitionsPath());
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
        fileSinkCfg.setExportOutputPath(exportDir);
        config.setFileSinks(List.of(fileSinkCfg));
        final String configFile = testDir + "/config.yaml";
        MAPPER.writeValue(new FileOutputStream(configFile), config);
        System.setProperty(PROP_ANALYTICS_CONFIG_FILE_PATH, configFile);
        System.setProperty(PROP_FLINK_REST_PORT, TestUtils.getAvailablePort().toString());
    }

    protected Connection getSourceDbConnection() {
        if (sourceConnection == null) {
            try {
                sourceConnection = DriverManager.getConnection(
                        sourceDb.getJdbcUrl(),
                        sourceDb.getDbContainer().getUsername(),
                        sourceDb.getDbContainer().getPassword());
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }

        return sourceConnection;
    }

    protected Connection getAnalyticsDbConnection() {
        if (analyticsConnection == null) {
            try {
                analyticsConnection = DriverManager.getConnection(
                        analyticsDb.getJdbcUrl(),
                        analyticsDb.getDbContainer().getUsername(),
                        analyticsDb.getDbContainer().getPassword());
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }

        return analyticsConnection;
    }

    private void createAnalyticsSchema() {
        try {
            updateDatabase(getLiquibase(getAnalyticsLiquibaseFile(), getAnalyticsDbConnection()));
        } catch (LiquibaseException e) {
            throw new RuntimeException(e);
        }
    }

    protected Liquibase getLiquibase(String file, Connection connection) throws LiquibaseException {
        Database db = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(connection));
        return new Liquibase(file, new ClassLoaderResourceAccessor(BaseJobTest.class.getClassLoader()), db);
    }

    protected void updateDatabase(Liquibase liquibase) throws LiquibaseException {
        liquibase.update((String) null);
        liquibase.getDatabase().getConnection().commit();
    }

    protected void addTestDataToSourceDb(String file) {
        TestUtils.executeScript(file, getSourceDbConnection());
    }

    protected void addTestDataToAnalyticsDb(String file) {
        TestUtils.executeScript("analytics/" + file, getAnalyticsDbConnection());
    }

    protected void clearAnalyticsDb() throws SQLException {
        deleteAllData(getAnalyticsDbConnection(), false);
    }

    protected abstract BaseTestDatabase getSourceDb();

    protected abstract String getSourceDbName();

    protected abstract String getSourceDbUser();

    protected abstract String getSourceDbPassword();

    protected abstract void createSourceSchema();

    protected abstract String getTableDefinitionsPath();

    protected abstract String getAnalyticsLiquibaseFile();

    private void deleteAllData(Connection connection, boolean disableKeys) throws SQLException {
        List<String> tables = getTableNames(connection);
        Statement statement = connection.createStatement();
        try {
            if (disableKeys) {
                statement.execute(DISABLE_KEYS);
            }
            for (String tableName : tables) {
                statement.executeUpdate(DELETE + tableName);
            }
        } finally {
            if (statement != null) {
                if (disableKeys) {
                    statement.execute(ENABLE_KEYS);
                }
                statement.close();
            }
        }
    }

    private List<String> getTableNames(Connection connection) throws SQLException {
        DatabaseMetaData dbmd = connection.getMetaData();
        ResultSet tables = dbmd.getTables(null, null, null, new String[] {"TABLE"});
        List<String> tableNames = new ArrayList();
        while (tables.next()) {
            tableNames.add(tables.getString("TABLE_NAME"));
        }
        return tableNames;
    }
}
