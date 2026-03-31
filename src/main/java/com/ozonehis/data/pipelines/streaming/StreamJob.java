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

package com.ozonehis.data.pipelines.streaming;

import com.ozonehis.data.pipelines.BaseJob;
import com.ozonehis.data.pipelines.config.JdbcSourceConfig;
import com.ozonehis.data.pipelines.config.KafkaStreamConfig;
import com.ozonehis.data.pipelines.utils.CommonUtils;
import com.ozonehis.data.pipelines.utils.ConnectorUtils;
import com.ozonehis.data.pipelines.utils.QueryFile;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
public class StreamJob extends BaseJob {

    private static final Logger LOG = LoggerFactory.getLogger(StreamJob.class);

    public StreamJob() {
        super(true);
    }

    @Override
    public void beforeExecute() {
        LOG.info("Registering databases tables for stream processing");
        for (KafkaStreamConfig kafkaStreamConfig :
                CommonUtils.getConfig(configFilePath).getKafkaStreams()) {
            Stream<QueryFile> tables = CommonUtils.getSQL(kafkaStreamConfig.getTableDefinitionsPath()).stream();
            tables.forEach(s -> {
                Map<String, String> connectorOptions = Stream.of(new String[][] {
                            {"connector", "kafka"},
                            {"properties.bootstrap.servers", kafkaStreamConfig.getBootstrapServers()},
                            {"properties.group.id", String.format("%s-group-id", s.fileName)},
                            {"topic", kafkaStreamConfig.getTopicPrefix() + String.format(".%s", s.fileName)},
                            {"scan.startup.mode", "earliest-offset"},
                            {"value.debezium-json.ignore-parse-errors", "false"},
                            {"value.format", "debezium-json"},
                        })
                        .collect(Collectors.toMap(data -> data[0], data -> data[1]));

                String queryDSL = s.content + "\n" + " WITH (\n"
                        + ConnectorUtils.propertyJoiner(",", "=").apply(connectorOptions) + ")";
                tableEnv.executeSql(queryDSL);
            });
        }

        // Register JDBC-backed lookup tables for streaming Lookup JOINs.
        // These tables query the source database directly instead of reading from Kafka,
        // ensuring data is always available at join time for dimension/reference data.
        List<JdbcSourceConfig> jdbcLookups = CommonUtils.getConfig(configFilePath).getJdbcLookups();
        if (jdbcLookups != null) {
            LOG.info("Registering JDBC lookup tables for stream processing");
            for (JdbcSourceConfig jdbcSourceConfig : jdbcLookups) {
                List<QueryFile> lookupTables = CommonUtils.getSQL(jdbcSourceConfig.getTableDefinitionsPath());
                for (QueryFile table : lookupTables) {
                    Map<String, String> connectorOptions = Stream.of(new String[][] {
                                {"connector", "jdbc"},
                                {"url", jdbcSourceConfig.getDatabaseUrl()},
                                {"username", jdbcSourceConfig.getUsername()},
                                {"password", jdbcSourceConfig.getPassword()},
                                {"table-name", table.fileName},
                                {"lookup.cache.max-rows", "5000"},
                                {"lookup.cache.ttl", "10min"},
                            })
                            .collect(Collectors.toMap(data -> data[0], data -> data[1]));

                    String queryDSL = table.content + "\n" + " WITH (\n"
                            + ConnectorUtils.propertyJoiner(",", "=").apply(connectorOptions) + ")";
                    LOG.info("Registering JDBC lookup table: {}", table.fileName);
                    tableEnv.executeSql(queryDSL);
                }
            }
        }
    }
}
