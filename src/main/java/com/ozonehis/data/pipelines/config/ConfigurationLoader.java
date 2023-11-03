package com.ozonehis.data.pipelines.config;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;

import org.apache.commons.text.StringSubstitutor;
import org.apache.commons.text.lookup.StringLookupFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

public class ConfigurationLoader {
	
	private final ObjectMapper objectMapper;
	
	private final StringSubstitutor stringSubstitutor;
	
	public ConfigurationLoader() {
		this.objectMapper = new ObjectMapper(new YAMLFactory());
		this.stringSubstitutor = new StringSubstitutor(StringLookupFactory.INSTANCE.environmentVariableStringLookup());
	}
	
	public <T> T loadConfiguration(File config, Class<T> cls) {
		try {
			String contents = this.stringSubstitutor.replace(new String(Files.readAllBytes(config.toPath())));
			
			return this.objectMapper.readValue(contents, cls);
		}
		catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}
}
