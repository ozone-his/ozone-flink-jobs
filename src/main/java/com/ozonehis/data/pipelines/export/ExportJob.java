package com.ozonehis.data.pipelines.export;

import com.ozonehis.data.pipelines.BaseJob;
import com.ozonehis.data.pipelines.Constants;
import com.ozonehis.data.pipelines.config.AppConfiguration;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExportJob extends BaseJob {

    private static final Logger LOG = LoggerFactory.getLogger(ExportJob.class);

    public ExportJob() {
        super(false);
    }

    @Override
    public void beforeExecute() {
        LOG.info("Registering destination tables for export");
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
        AppConfiguration cfg = CommonUtils.getConfig(configFilePath);
        if (cfg.getJdbcCatalogs().size() > 1) {
            throw new RuntimeException("Found multiple configured JDBC catalogs");
        }

        for (FileSinkConfig fileSinkConfig : cfg.getFileSinks()) {
            List<QueryFile> queries = CommonUtils.getSQL(fileSinkConfig.getQueryPath());
            final String catalog = cfg.getJdbcCatalogs().get(0).getName();
            for (QueryFile query : queries) {
                tableEnv.executeSql(query.content.replace(Constants.CFG_PLACEHOLDER_CATALOG, catalog));
            }
        }
    }
}
