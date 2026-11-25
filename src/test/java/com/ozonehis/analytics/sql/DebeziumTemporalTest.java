package com.ozonehis.analytics.sql;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

/**
 * The numbers are what Debezium 3.6 wrote for a MariaDB row holding 2001-12-31 12:34:56, captured
 * from a running connector with the shipped configuration.
 */
class DebeziumTemporalTest {

    private static final LocalDateTime MOMENT = LocalDateTime.of(2001, 12, 31, 12, 34, 56);

    @Test
    void readsTheNumbersDebeziumWrites() {
        assertThat(DebeziumTemporal.read("1009802096000", "TIMESTAMP")).isEqualTo(MOMENT); // DATETIME
        assertThat(DebeziumTemporal.read("1009802096123456", "TIMESTAMP"))
                .isEqualTo(MOMENT.withNano(123_456_000)); // DATETIME(6)
        assertThat(DebeziumTemporal.read("11687", "DATE"))
                .isEqualTo(MOMENT.toLocalDate().atStartOfDay());
        assertThat(DebeziumTemporal.read("45296000000", "TIME").toLocalTime()).isEqualTo(MOMENT.toLocalTime());
        // Before 1970 the numbers are negative.
        assertThat(DebeziumTemporal.read("-7140", "DATE")).isEqualTo(LocalDateTime.of(1950, 6, 15, 0, 0));
        assertThat(DebeziumTemporal.read("-157737600000", "TIMESTAMP")).isEqualTo(LocalDateTime.of(1965, 1, 1, 8, 0));
    }

    @Test
    void readsTextFromATimestampColumnOrAConverter() {
        assertThat(DebeziumTemporal.read("2001-12-31T12:34:56Z", "TIMESTAMP")).isEqualTo(MOMENT);
        assertThat(DebeziumTemporal.read("2001-12-31 12:34:56", "TIMESTAMP")).isEqualTo(MOMENT);
        assertThat(DebeziumTemporal.read("2001-12-31", "DATE"))
                .isEqualTo(MOMENT.toLocalDate().atStartOfDay());
        assertThat(DebeziumTemporal.read("12:34:56", "TIME").toLocalTime()).isEqualTo(MOMENT.toLocalTime());
    }

    @Test
    void missingOrUnreadableValuesAreNull() {
        assertThat(DebeziumTemporal.read(null, "DATE")).isNull();
        assertThat(DebeziumTemporal.read(" ", "TIMESTAMP")).isNull();
        assertThat(DebeziumTemporal.read("not a date", "TIMESTAMP")).isNull();
    }
}
