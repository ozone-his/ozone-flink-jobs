package com.ozonehis.analytics.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.commons.text.StringSubstitutor;
import org.apache.commons.text.lookup.StringLookupFactory;

/**
 * Reads the analytics configuration from YAML.
 *
 * <p>Placeholders of the form {@code ${VAR}} and {@code ${VAR:-default}} are resolved against the
 * process environment, which is how credentials and hostnames are injected without being written
 * to the file.
 *
 * <p>Substitution happens <em>after</em> the YAML is parsed, on the resulting string values.
 * Substituting into the raw text first — the obvious implementation — means any value containing a
 * quote, colon or newline changes the document's structure: a password of {@code pa'ss} expands
 * {@code password: '${PW}'} into {@code password: 'pa'ss'}, which no longer parses. Resolving
 * after the structure is fixed makes a value's contents irrelevant to it.
 *
 * <p>Unknown keys are rejected rather than ignored: silently dropping a mistyped key is how a
 * pipeline ends up quietly not doing what its configuration says.
 */
public final class ConfigLoader {

    private final ObjectMapper mapper;

    private final StringSubstitutor substitutor;

    public ConfigLoader() {
        this(new StringSubstitutor(StringLookupFactory.INSTANCE.environmentVariableStringLookup()));
    }

    /** Creates a loader resolving placeholders from {@code variables} instead of the environment. */
    public static ConfigLoader withVariables(Map<String, String> variables) {
        return new ConfigLoader(new StringSubstitutor(variables));
    }

    ConfigLoader(StringSubstitutor substitutor) {
        // Built via the mapper builder rather than ObjectMapper.configure(MapperFeature, ...),
        // which is deprecated: MapperFeatures affect cached state and are meant to be set at build.
        this.mapper = YAMLMapper.builder()
                .enable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                .enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
                .enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS)
                .build();
        this.substitutor = substitutor;
    }

    /**
     * @param path the YAML file to read
     * @return the parsed, resolved and validated configuration
     * @throws ConfigException if the file is missing, malformed, or fails validation
     */
    public AnalyticsConfig load(Path path) {
        if (!Files.isRegularFile(path)) {
            throw new ConfigException("Analytics configuration file not found: " + path.toAbsolutePath());
        }

        try {
            return parse(
                    Files.readString(path, StandardCharsets.UTF_8),
                    path.toAbsolutePath().toString());
        } catch (IOException e) {
            throw new ConfigException("Unable to read analytics configuration: " + path.toAbsolutePath(), e);
        }
    }

    /** Parses YAML, resolves placeholders, then binds and validates. */
    public AnalyticsConfig parse(String yaml, String origin) {
        JsonNode tree;
        try {
            tree = mapper.readTree(yaml);
        } catch (IOException e) {
            throw new ConfigException("Malformed YAML in " + origin + ": " + e.getMessage(), e);
        }

        if (tree == null || tree.isNull() || tree.isMissingNode()) {
            throw new ConfigException("Analytics configuration is empty: " + origin);
        }

        try {
            return mapper.treeToValue(resolve(tree), AnalyticsConfig.class);
        } catch (JsonMappingException e) {
            // A ConfigException from a record's constructor is the precise message we want; Jackson
            // wraps it, so surface it rather than bury it under a mapping stack trace.
            if (rootCause(e) instanceof ConfigException cause) {
                throw new ConfigException(cause.getMessage() + " (in " + origin + ")", e);
            }
            throw new ConfigException("Invalid analytics configuration in " + origin + ": " + describe(e), e);
        } catch (IOException e) {
            throw new ConfigException("Unable to parse analytics configuration: " + origin, e);
        }
    }

    /** Recursively replaces placeholders in every string value of the tree. */
    private JsonNode resolve(JsonNode node) {
        if (node.isTextual()) {
            return TextNode.valueOf(substitutor.replace(node.textValue()));
        }
        if (node.isObject()) {
            ObjectNode object = (ObjectNode) node;
            object.fieldNames().forEachRemaining(field -> object.set(field, resolve(object.get(field))));
            return object;
        }
        if (node.isArray()) {
            ArrayNode array = (ArrayNode) node;
            for (int i = 0; i < array.size(); i++) {
                array.set(i, resolve(array.get(i)));
            }
            return array;
        }
        return node;
    }

    private static Throwable rootCause(Throwable t) {
        Throwable cause = t;
        while (cause.getCause() != null && cause.getCause() != cause) {
            cause = cause.getCause();
        }
        return cause;
    }

    /** Renders the YAML path Jackson failed on, e.g. {@code catalogs[0].dialect}. */
    private static String describe(JsonMappingException e) {
        String path = e.getPath().stream()
                .map(ref -> ref.getFieldName() != null ? ref.getFieldName() : "[" + ref.getIndex() + "]")
                .collect(Collectors.joining("."))
                .replace(".[", "[");
        return path.isEmpty() ? e.getOriginalMessage() : path + ": " + e.getOriginalMessage();
    }
}
