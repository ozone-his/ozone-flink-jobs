package com.ozonehis.data.pipelines.batch;

import com.ozonehis.data.pipelines.JobHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BatchETLJob {

    private static final Logger LOG = LoggerFactory.getLogger(BatchETLJob.class);

    public static void main(String[] args) {
        LOG.info("Executing flink job in batch mode");
        JobHelper.run(new BatchJob());
    }
}
