package com.frescuratotal.erp.sales.model;

import com.frescuratotal.erp.common.db.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class SaleDao {

    private static final int STATUS_REGISTERED = 1;

    public String generateSaleNumber() {
        int year = LocalDate.now().getYear();
        String prefix = "V-" + year + "-";
        String sql = "SELECT COUNT(*) FROM ventas WHERE numero_venta LIKE ?";
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
            System.out.println("Error generating sale number: " + e.getMessage());
        }
        return prefix + String.format("%04d", count + 1);
    }

    // Saves the sale, its lines and discounts the stock. Returns the new sale id, or -1.
    public int save(Sale sale, List<SaleItem> items) {
        String saleSql = "INSERT INTO ventas (numero_venta, id_cliente, subtotal, total_descuento, "
                + "total_impuesto, monto_total, id_estado, usuario_registro) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        String detailSql = "INSERT INTO detalles_venta (id_venta, id_lote, cantidad_vendida, "
                + "precio_unitario, descuento_aplicado, monto_descuento, tasa_impuesto, "
                + "monto_impuesto, subtotal, total_linea) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        String stockSql = "UPDATE lotes_inventario SET cantidad = cantidad - ? "
                + "WHERE id_lote = ? AND cantidad >= ?";

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);   // start the transaction
            try {
                int saleId;
                try (PreparedStatement ps = conn.prepareStatement(saleSql, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setString(1, sale.saleNumber());
                    ps.setInt(2, sale.customerId());
                    ps.setDouble(3, sale.subtotal());
                    ps.setDouble(4, sale.totalDiscount());
                    ps.setDouble(5, sale.totalTax());
                    ps.setDouble(6, sale.total());
                    ps.setInt(7, STATUS_REGISTERED);
                    ps.setString(8, sale.username());
                    ps.executeUpdate();
                    try (ResultSet keys = ps.getGeneratedKeys()) {
                        keys.next();
                        saleId = keys.getInt(1);
                    }
                }

                for (SaleItem item : items) {
                    try (PreparedStatement ps = conn.prepareStatement(detailSql)) {
                        ps.setInt(1, saleId);
                        ps.setInt(2, item.lotId());
                        ps.setDouble(3, item.quantity());
                        ps.setDouble(4, item.unitPrice());
                        ps.setDouble(5, item.discountRate());
                        ps.setDouble(6, item.discountAmount());
                        ps.setDouble(7, item.taxRate());
                        ps.setDouble(8, item.taxAmount());
                        ps.setDouble(9, item.subtotal());
                        ps.setDouble(10, item.lineTotal());
                        ps.executeUpdate();
                    }
                    try (PreparedStatement ps = conn.prepareStatement(stockSql)) {
                        ps.setDouble(1, item.quantity());
                        ps.setInt(2, item.lotId());
                        ps.setDouble(3, item.quantity());
                        if (ps.executeUpdate() == 0) {
                            throw new SQLException("Not enough stock in lot " + item.lotNumber());
                        }
                    }
                }

                conn.commit();   // everything went well: save for real
                return saleId;

            } catch (SQLException e) {
                conn.rollback(); // something failed: undo everything
                throw e;
            }
        } catch (SQLException e) {
            System.out.println("Error saving sale: " + e.getMessage());
            return -1;
        }
    }

    // Finds a saved sale by its id, or null if it does not exist
    public Sale findById(int saleId) {
        String sql = "SELECT id_venta, numero_venta, id_cliente, subtotal, total_descuento, "
                + "total_impuesto, monto_total, id_estado, usuario_registro FROM ventas WHERE id_venta = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, saleId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Sale(
                            rs.getInt("id_venta"),
                            rs.getString("numero_venta"),
                            rs.getInt("id_cliente"),
                            rs.getDouble("subtotal"),
                            rs.getDouble("total_descuento"),
                            rs.getDouble("total_impuesto"),
                            rs.getDouble("monto_total"),
                            rs.getInt("id_estado"),
                            rs.getString("usuario_registro"));
                }
            }
        } catch (SQLException e) {
            System.out.println("Error finding sale: " + e.getMessage());
        }
        return null;
    }

    // Reads the lines of a saved sale (with lot number and product name)
    public List<SaleItem> findItems(int saleId) {
        List<SaleItem> items = new ArrayList<>();
        String sql = "SELECT d.id_lote, l.numero_lote, p.nombre, d.cantidad_vendida, d.precio_unitario, "
                + "d.descuento_aplicado, d.monto_descuento, d.tasa_impuesto, d.monto_impuesto, "
                + "d.subtotal, d.total_linea "
                + "FROM detalles_venta d "
                + "JOIN lotes_inventario l ON l.id_lote = d.id_lote "
                + "JOIN productos p ON p.id_producto = l.id_producto "
                + "WHERE d.id_venta = ? ORDER BY d.id_detalle";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, saleId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    items.add(new SaleItem(
                            rs.getInt("id_lote"),
                            rs.getString("numero_lote"),
                            rs.getString("nombre"),
                            rs.getDouble("cantidad_vendida"),
                            rs.getDouble("precio_unitario"),
                            rs.getDouble("descuento_aplicado"),
                            rs.getDouble("monto_descuento"),
                            rs.getDouble("tasa_impuesto"),
                            rs.getDouble("monto_impuesto"),
                            rs.getDouble("subtotal"),
                            rs.getDouble("total_linea")));
                }
            }
        } catch (SQLException e) {
            System.out.println("Error reading sale lines: " + e.getMessage());
        }
        return items;
    }
}
