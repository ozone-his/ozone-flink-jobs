package com.ozonehis.analytics.sql;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

/** Loads {@code .sql} files from a directory. */
public final class SqlScripts {

    private SqlScripts() {}

    /**
     * Loads every {@code .sql} file directly under {@code directory}, ordered by file name.
     *
     * <p>The ordering is deliberate: the filesystem returns entries in an arbitrary order, which
     * would make the generated SQL — and therefore the Flink job graph — differ between runs on
     * identical inputs.
     *
     * @throws SqlScriptException if the directory is missing or unreadable, or contains no scripts
     */
    public static List<SqlScript> loadAll(Path directory) {
        if (!Files.isDirectory(directory)) {
            throw new SqlScriptException("SQL directory not found: " + directory.toAbsolutePath());
        }

        try (Stream<Path> entries = Files.list(directory)) {
            List<SqlScript> scripts = entries.filter(Files::isRegularFile)
                    .filter(p -> p.getFileName().toString().endsWith(".sql"))
                    .sorted(Comparator.comparing(p -> p.getFileName().toString()))
                    .map(SqlScripts::read)
                    .toList();

            if (scripts.isEmpty()) {
                throw new SqlScriptException("No .sql files found in: " + directory.toAbsolutePath());
            }

            return scripts;
        } catch (IOException e) {
            throw new UncheckedIOException("Unable to list SQL directory: " + directory.toAbsolutePath(), e);
        }
    }

    private static SqlScript read(Path path) {
        try {
            return new SqlScript(
                    baseName(path.getFileName().toString()), Files.readString(path, StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new UncheckedIOException("Unable to read SQL file: " + path.toAbsolutePath(), e);
        }
    }

    private static String baseName(String fileName) {
        int dot = fileName.lastIndexOf('.');
        return dot == -1 ? fileName : fileName.substring(0, dot);
    }
}
