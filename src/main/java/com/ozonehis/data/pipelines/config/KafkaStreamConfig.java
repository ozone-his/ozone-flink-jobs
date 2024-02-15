package com.ozonehis.data.pipelines.config;

public class KafkaStreamConfig {

    private String topicPrefix;

    public String getTopicPrefix() {
        return topicPrefix;
    }

    public void setTopicPrefix(String topicPrefix) {
        this.topicPrefix = topicPrefix;
    }

    private String bootstrapServers;

    public String getBootstrapServers() {
        return bootstrapServers;
    }

    public void setBootstrapServers(String bootstrapServers) {
        this.bootstrapServers = bootstrapServers;
    }

    private String tableDefinitionsPath;

    public String getTableDefinitionsPath() {
        return tableDefinitionsPath;
    }

    public void setTableDefinitionsPath(String tableDefinitionsPath) {
        this.tableDefinitionsPath = tableDefinitionsPath;
    }
}
