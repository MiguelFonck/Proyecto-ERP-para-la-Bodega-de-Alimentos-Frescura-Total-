package com.frescuratotal.erp.sales.model;

import com.frescuratotal.erp.common.db.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CustomerDao {

    private static final String COLUMNS =
            "id_cliente, nombre, documento, email, telefono, direccion, id_tipo";

    public List<Customer> findAll() {
        List<Customer> customers = new ArrayList<>();
        String sql = "SELECT " + COLUMNS + " FROM clientes ORDER BY id_cliente";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                customers.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.out.println("Error reading customers: " + e.getMessage());
        }
        return customers;
    }

    public Customer findById(int id) {
        String sql = "SELECT " + COLUMNS + " FROM clientes WHERE id_cliente = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (SQLException e) {
            System.out.println("Error finding customer: " + e.getMessage());
        }
        return null;
    }

    public boolean insert(Customer c) {
        String sql = "INSERT INTO clientes (nombre, documento, email, telefono, direccion, id_tipo) "
                + "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, c.name());
            ps.setString(2, c.document());
            ps.setString(3, c.email());
            ps.setString(4, c.phone());
            ps.setString(5, c.address());
            ps.setInt(6, c.typeId());
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println("Error saving customer: " + e.getMessage());
            return false;
        }
    }

    // Converts one row of the table into a Customer
    private Customer mapRow(ResultSet rs) throws SQLException {
        return new Customer(
                rs.getInt("id_cliente"),
                rs.getString("nombre"),
                rs.getString("documento"),
                rs.getString("email"),
                rs.getString("telefono"),
                rs.getString("direccion"),
                rs.getInt("id_tipo"));
    }
}