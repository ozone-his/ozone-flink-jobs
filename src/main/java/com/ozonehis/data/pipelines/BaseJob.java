package com.ozonehis.data.pipelines;

import static com.ozonehis.data.pipelines.Constants.DEFAULT_ANALYTICS_CONFIG_FILE_PATH;
import static com.ozonehis.data.pipelines.Constants.PROP_ANALYTICS_CONFIG_FILE_PATH;

import com.ozonehis.data.pipelines.batch.BatchJob;
import com.ozonehis.data.pipelines.config.JdbcCatalogConfig;
import com.ozonehis.data.pipelines.config.JdbcSinkConfig;
import com.ozonehis.data.pipelines.utils.CommonUtils;
import com.ozonehis.data.pipelines.utils.Environment;
import com.ozonehis.data.pipelines.utils.QueryFile;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Stream;
import org.apache.commons.lang3.StringUtils;
import org.apache.flink.connector.jdbc.catalog.JdbcCatalog;
import org.apache.flink.runtime.minicluster.MiniCluster;
import org.apache.flink.streaming.api.environment.RemoteStreamEnvironment;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.table.api.EnvironmentSettings;
import org.apache.flink.table.api.bridge.java.StreamTableEnvironment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base class for flink ETL jobs.
 */
public abstract class BaseJob {

    private static final Logger LOG = LoggerFactory.getLogger(BaseJob.class);

    protected String configFilePath;

    protected StreamTableEnvironment tableEnv;

    private MiniCluster cluster;

    public void initConfig() {
        if (StringUtils.isNotBlank(System.getProperty(PROP_ANALYTICS_CONFIG_FILE_PATH))) {
            configFilePath = System.getProperty(PROP_ANALYTICS_CONFIG_FILE_PATH);
        } else {
            configFilePath = Environment.getEnv(PROP_ANALYTICS_CONFIG_FILE_PATH, DEFAULT_ANALYTICS_CONFIG_FILE_PATH);
        }
    }

    /**
     * Starts a {@link MiniCluster} to execute this job.
     *
     * @throws Exception
     */
    public MiniCluster startCluster() throws Exception {
        LOG.info("Starting mini cluster");
        cluster = Environment.initMiniClusterWithEnv(false);
        cluster.start();
        return cluster;
    }

    /**
     * Creates the flink table environment for this job.
     *
     * @throws Exception
     */
    private void createEnvironment() throws ExecutionException, InterruptedException {
        LOG.info("Creating table environment");
        StreamExecutionEnvironment env = new RemoteStreamEnvironment(
                cluster.getRestAddress().get().getHost(),
                cluster.getRestAddress().get().getPort(),
                cluster.getConfiguration());
        EnvironmentSettings envSettings =
                EnvironmentSettings.newInstance().inBatchMode().build();
        tableEnv = StreamTableEnvironment.create(env, envSettings);
    }

    /**
     * Executes the job
     */
    public void execute() throws Exception {
        createEnvironment();
        registerCatalogs();
        beforeExecute();
        doExecute();
    }

    /**
     * Registers the JDBC catalogs
     */
    private void registerCatalogs() {
        LOG.info("Registering catalogs");
        for (JdbcCatalogConfig catalogConfig :
                CommonUtils.getConfig(configFilePath).getJdbcCatalogs()) {
            JdbcCatalog catalog = new JdbcCatalog(
                    BatchJob.class.getClassLoader(),
                    catalogConfig.getName(),
                    catalogConfig.getDefaultDatabase(),
                    catalogConfig.getUsername(),
                    catalogConfig.getPassword(),
                    catalogConfig.getBaseUrl());
            tableEnv.registerCatalog(catalogConfig.getName(), catalog);
        }
    }

    /**
     * Executes the ETL logic
     *
     * @throws InterruptedException
     * @throws ExecutionException
     */
    protected void doExecute() throws InterruptedException, ExecutionException {
        String[] jobNames =
                cluster.listJobs().get().stream().map(job -> job.getJobName()).toArray(String[]::new);
        for (JdbcSinkConfig jdbcSinkConfig :
                CommonUtils.getConfig(configFilePath).getJdbcSinks()) {
            List<QueryFile> queries = CommonUtils.getSQL(jdbcSinkConfig.getQueryPath());
            for (QueryFile query : queries) {
                String queryDSL = "INSERT INTO  `" + jdbcSinkConfig.getJdbcCatalog() + "`.`"
                        + jdbcSinkConfig.getDatabaseName() + "`.`" + query.fileName + "`\n" + query.content;
                if (Stream.of(jobNames)
                        .noneMatch(jobName -> jobName.equals("insert-into_" + jdbcSinkConfig.getJdbcCatalog() + "."
                                + jdbcSinkConfig.getDatabaseName() + "." + query.fileName))) {
                    tableEnv.executeSql(queryDSL);
                }
            }
        }
    }

    /**
     * Called before job execution to allow subclasses to run any necessary preparation logic.
     */
    public abstract void beforeExecute();
}
