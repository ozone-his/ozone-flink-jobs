package com.ozonehis.data.pipelines.export;

import com.ozonehis.data.pipelines.config.FileSinkConfig;
import com.ozonehis.data.pipelines.config.JdbcCatalogConfig;
import com.ozonehis.data.pipelines.utils.CommonUtils;
import com.ozonehis.data.pipelines.utils.ConnectorUtils;
import com.ozonehis.data.pipelines.utils.Environment;
import com.ozonehis.data.pipelines.utils.QueryFile;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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

public class BatchExport {

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
        registerDestinationTables();
        executeExport();
        Environment.exitOnCompletion(cluster);
    }

    private static void registerCatalogs() {
        for (JdbcCatalogConfig catalogConfig :
                CommonUtils.getConfig(configFilePath).getJdbcCatalogs()) {
            JdbcCatalog catalog = new JdbcCatalog(
                    BatchExport.class.getClassLoader(),
                    catalogConfig.getName(),
                    catalogConfig.getDefaultDatabase(),
                    catalogConfig.getUsername(),
                    catalogConfig.getPassword(),
                    catalogConfig.getBaseUrl());
            tableEnv.registerCatalog(catalogConfig.getName(), catalog);
        }
    }

    private static void registerDestinationTables() {
        for (FileSinkConfig fileSinkConfig :
                CommonUtils.getConfig(configFilePath).getFileSinks()) {
            Stream<QueryFile> tables = CommonUtils.getSQL(fileSinkConfig.getDestinationTableDefinitionsPath()).stream();

            tables.forEach(s -> {
                Map<String, String> connectorOptions = Stream.of(new String[][] {
                            {"connector", "filesystem"},
                            {"format", fileSinkConfig.getFormat()},
                            {"sink.rolling-policy.file-size", "10MB"},
                            {
                                "path",
                                fileSinkConfig.getExportOutputPath() + "/" + s.fileName + "/"
                                        + fileSinkConfig.getExportOutPutTag() + "/"
                                        + DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(LocalDateTime.now())
                            },
                        })
                        .collect(Collectors.toMap(data -> data[0], data -> data[1]));

                String queryDSL = s.content + "\n" + " WITH (\n"
                        + ConnectorUtils.propertyJoiner(",", "=").apply(connectorOptions) + ")";
                tableEnv.executeSql(queryDSL);
            });
        }
    }

    private static void executeExport()
            throws IOException, ClassNotFoundException, InterruptedException, ExecutionException {
        for (FileSinkConfig fileSinkConfig :
                CommonUtils.getConfig(configFilePath).getFileSinks()) {
            List<QueryFile> queries = CommonUtils.getSQL(fileSinkConfig.getQueryPath());
            for (QueryFile query : queries) {
                tableEnv.executeSql(query.content);
            }
        }
    }
}
