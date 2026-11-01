package com.frescuratotal.erp.inventory.model;

import com.frescuratotal.erp.common.db.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

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

    public List<Lot> findAll() {
        List<Lot> lots = new ArrayList<>();
        String sql = "SELECT id_lote, numero_lote, id_producto, id_proveedor, "
                + "fecha_ingreso, fecha_vencimiento, cantidad_inicial, cantidad, "
                + "precio_compra, precio_venta, pasillo, estante, id_estado "
                + "FROM lotes_inventario";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Lot lot = new Lot();
                lot.setId(rs.getInt("id_lote"));
                lot.setLotNumber(rs.getString("numero_lote"));
                lot.setProductId(rs.getInt("id_producto"));
                lot.setSupplierId(rs.getInt("id_proveedor"));
                lot.setEntryDate(rs.getTimestamp("fecha_ingreso").toLocalDateTime());
                lot.setExpiryDate(rs.getDate("fecha_vencimiento").toLocalDate());
                lot.setInitialQuantity(rs.getDouble("cantidad_inicial"));
                lot.setQuantity(rs.getDouble("cantidad"));
                lot.setPurchasePrice(rs.getDouble("precio_compra"));
                lot.setSalePrice(rs.getDouble("precio_venta"));
                lot.setAisle(rs.getString("pasillo"));
                lot.setShelf(rs.getString("estante"));
                lot.setStatus(LotStatus.fromDbId(rs.getInt("id_estado")));
                lots.add(lot);
            }
        } catch (SQLException e) {
            System.out.println("Error reading lots: " + e.getMessage());
        }
        return lots;
    }

    public boolean updateStatus(int lotId, LotStatus newStatus) {
        String sql = "UPDATE lotes_inventario SET id_estado = ? WHERE id_lote = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, newStatus.getDbId());
            ps.setInt(2, lotId);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println("Error updating lot status: " + e.getMessage());
            return false;
        }
    }

    public boolean updateQuantity(int lotId, double newQuantity) {
        String sql = "UPDATE lotes_inventario SET cantidad = ? WHERE id_lote = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setDouble(1, newQuantity);
            ps.setInt(2, lotId);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println("Error updating quantity: " + e.getMessage());
            return false;
        }
    }

    public Lot findById(int lotId) {
        for (Lot lot : findAll()) {
            if (lot.getId() == lotId) {
                return lot;
            }
        }
        return null;
    }
}