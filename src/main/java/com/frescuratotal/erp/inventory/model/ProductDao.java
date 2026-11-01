package com.frescuratotal.erp.inventory.model;

import com.frescuratotal.erp.common.db.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ProductDao {

    public List<Product> findAll() {
        List<Product> products = new ArrayList<>();
        String sql = "SELECT id_producto, nombre, descripcion, id_categoria, "
                + "unidad_medida, precio_base FROM productos";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                products.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.out.println("Error reading products: " + e.getMessage());
        }
        return products;
    }

    public Product findById(int id) {
        String sql = "SELECT id_producto, nombre, descripcion, id_categoria, "
                + "unidad_medida, precio_base FROM productos WHERE id_producto = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (SQLException e) {
            System.out.println("Error finding product: " + e.getMessage());
        }
        return null;
    }

    // Converts one row of the table into a Product object
    private Product mapRow(ResultSet rs) throws SQLException {
        return new Product(
                rs.getInt("id_producto"),
                rs.getString("nombre"),
                rs.getString("descripcion"),
                rs.getInt("id_categoria"),
                rs.getString("unidad_medida"),
                rs.getDouble("precio_base")
        );
    }
}