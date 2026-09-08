package com.ozonehis.analytics.config;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

/**
 * Validation helpers for the configuration records.
 *
 * <p>Each failure names the offending YAML path so the message points at the file the operator has
 * to fix, rather than at a Java field.
 */
final class Validation {

    private Validation() {}

    static String requireText(String value, String path) {
        if (value == null || value.isBlank()) {
            throw new ConfigException("Missing required configuration value: " + path);
        }
        return value;
    }

    static <T> T requireNonNull(T value, String path) {
        if (value == null) {
            throw new ConfigException("Missing required configuration value: " + path);
        }
        return value;
    }

    static <T> List<T> requireNonEmpty(List<T> values, String path) {
        if (values == null || values.isEmpty()) {
            throw new ConfigException("At least one entry is required under: " + path);
        }
        return List.copyOf(values);
    }

    static <T> List<T> nullToEmpty(List<T> values) {
        return values == null ? List.of() : List.copyOf(values);
    }

    static <T, K> void requireUniqueBy(List<T> values, Function<T, K> key, String path) {
        Set<K> seen = new HashSet<>();
        for (T value : values) {
            K k = key.apply(value);
            if (!seen.add(k)) {
                throw new ConfigException("Duplicate value '" + k + "' in " + path + "; entries must be unique");
            }
        }
    }
}
