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

package net.mekomsolutions.data.pipelines.batch;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.mekomsolutions.data.pipelines.shared.jobs.Job;
import net.mekomsolutions.data.pipelines.utils.CommonUtils;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.table.api.EnvironmentSettings;
import org.apache.flink.table.api.StatementSet;
import org.apache.flink.table.api.TableEnvironment;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
    // private static final Logger LOG = new Log4jLoggerFactory().getLogger(StreamingETLJob.class.getName());

    public static void main(String[] args) throws Exception {
        String propertiesFilePath = System.getProperty("user.dir") + "/job.properties";
        final ParameterTool parameterTool = ParameterTool.fromArgs(args);
        propertiesFilePath = parameterTool.get("properties-file", "/opt/flink/usrlib/job.properties");
        ParameterTool parameter = ParameterTool.fromPropertiesFile(propertiesFilePath);
        Map<String, String> openmrsConnectorOptions = Stream
                .of(new String[][] { { "connector", "jdbc" }, { "url", parameterTool.get("source-url", "")  },
                        { "username", parameterTool.get("source-username", "") },
                        { "password", parameterTool.get("source-password", "") }, })
                .collect(Collectors.toMap(data -> data[0], data -> data[1]));
        Map<String, String> postgresConnectorOptions = Stream
                .of(new String[][] { { "connector", "jdbc" }, { "url", parameterTool.get("sink-url", "") },
                        { "username", parameterTool.get("sink-username", "") },
                        { "password", parameterTool.get("sink-password", "") }, { "sink.buffer-flush.max-rows", "1000" },
                        { "sink.buffer-flush.interval", "1s" } })
                .collect(Collectors.toMap(data -> data[0], data -> data[1]));

        EnvironmentSettings settings = EnvironmentSettings.newInstance().inBatchMode().build();

        TableEnvironment tEnv = TableEnvironment.create(settings);
        tEnv.getConfig().getConfiguration().setString("restart-strategy", "exponential-delay");
        // set the statebackend type to "rocksdb", other available options are "filesystem" and "jobmanager"
        // you can also set the full qualified Java class name of the StateBackendFactory to this option
        // e.g. org.apache.flink.contrib.streaming.state.RocksDBStateBackendFactory
        tEnv.getConfig().getConfiguration().setString("taskmanager.network.numberOfBuffers", "20");
        tEnv.getConfig() // access high-level configuration
                .getConfiguration() // set low-level key-value options
                .setString("table.exec.resource.default-parallelism", parameter.get("table.exec.resource.default-parallelism", "6") );
        // set the checkpoint directory, which is required by the RocksDB statebackend

        CommonUtils.setupSourceTables(tEnv, CommonUtils.SOURCE_TABLES, openmrsConnectorOptions);
        CommonUtils.setupSinkTables(tEnv, CommonUtils.SINK_TABLES, postgresConnectorOptions);
        final ObjectMapper objectMapper = new ObjectMapper();
        Job[] jobs = objectMapper.readValue(CommonUtils.getResourceFileAsString("jobs.json"), Job[].class);
        StatementSet stmtSet = tEnv.createStatementSet();
        for (Job job : jobs) {
            String queryDSL = "INSERT INTO " + job.getName() + " \n" + CommonUtils.getResourceFileAsString(job.getSourceFilePath());
            stmtSet.addInsertSql(queryDSL);
        }

        stmtSet.execute();
    }


}
