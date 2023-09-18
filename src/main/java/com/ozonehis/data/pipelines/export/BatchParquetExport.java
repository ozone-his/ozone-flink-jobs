package com.ozonehis.data.pipelines.export;

import com.ozonehis.data.pipelines.utils.ConnectorUtils;
import com.ozonehis.data.pipelines.utils.CommonUtils;
import com.ozonehis.data.pipelines.utils.QueryFile;
import com.ozonehis.data.pipelines.utils.Environment;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.flink.connector.jdbc.catalog.JdbcCatalog;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.table.api.EnvironmentSettings;
import org.apache.flink.table.api.StatementSet;
import org.apache.flink.table.api.bridge.java.StreamTableEnvironment;

public class BatchParquetExport {
	
	public static void main(String[] args) throws Exception {
		StreamExecutionEnvironment env = Environment.getExecutionEnvironment();
		EnvironmentSettings envSettings = EnvironmentSettings.newInstance().inBatchMode().build();
		String name = "analytics";
		String defaultDatabase = Environment.getEnv("ANALYTICS_DB_NAME", "analytics");
		String username = Environment.getEnv("ANALYTICS_DB_USER", "analytics");
		String password = Environment.getEnv("ANALYTICS_DB_PASSWORD", "analytics");
		String baseUrl = String.format("jdbc:postgresql://%s:%s", Environment.getEnv("ANALYTICS_DB_HOST", "localhost"),
		    Environment.getEnv("ANALYTICS_DB_PORT", "5432"));
		StreamTableEnvironment tableEnv = StreamTableEnvironment.create(env, envSettings);
		JdbcCatalog catalog = new JdbcCatalog(name, defaultDatabase, username, password, baseUrl);
		tableEnv.registerCatalog("analytics", catalog);
		Stream<QueryFile> tables = CommonUtils.getSQL(Environment.getEnv("EXPORT_DESTINATION_TABLES_PATH", "")).stream();
		tables.forEach(s -> {
			Map<String, String> connectorOptions = Stream
			        .of(new String[][] { { "connector", "filesystem" }, { "format", "parquet" },
			                { "sink.rolling-policy.file-size", "10MB" },
			                { "path",
			                        Environment.getEnv("EXPORT_OUTPUT_PATH", "/tmp") + "/" + s.fileName + "/"
			                                + Environment.getEnv("EXPORT_OUTPUT_TAG", "location1") + "/"
			                                + DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(LocalDateTime.now()) }, })
			        .collect(Collectors.toMap(data -> data[0], data -> data[1]));
			String queryDSL = s.content + "\n" + " WITH (\n"
			        + ConnectorUtils.propertyJoiner(",", "=").apply(connectorOptions) + ")";
			tableEnv.executeSql(queryDSL);
		});
		List<QueryFile> queries = CommonUtils.getSQL(Environment.getEnv("EXPORT_SOURCE_QUERIES_PATH", ""));
		StatementSet stmtSet = tableEnv.createStatementSet();
		for (QueryFile query : queries) {
			// String queryDSL = "INSERT INTO `analytics`.`analytics`.`" + query.fileName +
			// "`\n" + query.content;
			stmtSet.addInsertSql(query.content);
		}
		stmtSet.execute();
	}
	
}
