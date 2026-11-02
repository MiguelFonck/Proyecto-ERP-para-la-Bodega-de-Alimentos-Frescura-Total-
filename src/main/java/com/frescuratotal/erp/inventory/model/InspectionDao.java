package com.frescuratotal.erp.inventory.model;

import com.frescuratotal.erp.common.db.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class InspectionDao {

    // result must be exactly "Aprobado" or "Rechazado" (the ENUM of the table)
    public boolean insert(int lotId, String result, String inspectorName,
                          String rejectionReason, String notes) {
        String sql = "INSERT INTO inspecciones_calidad "
                + "(id_lote, resultado, nombre_inspector, motivo_rechazo, notas) "
                + "VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, lotId);
            ps.setString(2, result);
            ps.setString(3, inspectorName);
            ps.setString(4, rejectionReason);
            ps.setString(5, notes);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println("Error saving inspection: " + e.getMessage());
            return false;
        }
    }
}