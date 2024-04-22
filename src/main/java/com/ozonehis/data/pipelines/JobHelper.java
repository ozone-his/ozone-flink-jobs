package com.ozonehis.data.pipelines;

import com.ozonehis.data.pipelines.utils.Environment;
import org.apache.flink.runtime.minicluster.MiniCluster;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Contains the logic to start and execute an ETL job.
 */
public class JobHelper {

    private static final Logger LOG = LoggerFactory.getLogger(JobHelper.class);

    public static void run(BaseJob job) {
        MiniCluster cluster = null;
        try {
            job.initConfig();
            cluster = job.startCluster();
            job.execute();
        } catch (Throwable t) {
            LOG.error("An error was encountered while executing the job", t);
        } finally {
            if (cluster != null) {
                Environment.exitOnCompletion(cluster);
            }
        }
    }
}
