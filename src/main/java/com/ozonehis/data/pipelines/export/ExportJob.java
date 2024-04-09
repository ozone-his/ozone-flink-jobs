package com.ozonehis.data.pipelines.export;

import com.ozonehis.data.pipelines.BaseJob;
import com.ozonehis.data.pipelines.config.FileSinkConfig;
import com.ozonehis.data.pipelines.utils.CommonUtils;
import com.ozonehis.data.pipelines.utils.ConnectorUtils;
import com.ozonehis.data.pipelines.utils.QueryFile;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ExportJob extends BaseJob {

    @Override
    public void beforeExecute() {
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

    @Override
    protected void doExecute() {
        for (FileSinkConfig fileSinkConfig :
                CommonUtils.getConfig(configFilePath).getFileSinks()) {
            List<QueryFile> queries = CommonUtils.getSQL(fileSinkConfig.getQueryPath());
            for (QueryFile query : queries) {
                tableEnv.executeSql(query.content);
            }
        }
    }
}
