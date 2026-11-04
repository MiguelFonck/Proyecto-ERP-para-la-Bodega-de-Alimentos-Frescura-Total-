package com.frescuratotal.erp.inventory.model;

import com.frescuratotal.erp.common.db.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class TraceabilityDao {

    // Origin, location and stock of one lot. Returns null if the lot does not exist.
    public LotTrace findLot(int lotId) {
        String sql = "SELECT l.id_lote, l.numero_lote, p.nombre AS producto, "
                + "pr.nombre AS proveedor, pr.nit, l.fecha_ingreso, l.fecha_vencimiento, "
                + "l.cantidad_inicial, l.cantidad, l.pasillo, l.estante, e.nombre AS estado "
                + "FROM lotes_inventario l "
                + "JOIN productos p ON p.id_producto = l.id_producto "
                + "JOIN proveedores pr ON pr.id_proveedor = l.id_proveedor "
                + "JOIN estados_lote e ON e.id_estado = l.id_estado "
                + "WHERE l.id_lote = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, lotId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new LotTrace(
                            rs.getInt("id_lote"),
                            rs.getString("numero_lote"),
                            rs.getString("producto"),
                            rs.getString("proveedor"),
                            rs.getString("nit"),
                            rs.getString("fecha_ingreso"),
                            rs.getString("fecha_vencimiento"),
                            rs.getDouble("cantidad_inicial"),
                            rs.getDouble("cantidad"),
                            rs.getString("pasillo"),
                            rs.getString("estante"),
                            rs.getString("estado"));
                }
            }
        } catch (SQLException e) {
            System.out.println("Error reading lot: " + e.getMessage());
        }
        return null;
    }

    // Sales that took stock from this lot, newest first
    public List<LotSale> findSalesOfLot(int lotId) {
        List<LotSale> sales = new ArrayList<>();
        String sql = "SELECT v.numero_venta, v.fecha_venta, c.nombre, d.cantidad_vendida "
                + "FROM detalles_venta d "
                + "JOIN ventas v ON v.id_venta = d.id_venta "
                + "JOIN clientes c ON c.id_cliente = v.id_cliente "
                + "WHERE d.id_lote = ? AND v.id_estado <> 5 ORDER BY v.fecha_venta DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, lotId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    sales.add(new LotSale(
                            rs.getString("numero_venta"),
                            rs.getString("fecha_venta"),
                            rs.getString("nombre"),
                            rs.getDouble("cantidad_vendida")));
                }
            }
        } catch (SQLException e) {
            System.out.println("Error reading sales of lot: " + e.getMessage());
        }
        return sales;
    }
}