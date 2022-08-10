package net.mekomsolutions.data.pipelines.migrations;

import liquibase.Contexts;
import liquibase.LabelExpression;
import liquibase.Liquibase;
import liquibase.Scope;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.resource.ClassLoaderResourceAccessor;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class RunMigrations {
	
	static String flinkHome = System.getenv("FLINK_HOME");
	
	public static void main(String[] args) throws Exception {
		runLiquibase();
	}
	
	public static void runLiquibase() throws Exception {
		Map<String, Object> config = new HashMap<>();
		Properties prop = readPropertiesFile(flinkHome + "/job.properties");
		Scope.child(config, () -> {
			try {
				Connection connection = DriverManager.getConnection(prop.getProperty("postgres.url", ""),
				    prop.getProperty("postgres.user", ""), prop.getProperty("postgres.password", ""));
				Database database = DatabaseFactory.getInstance()
				        .findCorrectDatabaseImplementation(new JdbcConnection(connection));
				Liquibase liquibase = new liquibase.Liquibase("database/dbchangelog.xml", new ClassLoaderResourceAccessor(),
				        database);
				liquibase.update(new Contexts(), new LabelExpression());
			}
			catch (SQLException e) {
				throw new RuntimeException(e);
			}
		});
		
	}
	
	public static Properties readPropertiesFile(String fileName) throws IOException {
		FileInputStream fis = null;
		Properties prop = null;
		try {
			
			fis = new FileInputStream(fileName);
			prop = new Properties();
			prop.load(fis);
		}
		catch (IOException fnfe) {
			fnfe.printStackTrace();
		}
		finally {
			assert fis != null;
			fis.close();
		}
		return prop;
	}
}
