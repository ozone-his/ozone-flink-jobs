package com.ozonehis.data.pipelines;

import static com.ozonehis.data.pipelines.Constants.PROP_ANALYTICS_CONFIG_FILE_PATH;
import static com.ozonehis.data.pipelines.Constants.PROP_FLINK_REST_PORT;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.ozonehis.data.pipelines.config.AppConfiguration;
import com.ozonehis.data.pipelines.config.FileSinkConfig;
import com.ozonehis.data.pipelines.config.JdbcCatalogConfig;
import com.ozonehis.data.pipelines.config.JdbcSinkConfig;
import com.ozonehis.data.pipelines.config.JdbcSourceConfig;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import org.testcontainers.containers.ComposeContainer;
import org.testcontainers.containers.ContainerState;
import org.testcontainers.containers.wait.strategy.Wait;

public abstract class BaseJobTest {

    public static final String USER_OPENMRS_DB = "openmrs";

    public static final String PASSWORD_OPENMRS_DB = "password";

    public static final String DB_NAME_OPENMRS = "openmrs";

    public static final String USER_ODOO_DB = "odoo";

    public static final String PASSWORD_ODOO_DB = "password";

    public static final String DB_NAME_ODOO = "odoo";

    public static final String USER_ANALYTICS_DB = "test-analytics-user";

    public static final String PASSWORD_ANALYTICS_DB = "test-analytics-password";

    public static final String DB_NAME_ANALYTICS = "analytics";

    private static final String DELETE = "DELETE FROM ";

    private static final String DISABLE_KEYS = "SET FOREIGN_KEY_CHECKS=0";

    private static final String ENABLE_KEYS = "SET FOREIGN_KEY_CHECKS=1";

    private static ObjectMapper MAPPER = new ObjectMapper(new YAMLFactory());

    private static final String TEST_DIR = "flink-test-dir";

    private static final String EXPORT_DIR_NAME = "export";

    private static AppConfiguration config;

    private static ContainerState sourceDb;

    private static Connection sourceConnection;

    protected static ContainerState analyticsDb;

    private static Connection analyticsConnection;

    protected static String testDir;

    protected static String exportDir;

    protected static ComposeContainer composeContainer;

    private boolean initialized;

