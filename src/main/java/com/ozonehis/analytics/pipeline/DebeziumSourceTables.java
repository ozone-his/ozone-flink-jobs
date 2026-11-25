package com.ozonehis.analytics.pipeline;

import com.ozonehis.analytics.sql.ConnectorOptions;
import com.ozonehis.analytics.sql.DebeziumTemporal;
import com.ozonehis.analytics.sql.SqlScript;
import java.util.ArrayList;
import java.util.List;
import org.apache.flink.table.api.EnvironmentSettings;
import org.apache.flink.table.api.TableEnvironment;
import org.apache.flink.table.catalog.Column;
import org.apache.flink.table.catalog.ResolvedSchema;
import org.apache.flink.table.types.logical.LogicalType;

/**
 * Registers a source table so that its date and time columns survive being read from Debezium.
 *
 * <p>Flink's {@code debezium-json} format silently drops the numbers Debezium writes for dates and
 * times, so those columns were NULL in every row read from Kafka. The topic is therefore read into
 * {@code <table>$debezium} with those columns as text, and a view under the table's own name
 * converts them back with {@link DebeziumTemporal}. Queries are unaffected.
 */
final class DebeziumSourceTables {

    /**
     * Suffix of the table holding the topic's raw values. It starts with {@code $} so the job
     * fingerprint, which matches table names as whole words, still ties this table to its queries.
     */
    static final String RAW_SUFFIX = "$debezium";

    /** Run once per job, before its tables. */
    static final String FUNCTION = "CREATE TEMPORARY SYSTEM FUNCTION IF NOT EXISTS DEBEZIUM_TEMPORAL AS '"
            + DebeziumTemporal.class.getName() + "'";

    /** Lets the parser accept a definition, which has no connector of its own yet. */
    private static final ConnectorOptions PARSE_ONLY = ConnectorOptions.of("datagen");

    /** Only parses definitions; nothing is ever executed in it. */
    private final TableEnvironment parser = TableEnvironment.create(EnvironmentSettings.inStreamingMode());

    private int parsed;

    /**
     * @return the raw table and its converting view, or the definition as written when it has no
     *     date or time column, or has computed columns a view could not reproduce
     */
    List<String> definitions(SqlScript table, ConnectorOptions options) {
        // The parser needs a database to create the table in, and a unique one per definition avoids
        // collisions. The database is never used for anything else, so it can be named anything.
        String database = "parse_" + parsed++;
        parser.executeSql("CREATE DATABASE `" + database + "`");
        parser.useDatabase(database);
        parser.executeSql(PARSE_ONLY.appendTo(table.content()));
        String[] created = parser.listTables();
        if (created.length != 1) {
            throw new IllegalArgumentException("Table definition '" + table.name()
                    + "' must create one table, named without a catalog or database");
        }
        String tableName = created[0];
        ResolvedSchema schema = parser.from("`" + tableName + "`").getResolvedSchema();
        List<Column> columns = schema.getColumns();
        if (columns.stream().noneMatch(column -> kind(column) != null)
                || !columns.stream().allMatch(Column::isPhysical)) {
            return List.of(options.appendTo(table.content()));
        }

        List<String> raw = new ArrayList<>();
        List<String> converted = new ArrayList<>();
        for (Column column : columns) {
            String name = "`" + column.getName() + "`";
            LogicalType type = column.getDataType().getLogicalType();
            String kind = kind(column);
            raw.add(name + " " + (kind == null ? type.asSerializableString() : "STRING"));
            converted.add(
                    kind == null
                            ? name
                            : "CAST(DEBEZIUM_TEMPORAL(" + name + ", '" + kind + "') AS "
                                    + type.copy(true).asSerializableString() + ") AS " + name);
        }
        schema.getPrimaryKey()
                .ifPresent(
                        key -> raw.add("PRIMARY KEY (`" + String.join("`, `", key.getColumns()) + "`) NOT ENFORCED"));

        String rawName = "`" + tableName + RAW_SUFFIX + "`";
        return List.of(
                options.appendTo("CREATE TABLE " + rawName + " (\n  " + String.join(",\n  ", raw) + "\n)"),
                "CREATE TEMPORARY VIEW `" + tableName + "` AS\nSELECT\n  " + String.join(",\n  ", converted) + "\nFROM "
                        + rawName);
    }

    private static String kind(Column column) {
        return switch (column.getDataType().getLogicalType().getTypeRoot()) {
            case DATE -> "DATE";
            case TIME_WITHOUT_TIME_ZONE -> "TIME";
            case TIMESTAMP_WITHOUT_TIME_ZONE -> "TIMESTAMP";
            default -> null;
        };
    }
}
