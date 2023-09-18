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

import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.connector.jdbc.catalog.JdbcCatalog;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.table.api.EnvironmentSettings;
import org.apache.flink.table.api.StatementSet;
import org.apache.flink.table.api.bridge.java.StreamTableEnvironment;

import com.ozonehis.data.pipelines.utils.CommonUtils;
import com.ozonehis.data.pipelines.utils.QueryFile;
import com.ozonehis.data.pipelines.utils.ConnectorUtils;
import com.ozonehis.data.pipelines.utils.Environment;

import java.util.List;
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
		StreamExecutionEnvironment env = Environment.getExecutionEnvironment();
		EnvironmentSettings envSettings = EnvironmentSettings.newInstance().inBatchMode().build();
		String name = "analytics";
		String defaultDatabase = Environment.getEnv("ANALYTICS_DB_NAME", "analytics");
		String username = Environment.getEnv("ANALYTICS_DB_USER", "analytics");
		String password = Environment.getEnv("ANALYTICS_DB_PASSWORD", "analytics");
		String baseUrl = String.format("jdbc:postgresql://%s:%s", Environment.getEnv("ANALYTICS_DB_HOST", "localhost"),
		    Environment.getEnv("ANALYTICS_DB_PORT", "5432"));
		
		String openmrsDBName = Environment.getEnv("OPENMRS_DB_NAME", "openmrs");
		String openmrsDBuser = Environment.getEnv("OPENMRS_DB_USER", "openmrs");
		String openmrsDBpassword = Environment.getEnv("OPENMRS_DB_PASSWORD", "openmrs");
		String openmrsDBhost = Environment.getEnv("OPENMRS_DB_HOST", "localhost");
		String openmrsDBport = Environment.getEnv("OPENMRS_DB_PORT", "3306");
		String openmrsDBurl = String.format("jdbc:mysql://%s:%s/%s?sslmode=disable", openmrsDBhost, openmrsDBport,
		    openmrsDBName);
		
		String odooDBName = Environment.getEnv("ODOO_DB_NAME", "odoo");
		String odooDBuser = Environment.getEnv("ODOO_DB_USER", "odoo");
		String odooDBpassword = Environment.getEnv("ODOO_DB_PASSWORD", "odoo");
		String odooDBhost = Environment.getEnv("ODOO_DB_HOST", "localhost");
		String odooDBport = Environment.getEnv("ODOO_DB_PORT", "5432");
		String odooDBurl = String.format("jdbc:postgresql://%s:%s/%s?sslmode=disable", odooDBhost, odooDBport, odooDBName);
		
		StreamTableEnvironment tableEnv = StreamTableEnvironment.create(env, envSettings);
		JdbcCatalog catalog = new JdbcCatalog(name, defaultDatabase, username, password, baseUrl);
		tableEnv.registerCatalog("analytics", catalog);
		Stream<QueryFile> tables = CommonUtils.getSQL(Environment.getEnv("ANALYTICS_SOURCE_TABLES_PATH", "")).stream();
		tables.forEach(s -> {
			Map<String, String> connectorOptions = null;
			if (s.parent.equals("openmrs")) {
				connectorOptions = Stream
				        .of(new String[][] { { "connector", "jdbc" }, { "url", openmrsDBurl }, { "username", openmrsDBuser },
				                { "password", openmrsDBpassword }, { "table-name", s.fileName }, })
				        .collect(Collectors.toMap(data -> data[0], data -> data[1]));
			} else if (s.parent.equals("odoo")) {
				connectorOptions = Stream
				        .of(new String[][] { { "connector", "jdbc" }, { "url", odooDBurl }, { "username", odooDBuser },
				                { "password", odooDBpassword }, { "table-name", s.fileName }, })
				        .collect(Collectors.toMap(data -> data[0], data -> data[1]));
			}
			String queryDSL = s.content + "\n" + " WITH (\n"
			        + ConnectorUtils.propertyJoiner(",", "=").apply(connectorOptions) + ")";
			tableEnv.executeSql(queryDSL);
		});
		List<QueryFile> queries = CommonUtils.getSQL(Environment.getEnv("ANALYTICS_QUERIES_PATH", ""));
		StatementSet stmtSet = tableEnv.createStatementSet();
		for (QueryFile query : queries) {
			String queryDSL = "INSERT INTO  `analytics`.`analytics`.`" + query.fileName + "`\n" + query.content;
			stmtSet.addInsertSql(queryDSL);
		}
		stmtSet.execute();
	}
	
}