    private MiniCluster cluster;

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
            } finally {
                sourceConnection = null;
            }
        }

        if (analyticsConnection != null) {
            try {
                analyticsConnection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                analyticsConnection = null;
            }
        }

        composeContainer.stop();

        System.clearProperty(PROP_ANALYTICS_CONFIG_FILE_PATH);
        System.clearProperty(PROP_FLINK_REST_PORT);
        FileUtils.forceDelete(new File(testDir));
    }

    @BeforeEach
    public void beforeSuper() throws Exception {
        if (!initialized) {
            Map<String, String> envs = new HashMap<>();
            envs.put("SQL_SCRIPTS_PATH", getResourcePath("distro/data"));
            envs.put("DISTRO_PATH", getResourcePath("distro"));

            envs.put("POSTGRES_PASSWORD", "password");
            envs.put("POSTGRES_USER", "postgres");
            envs.put("ANALYTICS_DB_NAME", "analytics");
            envs.put("ANALYTICS_DB_USER", "analytics");
            envs.put("ANALYTICS_DB_PASSWORD", "password");

            envs.put("MYSQL_ROOT_PASSWORD", PASSWORD_OPENMRS_DB);
            envs.put("OPENMRS_DB_NAME", DB_NAME_OPENMRS);
            envs.put("OPENMRS_DB_USER", USER_OPENMRS_DB);
            envs.put("OPENMRS_DB_PASSWORD", "password");
            envs.put("OPENMRS_CONFIG_PATH", getResourcePath("distro/configs/openmrs/initializer_config"));
            envs.put("OPENMRS_PROPERTIES_PATH", getResourcePath("distro/configs/openmrs/properties"));
            envs.put("OPENMRS_FRONTEND_BINARY_PATH", getResourcePath("distro/binaries/openmrs/frontend"));
            envs.put("OPENMRS_FRONTEND_CONFIG_PATH", getResourcePath("distro/configs/openmrs/frontend_config"));

            envs.put("SUPERSET_CONFIG_PATH", getResourcePath("distro/configs/superset"));
            envs.put("SUPERSET_DB", "superset");
            envs.put("SUPERSET_DB_USER", "superset");
            envs.put("SUPERSET_DB_PASSWORD", "superset");
            composeContainer = new ComposeContainer(
                            new File(getResourcePath("run/docker/docker-compose-common.yml")),
                            new File(getResourcePath("run/docker/docker-compose-superset.yml")),
                            new File(getResourcePath("run/docker/" + getDockerComposeFile())))
                    .withTailChildContainers(true)
                    .withEnv(envs)
                    .withServices("env-substitution", "postgresql", "openmrs", getSourceDbServiceName())
                    .withExposedService("postgresql", 5432, Wait.forListeningPort())
                    .withExposedService(getSourceDbServiceName(), getSourceDbExposedPort(), Wait.forListeningPort())
                    .withStartupTimeout(Duration.of(60, ChronoUnit.SECONDS));
            composeContainer.start();
            analyticsDb =
                    composeContainer.getContainerByServiceName("postgresql").get();
            sourceDb = composeContainer
                    .getContainerByServiceName(getSourceDbServiceName())
                    .get();
            setupConfig();
            initialized = true;
        }
    }

    @AfterEach
    public void setup() throws Exception {
        clearAnalyticsDb();
        if (cluster != null) {
            try {
                cluster.close();
            } finally {
                cluster = null;
            }
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
        catalog.setDefaultDatabase(DB_NAME_ANALYTICS);
        catalog.setBaseUrl("jdbc:postgresql://localhost:" + analyticsDb.getMappedPort(5432));
        catalog.setUsername(USER_ANALYTICS_DB);
        catalog.setPassword(PASSWORD_ANALYTICS_DB);
        config.setJdbcCatalogs(List.of(catalog));
        JdbcSourceConfig source = new JdbcSourceConfig();
        source.setDatabaseUrl("jdbc:mysql://localhost:" + sourceDb.getMappedPort(3306));
        source.setUsername(getSourceDbUser());
        source.setPassword(getSourceDbPassword());
        source.setTableDefinitionsPath(getTableDefinitionsPath());
        config.setJdbcSources(List.of(source));
        JdbcSinkConfig jdbcSinkCfg = new JdbcSinkConfig();
        jdbcSinkCfg.setJdbcCatalog(catalogName);
        jdbcSinkCfg.setDatabaseName(DB_NAME_ANALYTICS);
        final String flattenQueryPath = testDir + "dsl/flattening/queries";
        Files.createDirectories(Paths.get(flattenQueryPath));
        addTestFile(getTestFilename() + ".sql", getResourcePath("dsl/flattening/queries"), flattenQueryPath);
        jdbcSinkCfg.setQueryPath(flattenQueryPath);
        config.setJdbcSinks(List.of(jdbcSinkCfg));
        FileSinkConfig fileSinkCfg = new FileSinkConfig();
        final String exportQueryPath = testDir + "dsl/export/queries";
        Files.createDirectories(Paths.get(exportQueryPath));
        addTestFile(getTestFilename() + ".sql", getResourcePath("dsl/export/queries"), exportQueryPath);
        fileSinkCfg.setQueryPath(exportQueryPath);
        final String exportTablePath = testDir + "dsl/export/tables";
        Files.createDirectories(Paths.get(exportTablePath));
        addTestFile(getTestFilename() + ".sql", getResourcePath("dsl/export/tables"), exportTablePath);
        fileSinkCfg.setDestinationTableDefinitionsPath(exportTablePath);
        fileSinkCfg.setFormat("json");
        fileSinkCfg.setExportOutPutTag("h1");
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
                        "jdbc:" + getSourceDbProtocol() + "://localhost:"
                                + sourceDb.getMappedPort(getSourceDbExposedPort()) + "/" + getSourceDbName(),
                        getSourceDbUser(),
                        getSourceDbPassword());
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
                        "jdbc:postgresql://localhost:" + analyticsDb.getMappedPort(5432) + "/analytics",
                        "postgres",
                        "password");
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }

        return analyticsConnection;
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

    protected boolean requiresSourceSchema() {
        return false;
    }

    protected abstract String getDockerComposeFile();

    protected abstract String getSourceDbServiceName();

    protected abstract int getSourceDbExposedPort();

    protected abstract String getSourceDbProtocol();

    protected abstract String getSourceDbName();

    protected abstract String getSourceDbUser();

    protected abstract String getSourceDbPassword();

    protected abstract String getTableDefinitionsPath();

    protected abstract String getAnalyticsLiquibaseFile();

    protected abstract String getTestFilename();

    protected String getResourcePath(String name) {
        return BaseJobTest.class.getClassLoader().getResource(name).getPath();
    }

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

    private void addTestFile(String file, String sourcePath, String destinationPath) throws IOException {
        Files.copy(Paths.get(sourcePath, file), Paths.get(destinationPath, file));
    }
}
