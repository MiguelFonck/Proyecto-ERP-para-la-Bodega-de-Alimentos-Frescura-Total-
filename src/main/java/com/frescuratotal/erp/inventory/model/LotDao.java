package com.frescuratotal.erp.inventory.model;

import com.frescuratotal.erp.common.db.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class LotDao {

    public boolean insert(Lot lot) {
        String sql = "INSERT INTO lotes_inventario "
                + "(numero_lote, id_producto, id_proveedor, fecha_vencimiento, "
                + "cantidad_inicial, cantidad, precio_compra, precio_venta, "
                + "pasillo, estante, id_estado) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, lot.getLotNumber());
            ps.setInt(2, lot.getProductId());
            ps.setInt(3, lot.getSupplierId());
            ps.setDate(4, java.sql.Date.valueOf(lot.getExpiryDate()));
            ps.setDouble(5, lot.getInitialQuantity());
            ps.setDouble(6, lot.getQuantity());
            ps.setDouble(7, lot.getPurchasePrice());
            ps.setDouble(8, lot.getSalePrice());
            ps.setString(9, lot.getAisle());
            ps.setString(10, lot.getShelf());
            ps.setInt(11, lot.getStatus().getDbId());

            ps.executeUpdate();
            return true;

        } catch (SQLException e) {
            System.out.println("Error inserting lot: " + e.getMessage());
            return false;
        }
    }
    public String generateLotNumber() {
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String prefix = "L-" + today + "-";
        String sql = "SELECT COUNT(*) FROM lotes_inventario WHERE numero_lote LIKE ?";
        int count = 0;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, prefix + "%");
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    count = rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            System.out.println("Error generating lot number: " + e.getMessage());
        }

        return prefix + String.format("%04d", count + 1);
    }
}