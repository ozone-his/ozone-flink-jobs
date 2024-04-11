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

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.sql.Connection;
import org.apache.ibatis.jdbc.ScriptRunner;

public class TestUtils {

    public static void executeScript(String filename, Connection connection) {
        final String path =
                TestUtils.class.getClassLoader().getResource(filename).getPath();
        FileReader fileReader;
        try {
            fileReader = new FileReader(path);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }

        ScriptRunner scriptRunner = new ScriptRunner(connection);
        scriptRunner.setSendFullScript(false);
        scriptRunner.setStopOnError(true);
        scriptRunner.runScript(fileReader);
    }
}
