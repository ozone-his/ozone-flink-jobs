package com.ozonehis.data.pipelines;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.ozonehis.data.pipelines.export.ExportJob;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

public class SaleOrderLineExportJobTest extends BaseOdooJobTest {

    @Override
    protected String getTestFilename() {
        return "sale_order_lines";
    }

    @Test
    public void execute_shouldExportAllSaleOrderLinesFromAnalyticsDbToAFile() throws Exception {
        addTestDataToAnalyticsDb("sale_order_lines.sql");
        final int expectedCount = 2;
        assertEquals(
                expectedCount,
                TestUtils.getRows("sale_order_lines", getAnalyticsDbConnection())
                        .size());
        ExportJob job = new ExportJob();
        initJobAndStartCluster(job);

        job.execute();
        // TODO Wait for job to complete, possibly use a JobListener
        Thread.sleep(15000);

        final String outputDir = exportDir + "/sale_order_lines/h1";
        final JsonMapper mapper = new JsonMapper();
        List<Object> orderLines = new ArrayList<>();
        Path outputPath = Files.list(
                        Files.list(Paths.get(outputDir)).findFirst().get())
                .findFirst()
                .get();
        try (MappingIterator<Object> it = mapper.readerFor(Object.class).readValues(Files.readString(outputPath))) {
            while (it.hasNextValue()) {
                orderLines.add(it.nextValue());
            }
        }

        assertEquals(expectedCount, orderLines.size());
    }
}
