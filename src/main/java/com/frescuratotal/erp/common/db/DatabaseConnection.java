package com.frescuratotal.erp.common.db;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DatabaseConnection {

    private static final String CONFIG_FILE = "config.properties";

    public static Connection getConnection() throws SQLException {
        Properties config = new Properties();

        try (FileInputStream file = new FileInputStream(CONFIG_FILE)) {
            config.load(file);
        } catch (IOException e) {
            throw new SQLException("Could not read " + CONFIG_FILE
                    + ". Copy config.properties.example to config.properties and fill it in.", e);
        }

        String url = config.getProperty("db.url");
        String user = config.getProperty("db.user");
        String password = config.getProperty("db.password");

        return DriverManager.getConnection(url, user, password);
    }
}