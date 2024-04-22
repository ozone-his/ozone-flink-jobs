package com.ozonehis.data.pipelines.export;

import com.ozonehis.data.pipelines.JobHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BatchExport {

    private static final Logger LOG = LoggerFactory.getLogger(BatchExport.class);

    public static void main(String[] args) {
        LOG.info("Executing flink job in export mode");
        JobHelper.run(new ExportJob());
    }
}
