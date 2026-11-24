package com.ozonehis.analytics.sql;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import org.apache.flink.table.annotation.DataTypeHint;
import org.apache.flink.table.functions.ScalarFunction;

/**
 * {@code DEBEZIUM_TEMPORAL(value, kind)}: reads a date or time the way Debezium writes it, where
 * {@code kind} is {@code 'DATE'}, {@code 'TIME'} or {@code 'TIMESTAMP'}.
 *
 * <p>Debezium writes a {@code DATE} as days since 1970, a {@code DATETIME} as milliseconds (or
 * microseconds, for sub-millisecond columns) since 1970, and a {@code TIME} as microseconds since
 * midnight. Flink's {@code debezium-json} format only parses formatted text and drops anything
 * else, which left every such column NULL. Text is accepted too: a {@code TIMESTAMP} column
 * arrives in ISO form, and a converter plugin sends formatted text.
 */
public class DebeziumTemporal extends ScalarFunction {

    private static final LocalDateTime EPOCH = LocalDateTime.of(1970, 1, 1, 0, 0);

    /** Milliseconds stay below this until the year 5138; microseconds pass it in 1973. */
    private static final long MILLIS_LIMIT = 100_000_000_000_000L;

    public @DataTypeHint("TIMESTAMP(6)") LocalDateTime eval(String value, String kind) {
        return read(value, kind);
    }

    /** @return the value as a date-time, a date at midnight or a time on 1970-01-01; null if unreadable */
    static LocalDateTime read(String value, String kind) {
        if (value == null || value.isBlank() || kind == null) {
            return null;
        }
        String text = value.trim();
        try {
            if (text.matches("-?\\d+")) {
                long number = Long.parseLong(text);
                return switch (kind) {
                    case "DATE" -> EPOCH.plusDays(number);
                    case "TIME" -> EPOCH.plus(number, ChronoUnit.MICROS);
                    default ->
                        EPOCH.plus(number, Math.abs(number) < MILLIS_LIMIT ? ChronoUnit.MILLIS : ChronoUnit.MICROS);
                };
            }
            String iso = text.replace(' ', 'T').replaceFirst("Z$", "");
            if (kind.equals("TIME")) {
                return LocalTime.parse(iso).atDate(EPOCH.toLocalDate());
            }
            return iso.length() == 10 ? LocalDate.parse(iso).atStartOfDay() : LocalDateTime.parse(iso);
        } catch (RuntimeException e) {
            return null;
        }
    }
}
