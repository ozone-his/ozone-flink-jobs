package com.ozonehis.data.pipelines.config;

import java.util.List;

public class AppConfiguration {
	
	private List<JdbcCatalogConfig> jdbcCatalogs;
	
	private List<KafkaStreamConfig> kafkaStreams;
	
	private List<JdbcSinkConfig> jdbcSinks;
	
	private List<FileSinkConfig> fileSinks;
	
	public List<FileSinkConfig> getFileSinks() {
		return fileSinks;
	}
	
	public void setFileSinks(List<FileSinkConfig> fileSinks) {
		this.fileSinks = fileSinks;
	}
	
	private List<JdbcSourceConfig> jdbcSources;
	
	public List<JdbcSourceConfig> getJdbcSources() {
		return jdbcSources;
	}
	
	public void setJdbcSources(List<JdbcSourceConfig> jdbcSources) {
		this.jdbcSources = jdbcSources;
	}
	
	public List<JdbcSinkConfig> getJdbcSinks() {
		return jdbcSinks;
	}
	
	public void setJdbcSinks(List<JdbcSinkConfig> jdbcSinks) {
		this.jdbcSinks = jdbcSinks;
	}
	
	public List<JdbcCatalogConfig> getJdbcCatalogs() {
		return jdbcCatalogs;
	}
	
	public void setJdbcCatalogs(List<JdbcCatalogConfig> jdbcCatalogs) {
		this.jdbcCatalogs = jdbcCatalogs;
	}
	
	public List<KafkaStreamConfig> getKafkaStreams() {
		return kafkaStreams;
	}
	
	public void setKafkaStreams(List<KafkaStreamConfig> kafkaStreams) {
		this.kafkaStreams = kafkaStreams;
	}
}
