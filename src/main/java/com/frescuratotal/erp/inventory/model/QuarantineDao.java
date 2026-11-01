package com.frescuratotal.erp.inventory.model;

import com.frescuratotal.erp.common.db.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class QuarantineDao {

    // Saves the quarantine record (INSERT)
    public boolean insert(int lotId, String reason, String username) {
        String sql = "INSERT INTO registro_cuarentena (id_lote, motivo, usuario) VALUES (?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, lotId);
            ps.setString(2, reason);
            ps.setString(3, username);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println("Error saving quarantine record: " + e.getMessage());
            return false;
        }
    }

    // Marks the open quarantine record of a lot as resolved (UPDATE)
    public boolean markResolved(int lotId, String notes) {
        String sql = "UPDATE registro_cuarentena "
                + "SET resuelta = TRUE, fecha_resolucion = NOW(), notas_resolucion = ? "
                + "WHERE id_lote = ? AND resuelta = FALSE";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, notes);
            ps.setInt(2, lotId);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println("Error resolving quarantine: " + e.getMessage());
            return false;
        }
    }
}