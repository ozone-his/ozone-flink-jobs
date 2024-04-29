package com.ozonehis.data.pipelines;

import java.io.IOException;
import java.net.ServerSocket;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
}
