package com.ozonehis.data.pipelines;

import com.ozonehis.data.pipelines.batch.BatchJob;
import com.ozonehis.data.pipelines.export.ExportJob;
import com.ozonehis.data.pipelines.streaming.StreamJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Contains the logic to start and execute an ETL job.
 */
public class Bootstrap {

    private static final Logger LOG = LoggerFactory.getLogger(Bootstrap.class);

    public static void main(String[] args) {
        BaseJob job;
        if (args.length == 0) {
            LOG.info("Executing flink job in stream mode");
            job = new StreamJob();
        } else if ("batch".equalsIgnoreCase(args[0])) {
            LOG.info("Executing flink job in batch mode");
            job = new BatchJob();
        } else if ("export".equalsIgnoreCase(args[0])) {
            LOG.info("Executing flink job in export mode");
            job = new ExportJob();
        } else {
            throw new RuntimeException("Unsupported commandline argument: " + args[0]);
        }

        job.execute();
    }
}
