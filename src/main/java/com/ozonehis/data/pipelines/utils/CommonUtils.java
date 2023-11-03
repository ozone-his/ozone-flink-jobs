package com.ozonehis.data.pipelines.utils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

import com.ozonehis.data.pipelines.config.AppConfiguration;
import com.ozonehis.data.pipelines.config.ConfigurationLoader;

public class CommonUtils {
	
	public static String getBaseName(String fileName) {
		int index = fileName.lastIndexOf('.');
		if (index == -1) {
			return fileName;
		} else {
			return fileName.substring(0, index);
		}
	}
	
	public static String readFile(Path path) throws IOException {
		byte[] encoded = Files.readAllBytes(path);
		return new String(encoded, StandardCharsets.UTF_8);
	}
	
	public static List<QueryFile> getSQL(String directory) {
		
		try {
			return Files.walk(Paths.get(directory)).filter(Files::isRegularFile)
			        .filter(p -> p.getFileName().toString().endsWith(".sql")).map(path -> {
				        try {
					        return new QueryFile(path.toFile().getParentFile().getName(),
					                getBaseName(path.getFileName().toString()), readFile(path));
				        }
				        catch (IOException e) {
					        throw new RuntimeException(e);
				        }
			        }).collect(Collectors.toList());
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
		
	}
	
	public static AppConfiguration getConfig(String configPath) {
		ConfigurationLoader configLoader = new ConfigurationLoader();
		AppConfiguration config = configLoader.loadConfiguration(new File(configPath), AppConfiguration.class);
		return config;
	}
}
