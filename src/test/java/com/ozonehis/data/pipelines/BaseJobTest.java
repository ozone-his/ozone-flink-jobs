package com.ozonehis.data.pipelines;

import static com.ozonehis.data.pipelines.Constants.PROP_ANALYTICS_CONFIG_FILE_PATH;
import static com.ozonehis.data.pipelines.Constants.PROP_FLINK_REST_PORT;
import static java.time.temporal.ChronoUnit.SECONDS;

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
import java.util.ArrayList;
import java.util.Collections;
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

    private static final Integer WAIT = 600;

    private static final String DELETE = "DELETE FROM ";

    private static final String DISABLE_KEYS = "SET FOREIGN_KEY_CHECKS=0";

    private static final String ENABLE_KEYS = "SET FOREIGN_KEY_CHECKS=1";

    private static ObjectMapper MAPPER = new ObjectMapper(new YAMLFactory());

    private static final String TEST_DIR = "flink-test-dir";

    private static final String EXPORT_DIR_NAME = "export";

    public static final String USER_ANALYTICS_DB = "analytics";

    public static final String PASSWORD_ANALYTICS_DB = "password";

    public static final String DB_NAME_ANALYTICS = "analytics";

    public static final String USER_OPENMRS_DB = "openmrs";

    public static final String PASSWORD_OPENMRS_DB = "password";

    public static final String DB_NAME_OPENMRS = "openmrs";

    public static final String USER_ODOO_DB = "odoo";

    public static final String PASSWORD_ODOO_DB = "password";

    public static final String DB_NAME_ODOO = "odoo";

    private AppConfiguration config;

    private ContainerState sourceDb;

    private Connection sourceConnection;

    protected ContainerState analyticsDb;

    private Connection analyticsConnection;

    protected static String testDir;

    protected static String exportDir;

    protected ComposeContainer ozoneCompose;

    protected ComposeContainer analyticsCompose;

    private MiniCluster cluster;

    @BeforeAll
    public static void beforeAllSuper() {
        try {
            testDir = Files.createTempDirectory(TEST_DIR).toFile().getAbsolutePath();
            exportDir = testDir + "/" + EXPORT_DIR_NAME;
            TestUtils.createNetworkIfNecessary("web", testDir);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @AfterAll
    public static void afterAllSuper() throws IOException {
        System.clearProperty(PROP_ANALYTICS_CONFIG_FILE_PATH);
        System.clearProperty(PROP_FLINK_REST_PORT);
        FileUtils.forceDelete(new File(testDir));
    }

    @BeforeEach
    public void beforeSuper() throws Exception {
        Map<String, String> commonEnvs = new HashMap<>();
        commonEnvs.put("DISTRO_PATH", getResourcePath("distro"));
        commonEnvs.put("SQL_SCRIPTS_PATH", getResourcePath("distro/data"));
        commonEnvs.put("POSTGRES_PASSWORD", "password");
        commonEnvs.put("POSTGRES_USER", "postgres");
        commonEnvs.put("POSTGRES_DB_HOST", "postgresql");
        Map<String, String> ozoneEnvs = new HashMap<>(commonEnvs);

        ozoneEnvs.put("MYSQL_ROOT_PASSWORD", PASSWORD_OPENMRS_DB);
        ozoneEnvs.put("OPENMRS_DB_NAME", DB_NAME_OPENMRS);
        ozoneEnvs.put("OPENMRS_DB_USER", USER_OPENMRS_DB);
        ozoneEnvs.put("OPENMRS_DB_PASSWORD", "password");
        ozoneEnvs.put("OPENMRS_CONFIG_PATH", getResourcePath("distro/configs/openmrs/initializer_config"));
        ozoneEnvs.put("OPENMRS_PROPERTIES_PATH", getResourcePath("distro/configs/openmrs/properties"));
        ozoneEnvs.put("OPENMRS_FRONTEND_BINARY_PATH", getResourcePath("distro/binaries/openmrs/frontend"));
        ozoneEnvs.put("OPENMRS_FRONTEND_CONFIG_PATH", getResourcePath("distro/configs/openmrs/frontend_config"));

        ozoneEnvs.put("ODOO_CONFIG_FILE_PATH", getResourcePath("distro/configs/odoo/config/odoo.conf"));
        ozoneEnvs.put("ODOO_DB_NAME", DB_NAME_ODOO);
        ozoneEnvs.put("ODOO_DB_USER", USER_ODOO_DB);
        ozoneEnvs.put("ODOO_DB_PASSWORD", PASSWORD_ODOO_DB);
        ozoneEnvs.put("EIP_ODOO_OPENMRS_ROUTES_PATH", testDir);

        List<File> ozoneComposeFiles = new ArrayList<>();
        ozoneComposeFiles.add(new File(getResourcePath("run/docker/docker-compose-common.yml")));
        List<String> ozoneServices = new ArrayList<>();
        ozoneServices.add("env-substitution");
        ozoneServices.add("postgresql");
        if (requiresSourceDb()) {
            ozoneComposeFiles.add(new File(getResourcePath("run/docker/" + getDockerComposeFile())));
            ozoneServices.add(getSourceDbServiceName());
            ozoneServices.add(getSourceSystemName());
            if ("odoo".equalsIgnoreCase(getSourceSystemName())) {
                ozoneComposeFiles.add(new File(getResourcePath("run/docker/docker-compose-openmrs.yml")));
            }
        }

        ozoneCompose = new ComposeContainer(ozoneComposeFiles)
                .withEnv(ozoneEnvs)
                .withTailChildContainers(true)
                .withServices(ozoneServices.toArray(String[]::new));
        if (requiresSourceDb()) {
            ozoneCompose.withExposedService(
                    getSourceDbServiceName(), getSourceDbExposedPort(), Wait.forListeningPort());
            if ("openmrs".equals(getSourceSystemName())) {
                ozoneCompose.waitingFor(
                        "openmrs", Wait.forHealthcheck().withStartupTimeout(Duration.of(WAIT, SECONDS)));
            } else if ("odoo".equals(getSourceSystemName())) {
                ozoneCompose.waitingFor(
                        "odoo",
                        Wait.forLogMessage(".*odoo\\.modules\\.loading: Modules loaded.*", 1)
                                .withStartupTimeout(Duration.of(120, SECONDS)));
            }
        }
        ozoneCompose.withStartupTimeout(Duration.of(WAIT, SECONDS));

        Map<String, String> analyticsEnvs = new HashMap<>(commonEnvs);
        analyticsEnvs.put("ANALYTICS_DB_NAME", DB_NAME_ANALYTICS);
        analyticsEnvs.put("ANALYTICS_DB_USER", USER_ANALYTICS_DB);
        analyticsEnvs.put("ANALYTICS_DB_PASSWORD", PASSWORD_ANALYTICS_DB);
        analyticsEnvs.put("SUPERSET_CONFIG_PATH", getResourcePath("distro/configs/superset"));
        analyticsEnvs.put("SUPERSET_DASHBOARDS_PATH", getResourcePath("distro/configs/superset/assets"));
        analyticsEnvs.put("SUPERSET_DB", "superset");
        analyticsEnvs.put("SUPERSET_DB_USER", "superset");
        analyticsEnvs.put("SUPERSET_DB_PASSWORD", "password");
        List<File> analyticsComposeFiles = new ArrayList<>();
        analyticsComposeFiles.add(new File(getResourcePath("docker-compose-db.yaml")));
        analyticsComposeFiles.add(new File(getResourcePath("docker-compose-superset.yaml")));
        List<String> analyticsServices = new ArrayList<>();
        analyticsServices.add("postgresql");
        analyticsServices.add("superset");
        analyticsCompose = new ComposeContainer(analyticsComposeFiles)
                .withEnv(analyticsEnvs)
                .withTailChildContainers(true)
                .withServices(analyticsServices.toArray(String[]::new))
                .withExposedService("postgresql", 5432, Wait.forListeningPort());
        analyticsCompose.withStartupTimeout(Duration.of(WAIT, SECONDS));

        long start = System.currentTimeMillis();
        ozoneCompose.start();
        analyticsCompose.start();
        long duration = (System.currentTimeMillis() - start) / 1000;
        System.out.println("Compose containers startup took: " + duration + "secs");

        analyticsDb = analyticsCompose.getContainerByServiceName("postgresql").get();
        if (requiresSourceDb()) {
            sourceDb = ozoneCompose
                    .getContainerByServiceName(getSourceDbServiceName())
                    .get();
        }
        createAnalyticsSchema();
        setupConfig();
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

        ozoneCompose.stop();
        analyticsCompose.stop();
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
        if (requiresSourceDb()) {
            JdbcSourceConfig source = new JdbcSourceConfig();
            source.setDatabaseUrl("jdbc:" + getSourceDbProtocol() + "://localhost:"
                    + sourceDb.getMappedPort(getSourceDbExposedPort()) + "/" + getSourceDbName());
            source.setUsername(getSourceDbUser());
            source.setPassword(getSourceDbPassword());
            source.setTableDefinitionsPath(getTableDefinitionsPath());
            config.setJdbcSources(List.of(source));
        } else {
            config.setJdbcSources(Collections.emptyList());
        }

        JdbcSinkConfig jdbcSinkCfg = new JdbcSinkConfig();
        jdbcSinkCfg.setJdbcCatalog(catalogName);
        jdbcSinkCfg.setDatabaseName(DB_NAME_ANALYTICS);
        final String flattenQueryPath = testDir + "/dsl/flattening/queries";
        Files.createDirectories(Paths.get(flattenQueryPath));
        addTestFile(getTestFilename() + ".sql", getResourcePath("dsl/flattening/queries"), flattenQueryPath);
        jdbcSinkCfg.setQueryPath(flattenQueryPath);
        config.setJdbcSinks(List.of(jdbcSinkCfg));
        FileSinkConfig fileSinkCfg = new FileSinkConfig();
        final String exportQueryPath = testDir + "/dsl/export/queries";
        Files.createDirectories(Paths.get(exportQueryPath));
        addTestFile(getTestFilename() + ".sql", getResourcePath("dsl/export/queries"), exportQueryPath);
        fileSinkCfg.setQueryPath(exportQueryPath);
        final String exportTablePath = testDir + "/dsl/export/tables";
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
                        "jdbc:postgresql://localhost:" + analyticsDb.getMappedPort(5432) + "/" + DB_NAME_ANALYTICS,
                        USER_ANALYTICS_DB,
                        PASSWORD_ANALYTICS_DB);
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

    protected boolean requiresSourceDb() {
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

    protected abstract String getSourceSystemName();

    protected abstract String getTestFilename();

    protected String getResourcePath(String name) {
        return BaseJobTest.class.getClassLoader().getResource(name).getPath();
    }

    private void createAnalyticsSchema() {
        try {
            updateDatabase(getLiquibase(getAnalyticsLiquibaseFile(), getAnalyticsDbConnection()));
        } catch (LiquibaseException e) {
            throw new RuntimeException(e);
        }
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
