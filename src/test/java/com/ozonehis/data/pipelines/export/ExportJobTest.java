package com.ozonehis.data.pipelines.export;

import static com.ozonehis.data.pipelines.Constants.PROP_ANALYTICS_CONFIG_FILE_PATH;
import static org.mockito.Mockito.when;

import com.ozonehis.data.pipelines.config.AppConfiguration;
import com.ozonehis.data.pipelines.config.JdbcCatalogConfig;
import com.ozonehis.data.pipelines.utils.CommonUtils;
import java.util.List;
import org.junit.Assert;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class ExportJobTest {

    private static final String TEST_CFG_PATH = "/some/test/path";

    private static MockedStatic<CommonUtils> mockCommonUtils;

    private ExportJob job;

    @Mock
    private AppConfiguration mockConfig;

    @BeforeEach
    public void setupClass() {
        mockCommonUtils = Mockito.mockStatic(CommonUtils.class);
        when(CommonUtils.getConfig(TEST_CFG_PATH)).thenReturn(mockConfig);
        System.setProperty(PROP_ANALYTICS_CONFIG_FILE_PATH, TEST_CFG_PATH);
        job = new ExportJob();
        job.initConfig();
    }

    @AfterEach
    public void tearDownClass() {
        mockCommonUtils.close();
        System.clearProperty(PROP_ANALYTICS_CONFIG_FILE_PATH);
    }

    @Test
    public void doExecute_shouldRejectAConfigWithMultipleJdbcCatalogs() {
        when(mockConfig.getJdbcCatalogs()).thenReturn(List.of(new JdbcCatalogConfig(), new JdbcCatalogConfig()));
        RuntimeException e = Assert.assertThrows(RuntimeException.class, () -> job.doExecute());
        Assert.assertEquals("Found multiple configured JDBC catalogs", e.getMessage());
    }

    @Test
    public void doExecute_shouldPassForAConfigWithASingleJdbcCatalog() {
        when(mockConfig.getJdbcCatalogs()).thenReturn(List.of(new JdbcCatalogConfig()));
        job.doExecute();
    }
}
