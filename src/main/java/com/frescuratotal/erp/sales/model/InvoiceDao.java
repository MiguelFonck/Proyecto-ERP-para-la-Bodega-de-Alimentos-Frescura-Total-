package com.frescuratotal.erp.sales.model;

import com.frescuratotal.erp.common.db.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

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
}