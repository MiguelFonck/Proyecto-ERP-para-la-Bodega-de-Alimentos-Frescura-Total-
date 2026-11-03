package com.frescuratotal.erp.common.db;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DatabaseConnection {

    private static final String CONFIG_FILE = "config.properties";

    public static Connection getConnection() throws SQLException {
        Properties config = loadConfig();

        String url = config.getProperty("db.url");
        String user = config.getProperty("db.user");
        String password = config.getProperty("db.password");

        return DriverManager.getConnection(url, user, password);
    }

    private static Properties loadConfig() throws SQLException {
        Properties config = new Properties();
        File file = new File(CONFIG_FILE);

        try {
            if (file.exists()) {
                // 1. In the folder the program runs from (project root)
                try (FileInputStream in = new FileInputStream(file)) {
                    config.load(in);
                }
            } else {
                // 2. Inside src/main/resources (classpath)
                try (InputStream in = DatabaseConnection.class.getClassLoader()
                        .getResourceAsStream(CONFIG_FILE)) {
                    if (in == null) {
                        throw new SQLException("Could not find " + CONFIG_FILE + ". I looked in: "
                                + file.getAbsolutePath() + " and in the classpath. "
                                + "Copy config.properties.example to config.properties and fill it in.");
                    }
                    config.load(in);
                }
            }
        } catch (IOException e) {
            throw new SQLException("Could not read " + CONFIG_FILE + ": " + e.getMessage(), e);
        }
        return config;
    }
}