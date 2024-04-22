package com.ozonehis.data.pipelines.streaming;

import com.ozonehis.data.pipelines.JobHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StreamingETLJob {

    private static final Logger LOG = LoggerFactory.getLogger(StreamingETLJob.class);

    public static void main(String[] args) {
        LOG.info("Executing flink job in stream mode");
        JobHelper.run(new StreamJob());
    }
}
