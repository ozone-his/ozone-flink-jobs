package com.ozonehis.data.pipelines.config;

public class FileSinkConfig {

    private String destinationTableDefinitionsPath;

    private String queryPath;

    private String exportOutputPath;

    private String format = "parquet";

    private String exportOutPutTag;

    public String getExportOutPutTag() {
        return exportOutPutTag;
    }

    public void setExportOutPutTag(String exportOutPutTag) {
        this.exportOutPutTag = exportOutPutTag;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public String getExportOutputPath() {
        return exportOutputPath;
    }

    public void setExportOutputPath(String exportOutputPath) {
        this.exportOutputPath = exportOutputPath;
    }

    public String getQueryPath() {
        return queryPath;
    }

    public void setQueryPath(String queryPath) {
        this.queryPath = queryPath;
    }

    public String getDestinationTableDefinitionsPath() {
        return destinationTableDefinitionsPath;
    }

    public void setDestinationTableDefinitionsPath(String destinationTableDefinitionsPath) {
        this.destinationTableDefinitionsPath = destinationTableDefinitionsPath;
    }
}
