package com.ozonehis.analytics.sql;

import java.util.Objects;

/**
 * A {@code .sql} file loaded from disk.
 *
 * @param name the file name without its extension. This is significant: it is the destination
 *     table name for a flattening query, and the source table (and Kafka topic suffix) for a table
 *     definition.
 * @param content the raw SQL
 */
public record SqlScript(String name, String content) {

    public SqlScript {
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(content, "content");
    }
}
