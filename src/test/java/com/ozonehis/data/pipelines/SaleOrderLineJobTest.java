package com.ozonehis.data.pipelines;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.ozonehis.data.pipelines.batch.BatchJob;
import org.junit.jupiter.api.Test;

public class SaleOrderLineJobTest extends BaseOdooJobTest {

    @Override
    protected boolean requiresSourceDb() {
        return true;
    }

    @Override
    protected String getTestFilename() {
        return "sale_order_lines";
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
        Thread.sleep(15000);

        // TODO check each row data
        assertEquals(
                count,
                TestUtils.getRows("sale_order_lines", getAnalyticsDbConnection())
                        .size());
    }
}
