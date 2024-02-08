/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ozonehis.data.pipelines.batch;

import com.ozonehis.data.pipelines.config.JdbcCatalogConfig;
import com.ozonehis.data.pipelines.config.JdbcSinkConfig;
import com.ozonehis.data.pipelines.config.JdbcSourceConfig;
import com.ozonehis.data.pipelines.utils.CommonUtils;
import com.ozonehis.data.pipelines.utils.ConnectorUtils;
import com.ozonehis.data.pipelines.utils.Environment;
import com.ozonehis.data.pipelines.utils.QueryFile;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.flink.connector.jdbc.catalog.JdbcCatalog;
import org.apache.flink.runtime.minicluster.MiniCluster;
import org.apache.flink.streaming.api.environment.RemoteStreamEnvironment;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.table.api.EnvironmentSettings;
import org.apache.flink.table.api.bridge.java.StreamTableEnvironment;

/**
 * <p>
 * For a tutorial how to write a Flink streaming application, check the tutorials and examples on
 * the <a href="https://flink.apache.org/docs/stable/">Flink Website</a>.
 * <p>
 * To package your application into a JAR file for execution, run 'mvn clean package' on the command
 * line.
 * <p>
 * If you change the name of the main class (with the public static void main(String[] args))
 * method, change the respective entry in the POM.xml file (simply search for 'mainClass').
 */
public class BatchETLJob {

    // private static final Logger LOG = new
    // Log4jLoggerFactory().getLogger(BatchETLJob.class.getName());
    private static String configFilePath =
            Environment.getEnv("ANALYTICS_CONFIG_FILE_PATH", "/etc/analytics/config.yaml");

    private static StreamTableEnvironment tableEnv = null;

    private static MiniCluster cluster = null;

    public static void main(String[] args) throws Exception {
        cluster = Environment.initMiniClusterWithEnv(false);
        cluster.start();
        StreamExecutionEnvironment env = new RemoteStreamEnvironment(
                cluster.getRestAddress().get().getHost(),
                cluster.getRestAddress().get().getPort(),
                cluster.getConfiguration());
        EnvironmentSettings envSettings =
                EnvironmentSettings.newInstance().inBatchMode().build();
        tableEnv = StreamTableEnvironment.create(env, envSettings);
        registerCatalogs();
        registerJdbcTables();
        executeFlattening();
        Environment.exitOnComplete(cluster);
    }

    private static void registerCatalogs() {
        for (JdbcCatalogConfig catalogConfig :
                CommonUtils.getConfig(configFilePath).getJdbcCatalogs()) {
            JdbcCatalog catalog = new JdbcCatalog(
                    BatchETLJob.class.getClassLoader(),
                    catalogConfig.getName(),
                    catalogConfig.getDefaultDatabase(),
                    catalogConfig.getUsername(),
                    catalogConfig.getPassword(),
                    catalogConfig.getBaseUrl());
            tableEnv.registerCatalog(catalogConfig.getName(), catalog);
        }
    }

    private static void registerJdbcTables() {
        for (JdbcSourceConfig jdbcSourceConfig :
                CommonUtils.getConfig(configFilePath).getJdbcSources()) {
            Stream<QueryFile> tables = CommonUtils.getSQL(jdbcSourceConfig.getTableDefinitionsPath()).stream();

            tables.forEach(s -> {
                Map<String, String> connectorOptions = Stream.of(new String[][] {
                            {"connector", "jdbc"},
                            {"url", jdbcSourceConfig.getDatabaseUrl()},
                            {"username", jdbcSourceConfig.getUsername()},
                            {"password", jdbcSourceConfig.getPassword()},
                            {"table-name", s.fileName},
                        })
                        .collect(Collectors.toMap(data -> data[0], data -> data[1]));

                String queryDSL = s.content + "\n" + " WITH (\n"
                        + ConnectorUtils.propertyJoiner(",", "=").apply(connectorOptions) + ")";
                tableEnv.executeSql(queryDSL);
            });
        }
    }

    private static void executeFlattening()
            throws IOException, ClassNotFoundException, InterruptedException, ExecutionException {
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
}
