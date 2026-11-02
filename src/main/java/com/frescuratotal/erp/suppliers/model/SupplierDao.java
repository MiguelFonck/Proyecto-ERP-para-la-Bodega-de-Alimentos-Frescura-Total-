package com.frescuratotal.erp.suppliers.model;

import com.frescuratotal.erp.common.db.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class SupplierDao {

    public List<Supplier> findAll() {
        List<Supplier> suppliers = new ArrayList<>();
        String sql = "SELECT id_proveedor, nombre, nit, persona_contacto, email, telefono, "
                + "direccion, activo FROM proveedores ORDER BY id_proveedor";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                suppliers.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.out.println("Error reading suppliers: " + e.getMessage());
        }
        return suppliers;
    }

    public Supplier findById(int id) {
        String sql = "SELECT id_proveedor, nombre, nit, persona_contacto, email, telefono, "
                + "direccion, activo FROM proveedores WHERE id_proveedor = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (SQLException e) {
            System.out.println("Error reading supplier: " + e.getMessage());
        }
        return null;
    }

    // Returns the new supplier id, or -1 if something failed (for example, a repeated NIT)
    public int insert(String name, String nit, String contact, String email,
                      String phone, String address) {
        String sql = "INSERT INTO proveedores (nombre, nit, persona_contacto, email, telefono, direccion) "
                + "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, name);
            ps.setString(2, nit);
            ps.setString(3, contact);
            ps.setString(4, email);
            ps.setString(5, phone);
            ps.setString(6, address);
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
        } catch (SQLException e) {
            System.out.println("Error saving supplier: " + e.getMessage());
        }
        return -1;
    }

    // Products that this supplier sells (catalog). Each line: "id - name"
    public List<String> findCatalog(int supplierId) {
        List<String> catalog = new ArrayList<>();
        String sql = "SELECT p.id_producto, p.nombre FROM productos_proveedor pp "
                + "JOIN productos p ON p.id_producto = pp.id_producto "
                + "WHERE pp.id_proveedor = ? ORDER BY p.id_producto";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, supplierId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    catalog.add(rs.getInt(1) + " - " + rs.getString(2));
                }
            }
        } catch (SQLException e) {
            System.out.println("Error reading catalog: " + e.getMessage());
        }
        return catalog;
    }

    public boolean addProductToCatalog(int supplierId, int productId) {
        String sql = "INSERT INTO productos_proveedor (id_proveedor, id_producto) VALUES (?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, supplierId);
            ps.setInt(2, productId);
            ps.executeUpdate();
            return true;

        } catch (SQLException e) {
            System.out.println("Could not add product: " + e.getMessage());
            return false;
        }
    }

    // Purchase history = lots received from this supplier
    public List<SupplierPurchase> findPurchases(int supplierId) {
        List<SupplierPurchase> purchases = new ArrayList<>();
        String sql = "SELECT l.id_lote, l.numero_lote, p.nombre, l.fecha_ingreso, "
                + "l.fecha_vencimiento, l.cantidad_inicial, l.precio_compra "
                + "FROM lotes_inventario l JOIN productos p ON p.id_producto = l.id_producto "
                + "WHERE l.id_proveedor = ? ORDER BY l.fecha_ingreso DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, supplierId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    purchases.add(new SupplierPurchase(
                            rs.getInt("id_lote"),
                            rs.getString("numero_lote"),
                            rs.getString("nombre"),
                            rs.getString("fecha_ingreso"),
                            rs.getString("fecha_vencimiento"),
                            rs.getDouble("cantidad_inicial"),
                            rs.getDouble("precio_compra")));
                }
            }
        } catch (SQLException e) {
            System.out.println("Error reading purchases: " + e.getMessage());
        }
        return purchases;
    }

    private Supplier mapRow(ResultSet rs) throws SQLException {
        return new Supplier(
                rs.getInt("id_proveedor"),
                rs.getString("nombre"),
                rs.getString("nit"),
                rs.getString("persona_contacto"),
                rs.getString("email"),
                rs.getString("telefono"),
                rs.getString("direccion"),
                rs.getBoolean("activo"));
    }
}