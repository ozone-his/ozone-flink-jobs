package com.ozonehis.analytics.sql;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class ConnectorOptionsTest {

    @Test
    void rendersOptionsAsQuotedPairsInInsertionOrder() {
        String sql = ConnectorOptions.of("kafka")
                .set("topic", "emr.openmrs.patient")
                .set("scan.startup.mode", "group-offsets")
                .appendTo("CREATE TABLE patient (id INT)");

        assertThat(sql).isEqualTo("""
                        CREATE TABLE patient (id INT)
                        WITH (
                          'connector' = 'kafka',
                          'topic' = 'emr.openmrs.patient',
                          'scan.startup.mode' = 'group-offsets'
                        )""");
    }

    @Test
    void escapesSingleQuotesSoCredentialsCannotTerminateTheLiteral() {
        String sql = ConnectorOptions.of("jdbc").set("password", "pa'ss").appendTo("CREATE TABLE t (id INT)");

        // The quote is doubled, keeping it inside the literal rather than closing it early.
        assertThat(sql).contains("'password' = 'pa''ss'");
    }

    @Test
    void rejectsNullValueRatherThanEmittingTheStringNull() {
        assertThatThrownBy(() -> ConnectorOptions.of("jdbc").set("password", null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("password");
    }

    @Test
    void laterValueForSameKeyWins() {
        String sql = ConnectorOptions.of("jdbc")
                .set("url", "first")
                .set("url", "second")
                .appendTo("CREATE TABLE t ()");

        assertThat(sql).contains("'url' = 'second'").doesNotContain("first");
    }
}
