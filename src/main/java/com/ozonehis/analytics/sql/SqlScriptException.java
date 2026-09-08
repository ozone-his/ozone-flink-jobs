package com.ozonehis.analytics.sql;

/** Thrown when the SQL scripts backing a pipeline cannot be located or read. */
public class SqlScriptException extends RuntimeException {

    public SqlScriptException(String message) {
        super(message);
    }
}
