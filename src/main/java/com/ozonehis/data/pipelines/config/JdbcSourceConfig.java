package com.ozonehis.data.pipelines.config;

public class JdbcSourceConfig {
	
	private String databaseUrl;
	
	private String username;
	
	private String password;
	
	private String tableDefinitionsPath;
	
	public String getTableDefinitionsPath() {
		return tableDefinitionsPath;
	}
	
	public void setTableDefinitionsPath(String tableDefinitionsPath) {
		this.tableDefinitionsPath = tableDefinitionsPath;
	}
	
	public String getPassword() {
		return password;
	}
	
	public void setPassword(String password) {
		this.password = password;
	}
	
	public String getUsername() {
		return username;
	}
	
	public void setUsername(String username) {
		this.username = username;
	}
	
	public String getDatabaseUrl() {
		return databaseUrl;
	}
	
	public void setDatabaseUrl(String databaseUrl) {
		this.databaseUrl = databaseUrl;
	}
}
