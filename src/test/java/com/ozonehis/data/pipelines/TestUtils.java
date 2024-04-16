/*
 * Copyright (C) Amiyul LLC - All Rights Reserved
 *
 * This source code is protected under international copyright law. All rights
 * reserved and protected by the copyright holder.
 *
 * This file is confidential and only available to authorized individuals with the
 * permission of the copyright holder. If you encounter this file and do not have
 * permission, please contact the copyright holder and delete this file.
 */
package com.ozonehis.data.pipelines;

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
}
