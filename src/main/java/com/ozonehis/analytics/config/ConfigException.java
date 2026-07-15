package com.ozonehis.analytics.config;

/** Thrown when the analytics configuration is missing, malformed or internally inconsistent. */
public class ConfigException extends RuntimeException {

    public ConfigException(String message) {
        super(message);
    }

    public ConfigException(String message, Throwable cause) {
        super(message, cause);
    }
}
