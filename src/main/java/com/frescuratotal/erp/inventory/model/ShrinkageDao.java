package com.frescuratotal.erp.inventory.model;

import com.frescuratotal.erp.common.db.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

public class ShrinkageDao {

    // Saves a shrinkage record. Returns the new id, or -1 if it failed.
    public int insert(int lotId, double quantityLost, String reason, String username) {
        String sql = "INSERT INTO registro_mermas "
                + "(id_lote, cantidad_perdida, motivo, usuario_registro) VALUES (?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, lotId);
            ps.setDouble(2, quantityLost);
            ps.setString(3, reason);
            ps.setString(4, username);
            ps.executeUpdate();

            try (var keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
        } catch (SQLException e) {
            System.out.println("Error saving shrinkage: " + e.getMessage());
        }
        return -1;
    }
}