package com.ozonehis.data.pipelines.export;

import static com.ozonehis.data.pipelines.Constants.CFG_PLACEHOLDER_CATALOG;
import static org.mockito.Mockito.when;

import com.ozonehis.data.pipelines.config.AppConfiguration;
import com.ozonehis.data.pipelines.config.FileSinkConfig;
import com.ozonehis.data.pipelines.config.JdbcCatalogConfig;
import com.ozonehis.data.pipelines.utils.CommonUtils;
import com.ozonehis.data.pipelines.utils.QueryFile;
import java.util.List;
import org.apache.flink.table.api.bridge.java.StreamTableEnvironment;
import org.junit.Assert;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.powermock.reflect.internal.WhiteboxImpl;

@ExtendWith(MockitoExtension.class)
public class ExportJobTest {

    private static final String TEST_CFG_PATH = "/some/test/path";

    private static MockedStatic<CommonUtils> mockCommonUtils;

    private ExportJob job;

    @Mock
    private AppConfiguration mockConfig;

    @Mock
    private StreamTableEnvironment mockTableEnv;

    @BeforeEach
    public void setupClass() {
        mockCommonUtils = Mockito.mockStatic(CommonUtils.class);
        when(CommonUtils.getConfig(TEST_CFG_PATH)).thenReturn(mockConfig);
        job = new ExportJob();
        WhiteboxImpl.setInternalState(job, "configFilePath", TEST_CFG_PATH);
        WhiteboxImpl.setInternalState(job, "tableEnv", mockTableEnv);
    }

    @AfterEach
    public void tearDownClass() {
        mockCommonUtils.close();
    }

    @Test
    public void doExecute_shouldRejectAConfigWithMultipleJdbcCatalogs() {
        when(mockConfig.getJdbcCatalogs()).thenReturn(List.of(new JdbcCatalogConfig(), new JdbcCatalogConfig()));
        RuntimeException e = Assert.assertThrows(RuntimeException.class, () -> job.doExecute());
        Assert.assertEquals("Found multiple configured JDBC catalogs", e.getMessage());
    }

    @Test
    public void doExecute_shouldReplacePlaceholderWithCatalogNameInExportQueries() {
        final String catalog = "test";
        final String queryPath = "/test/query/path";
        final String query =
                "INSERT into patients SELECT t.*  from " + CFG_PLACEHOLDER_CATALOG + ".analytics.patients t";
        JdbcCatalogConfig catalogCfg = new JdbcCatalogConfig();
        catalogCfg.setName(catalog);
        when(CommonUtils.getSQL(queryPath)).thenReturn(List.of(new QueryFile(null, null, query)));
        FileSinkConfig fileSinkCfg = new FileSinkConfig();
        fileSinkCfg.setQueryPath(queryPath);
        when(mockConfig.getJdbcCatalogs()).thenReturn(List.of(catalogCfg));
        when(mockConfig.getFileSinks()).thenReturn(List.of(fileSinkCfg));

        job.doExecute();

        Mockito.verify(mockTableEnv).executeSql(query.replace(CFG_PLACEHOLDER_CATALOG, catalog));
    }
}
