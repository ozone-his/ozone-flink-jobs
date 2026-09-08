package com.ozonehis.analytics.sql;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Builds the {@code WITH (...)} clause that binds a table definition to a Flink connector.
 *
 * <p>Values are emitted as SQL string literals with embedded quotes escaped. This matters: a
 * password containing an apostrophe would otherwise terminate the literal early and produce SQL
 * that either fails to parse or, worse, parses into something unintended.
 *
 * <p>Insertion order is preserved so the rendered SQL is stable across runs.
 */
public final class ConnectorOptions {

    private final Map<String, String> options = new LinkedHashMap<>();

    public static ConnectorOptions of(String connector) {
        return new ConnectorOptions().set("connector", connector);
    }

    public ConnectorOptions set(String key, String value) {
        if (key == null || key.isBlank()) {
            throw new IllegalArgumentException("Connector option key must not be blank");
        }
        if (value == null) {
            throw new IllegalArgumentException("Connector option '" + key + "' must not be null");
        }
        options.put(key, value);
        return this;
    }

    /**
     * Appends this clause to a {@code CREATE TABLE} definition.
     *
     * @param tableDefinition the {@code CREATE TABLE ...} statement, without a WITH clause
     * @return the definition with its connector options attached
     */
    public String appendTo(String tableDefinition) {
        return tableDefinition + "\nWITH (\n" + render() + "\n)";
    }

    /** @return the option list as it appears inside {@code WITH (...)} */
    String render() {
        StringBuilder sql = new StringBuilder();
        options.forEach((key, value) -> {
            if (!sql.isEmpty()) {
                sql.append(",\n");
            }
            sql.append("  '")
                    .append(escape(key))
                    .append("' = '")
                    .append(escape(value))
                    .append('\'');
        });
        return sql.toString();
    }

    /** Escapes a SQL string literal by doubling embedded single quotes. */
    private static String escape(String value) {
        return value.replace("'", "''");
    }
}
