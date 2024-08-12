package com.ozonehis.data.pipelines;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ScriptUtils;

public class TestUtils {

    public static void executeScript(String file, Connection connection) {
        ScriptUtils.executeSqlScript(connection, new ClassPathResource(file));
        Boolean autocommit = null;
        try {
            autocommit = connection.getAutoCommit();
            connection.setAutoCommit(false);
            connection.commit();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            if (autocommit != null) {
                try {
                    connection.setAutoCommit(autocommit);
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    public static List<Map<String, Object>> getRows(String table, Connection connection) {
        List<Map<String, Object>> rows = new ArrayList<>();
        try (Statement s = connection.createStatement();
                ResultSet rs = s.executeQuery("SELECT * FROM " + table)) {
            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();
                for (int i = 1; i < rs.getMetaData().getColumnCount() + 1; i++) {
                    row.put(rs.getMetaData().getColumnName(i), rs.getObject(i));
                }

                rows.add(row);
            }

            rs.close();
            return rows;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static Integer getAvailablePort() {
        for (int i = 1024; i < 49151; i++) {
            try {
                new ServerSocket(i).close();
                return i;
            } catch (IOException e) {
                // Port is not available for use
            }
        }

        // Really! No port is available?
        throw new RuntimeException("No available port found");
    }

    public static void createNetworkIfNecessary(String name, String workingDir) {
        try {
            execute("docker network inspect " + name, workingDir);
        } catch (RuntimeException t) {
            // Ignore, network does not exist
            System.out.println("Creating network " + name);
            execute("docker network create -d bridge " + name, workingDir);
            System.out.println("Network created " + name);
        }
    }

    private static void execute(String cmd, String workingDir) {
        ProcessBuilder builder = new ProcessBuilder();
        builder.command("sh", "-c", cmd).directory(new File(workingDir));
        ExecutorService executor = Executors.newFixedThreadPool(2);
        try {
            Process process = builder.start();
            OutputCaptor outputCaptor = new OutputCaptor(process.getInputStream(), System.out::println);
            OutputCaptor errorCaptor = new OutputCaptor(process.getErrorStream(), System.err::println);
            executor.execute(outputCaptor);
            executor.execute(errorCaptor);
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                throw new RuntimeException("An error occurred while executing: " + cmd + ", exit code: " + exitCode);
            }
        } catch (Throwable t) {
            throw new RuntimeException(t);
        } finally {
            executor.shutdownNow();
        }
    }

    private static class OutputCaptor implements Runnable {

        private InputStream inputStream;

        private Consumer<String> consumer;

        public OutputCaptor(InputStream inputStream, Consumer<String> consumer) {
            this.inputStream = inputStream;
            this.consumer = consumer;
        }

        @Override
        public void run() {
            new BufferedReader(new InputStreamReader(inputStream)).lines().forEach(consumer);
        }
    }
}
