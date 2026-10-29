package com.frescuratotal.erp;

import com.frescuratotal.erp.common.db.DatabaseConnection;
import java.sql.Connection;
import java.sql.SQLException;

public class Main   {
    public static void main(String[] args) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            System.out.println("Connected to the database");
        } catch (SQLException e) {
            System.out.println("Connection error: " + e.getMessage());
        }
    }
}
