package com.ozonehis.data.pipelines.utils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

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
			return Files.walk(Paths.get(directory)).filter(Files::isRegularFile).map(path -> {
				try {
					return new QueryFile(path.toFile().getParentFile().getName(), getBaseName(path.getFileName().toString()),
					        readFile(path));
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
}
