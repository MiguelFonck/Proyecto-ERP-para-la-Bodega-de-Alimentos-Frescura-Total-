package com.frescuratotal.erp.sales.model;

import com.frescuratotal.erp.common.db.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

public class InvoiceDao {

    public Company findCompany() {
        String sql = "SELECT nombre_empresa, nit, direccion, ciudad, telefono, email "
                + "FROM informacion_empresa LIMIT 1";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                return new Company(
                        rs.getString("nombre_empresa"),
                        rs.getString("nit"),
                        rs.getString("direccion"),
                        rs.getString("ciudad"),
                        rs.getString("telefono"),
                        rs.getString("email"));
            }
        } catch (SQLException e) {
            System.out.println("Error reading company: " + e.getMessage());
        }
        return null;
    }

    public boolean insert(int saleId, String invoiceNumber, String cufe, String qrLink, Sale sale) {
        String sql = "INSERT INTO facturas (id_venta, numero_factura, cufe, enlace_qr, "
                + "subtotal, total_descuento, monto_impuesto, monto_total) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, saleId);
            ps.setString(2, invoiceNumber);
            ps.setString(3, cufe);
            ps.setString(4, qrLink);
            ps.setDouble(5, sale.subtotal());
            ps.setDouble(6, sale.totalDiscount());
            ps.setDouble(7, sale.totalTax());
            ps.setDouble(8, sale.total());
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println("Error saving invoice: " + e.getMessage());
            return false;
        }
    }

    // Finds the invoice already saved for a sale, or null if the sale has no invoice
    public Invoice findBySaleId(int saleId) {
        String sql = "SELECT numero_factura, cufe, enlace_qr, fecha_emision FROM facturas WHERE id_venta = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, saleId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Timestamp issued = rs.getTimestamp("fecha_emision");
                    return new Invoice(
                            rs.getString("numero_factura"),
                            rs.getString("cufe"),
                            rs.getString("enlace_qr"),
                            issued != null ? issued.toLocalDateTime() : null);
                }
            }
        } catch (SQLException e) {
            System.out.println("Error reading invoice: " + e.getMessage());
        }
        return null;
    }
}
