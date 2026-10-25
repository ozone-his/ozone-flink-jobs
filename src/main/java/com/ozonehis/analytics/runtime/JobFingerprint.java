package com.ozonehis.analytics.runtime;

import com.ozonehis.analytics.pipeline.Job;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A short hash of what a streaming job runs, so the runner can tell a job running the current query
 * from one left over from an earlier version of it.
 *
 * <p>It covers the INSERT and the definitions of the tables that INSERT mentions, connector options
 * included. A table counts as mentioned when its name appears as a word anywhere in the INSERT, so
 * an alias or column that happens to share a table's name only makes the hash cover one table too
 * many. Every job registers every source table, and hashing them all would replace all jobs
 * whenever any one table definition changed.
 *
 * <p>Comments and spacing are left out, so editing a comment does not replace a job and make it
 * rebuild its table from scratch. Optimizer hints ({@code /*+ ... *}{@code /}) are kept: they change
 * how the job runs.
 */
public final class JobFingerprint {

    /** Hex characters kept from the hash. Eight are plenty to tell versions of one job apart. */
    static final int LENGTH = 8;

    private static final Pattern TABLE_NAME =
            Pattern.compile("(?i)CREATE\\s+TABLE\\s+(?:IF\\s+NOT\\s+EXISTS\\s+)?`?([\\w.]+)`?");

    private JobFingerprint() {}

    public static String of(Job job) {
        String insert = normalize(job.insertStatement());
        StringBuilder covered = new StringBuilder(insert);
        for (String definition : job.tableDefinitions()) {
            Matcher name = TABLE_NAME.matcher(definition);
            if (!name.find() || mentions(insert, name.group(1))) {
                covered.append('\n').append(normalize(definition));
            }
        }
        return sha256(covered.toString()).substring(0, LENGTH);
    }

    private static boolean mentions(String sql, String table) {
        return Pattern.compile("(?i)(?<![\\w])" + Pattern.quote(table) + "(?![\\w])")
                .matcher(sql)
                .find();
    }

    /**
     * Drops comments, except optimizer hints, and collapses every run of whitespace to one space.
     * Text inside single-quoted string literals is kept exactly.
     */
    static String normalize(String sql) {
        StringBuilder out = new StringBuilder(sql.length());
        int i = 0;
        boolean pendingSpace = false;
        while (i < sql.length()) {
            char c = sql.charAt(i);
            if (c == '\'') {
                int end = i + 1;
                while (end < sql.length()) {
                    if (sql.charAt(end) == '\'') {
                        if (end + 1 < sql.length() && sql.charAt(end + 1) == '\'') {
                            end += 2;
                            continue;
                        }
                        break;
                    }
                    end++;
                }
                end = Math.min(end + 1, sql.length());
                if (pendingSpace && out.length() > 0) out.append(' ');
                pendingSpace = false;
                out.append(sql, i, end);
                i = end;
            } else if (sql.startsWith("--", i)) {
                int end = sql.indexOf('\n', i);
                i = end < 0 ? sql.length() : end;
                pendingSpace = true;
            } else if (sql.startsWith("/*", i) && !sql.startsWith("/*+", i)) {
                int end = sql.indexOf("*/", i + 2);
                i = end < 0 ? sql.length() : end + 2;
                pendingSpace = true;
            } else if (Character.isWhitespace(c)) {
                i++;
                pendingSpace = true;
            } else {
                if (pendingSpace && out.length() > 0) out.append(' ');
                pendingSpace = false;
                out.append(c);
                i++;
            }
        }
        return out.toString();
    }

    private static String sha256(String text) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(text.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is required of every Java runtime", e);
        }
    }
}
