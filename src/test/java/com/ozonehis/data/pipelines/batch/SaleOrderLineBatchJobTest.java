package com.ozonehis.data.pipelines.batch;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.ozonehis.data.pipelines.BaseOdooJobTest;
import com.ozonehis.data.pipelines.TestUtils;
import org.junit.jupiter.api.Test;

public class SaleOrderLineBatchJobTest extends BaseOdooJobTest {

    @Override
    protected String getTestFilename() {
        return "sale_order_lines";
    }

    @Override
    protected boolean requiresSourceSchema() {
        return true;
    }

    @Test
    public void execute_shouldLoadAllSaleOrderLinesFromOdooDbToAnalyticsDb() throws Exception {
        addTestDataToSourceDb("odoo/sale_order_line.sql");
        final int expectedCount = 2;
        final int count =
                TestUtils.getRows("sale_order_line", getSourceDbConnection()).size();
        assertEquals(expectedCount, count);
        BatchJob job = new BatchJob();
        initJobAndStartCluster(job);

        job.execute();
        // TODO Wait for job to complete, possibly use a JobListener
        Thread.sleep(5000);

        // TODO check each row data
        assertEquals(
                count,
                TestUtils.getRows("sale_order_lines", getAnalyticsDbConnection())
                        .size());
    }
}
