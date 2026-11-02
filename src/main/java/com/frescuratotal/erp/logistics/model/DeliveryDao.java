package com.frescuratotal.erp.logistics.model;

import com.frescuratotal.erp.common.db.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class DeliveryDao {

    private static final String ORDER_SELECT =
            "SELECT v.id_venta, v.numero_venta, c.nombre, c.direccion, v.monto_total "
                    + "FROM ventas v JOIN clientes c ON c.id_cliente = v.id_cliente ";

    // ---------- Sales ----------

    // Sales with status 1 (Registrada)
    public List<DispatchOrder> findRegisteredSales() {
        return queryOrders(ORDER_SELECT + "WHERE v.id_estado = 1 ORDER BY v.id_venta");
    }

    // Dispatch panel: sales in status 2 (En Preparacion) that are not in an active route yet
    public List<DispatchOrder> findPendingDispatch() {
        return queryOrders(ORDER_SELECT
                + "WHERE v.id_estado = 2 AND NOT EXISTS ("
                + "SELECT 1 FROM ventas_ruta vr JOIN rutas_entrega r ON r.id_ruta = vr.id_ruta "
                + "WHERE vr.id_venta = v.id_venta AND r.estado <> 'Cancelada') "
                + "ORDER BY v.id_venta");
    }

    // Changes a sale from Registrada (1) to En Preparacion (2)
    public boolean sendToPreparation(int saleId) {
        String sql = "UPDATE ventas SET id_estado = 2 WHERE id_venta = ? AND id_estado = 1";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, saleId);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println("Error updating sale: " + e.getMessage());
            return false;
        }
    }

    private List<DispatchOrder> queryOrders(String sql) {
        List<DispatchOrder> orders = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                orders.add(new DispatchOrder(
                        rs.getInt("id_venta"),
                        rs.getString("numero_venta"),
                        rs.getString("nombre"),
                        rs.getString("direccion"),
                        rs.getDouble("monto_total")));
            }
        } catch (SQLException e) {
            System.out.println("Error reading sales: " + e.getMessage());
        }
        return orders;
    }

    // ---------- Vehicles and drivers ----------

    // Active vehicles that are not in a Planificada or En Transito route
    public List<Vehicle> findFreeVehicles() {
        List<Vehicle> vehicles = new ArrayList<>();
        String sql = "SELECT ve.id_vehiculo, ve.placa, ve.modelo, ve.capacidad FROM vehiculos ve "
                + "WHERE ve.estado = 'Activo' AND NOT EXISTS ("
                + "SELECT 1 FROM asignaciones_entrega a JOIN rutas_entrega r ON r.id_ruta = a.id_ruta "
                + "WHERE a.id_vehiculo = ve.id_vehiculo AND r.estado IN ('Planificada', 'En Transito')) "
                + "ORDER BY ve.id_vehiculo";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                vehicles.add(new Vehicle(
                        rs.getInt("id_vehiculo"),
                        rs.getString("placa"),
                        rs.getString("modelo"),
                        rs.getDouble("capacidad")));
            }
        } catch (SQLException e) {
            System.out.println("Error reading vehicles: " + e.getMessage());
        }
        return vehicles;
    }

    // Active drivers that are not in a Planificada or En Transito route
    public List<Driver> findFreeDrivers() {
        List<Driver> drivers = new ArrayList<>();
        String sql = "SELECT co.id_conductor, co.nombre, co.numero_licencia FROM conductores co "
                + "WHERE co.activo = TRUE AND NOT EXISTS ("
                + "SELECT 1 FROM asignaciones_entrega a JOIN rutas_entrega r ON r.id_ruta = a.id_ruta "
                + "WHERE a.id_conductor = co.id_conductor AND r.estado IN ('Planificada', 'En Transito')) "
                + "ORDER BY co.id_conductor";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                drivers.add(new Driver(
                        rs.getInt("id_conductor"),
                        rs.getString("nombre"),
                        rs.getString("numero_licencia")));
            }
        } catch (SQLException e) {
            System.out.println("Error reading drivers: " + e.getMessage());
        }
        return drivers;
    }

    // ---------- Routes ----------

    public List<DeliveryRoute> findRoutesByStatus(String status) {
        List<DeliveryRoute> routes = new ArrayList<>();
        String sql = "SELECT r.id_ruta, r.nombre_ruta, r.estado, ve.placa, co.nombre "
                + "FROM rutas_entrega r "
                + "JOIN asignaciones_entrega a ON a.id_ruta = r.id_ruta "
                + "JOIN vehiculos ve ON ve.id_vehiculo = a.id_vehiculo "
                + "JOIN conductores co ON co.id_conductor = a.id_conductor "
                + "WHERE r.estado = ? ORDER BY r.id_ruta";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, status);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    routes.add(new DeliveryRoute(
                            rs.getInt(1), rs.getString(2), rs.getString(3),
                            rs.getString(4), rs.getString(5)));
                }
            }
        } catch (SQLException e) {
            System.out.println("Error reading routes: " + e.getMessage());
        }
        return routes;
    }

    // Creates the route, the vehicle/driver assignment and one stop per sale (one transaction)
    public int createRoute(String routeName, String username, int vehicleId,
                           int driverId, List<Integer> saleIds) {
        String routeSql = "INSERT INTO rutas_entrega (nombre_ruta, usuario_creacion, estado) "
                + "VALUES (?, ?, 'Planificada')";
        String assignSql = "INSERT INTO asignaciones_entrega "
                + "(id_ruta, id_vehiculo, id_conductor, usuario_asignacion) VALUES (?, ?, ?, ?)";
        String stopSql = "INSERT INTO ventas_ruta (id_ruta, id_venta, orden_entrega, direccion_destino) "
                + "SELECT ?, v.id_venta, ?, COALESCE(c.direccion, 'No address') "
                + "FROM ventas v JOIN clientes c ON c.id_cliente = v.id_cliente "
                + "WHERE v.id_venta = ?";

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                int routeId;
                try (PreparedStatement ps = conn.prepareStatement(routeSql, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setString(1, routeName);
                    ps.setString(2, username);
                    ps.executeUpdate();
                    try (ResultSet keys = ps.getGeneratedKeys()) {
                        keys.next();
                        routeId = keys.getInt(1);
                    }
                }

                try (PreparedStatement ps = conn.prepareStatement(assignSql)) {
                    ps.setInt(1, routeId);
                    ps.setInt(2, vehicleId);
                    ps.setInt(3, driverId);
                    ps.setString(4, username);
                    ps.executeUpdate();
                }

                int order = 1;
                for (int saleId : saleIds) {
                    try (PreparedStatement ps = conn.prepareStatement(stopSql)) {
                        ps.setInt(1, routeId);
                        ps.setInt(2, order);
                        ps.setInt(3, saleId);
                        ps.executeUpdate();
                    }
                    order++;
                }

                conn.commit();
                return routeId;

            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            System.out.println("Error creating route: " + e.getMessage());
            return -1;
        }
    }

    // Planificada -> En Transito. The sales of the route go from En Preparacion (2) to En Transito (3)
    public boolean startRoute(int routeId) {
        String routeSql = "UPDATE rutas_entrega SET estado = 'En Transito', hora_salida_real = NOW() "
                + "WHERE id_ruta = ? AND estado = 'Planificada'";
        String salesSql = "UPDATE ventas SET id_estado = 3 WHERE id_estado = 2 AND id_venta IN "
                + "(SELECT id_venta FROM ventas_ruta WHERE id_ruta = ?)";

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                int changed;
                try (PreparedStatement ps = conn.prepareStatement(routeSql)) {
                    ps.setInt(1, routeId);
                    changed = ps.executeUpdate();
                }
                if (changed == 0) {
                    conn.rollback();
                    return false;
                }
                try (PreparedStatement ps = conn.prepareStatement(salesSql)) {
                    ps.setInt(1, routeId);
                    ps.executeUpdate();
                }
                conn.commit();
                return true;

            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            System.out.println("Error starting route: " + e.getMessage());
            return false;
        }
    }

    // En Transito -> Completada. The sales go from En Transito (3) to Entregada (4)
    public boolean completeRoute(int routeId, String receivedBy) {
        String routeSql = "UPDATE rutas_entrega SET estado = 'Completada', hora_llegada_real = NOW() "
                + "WHERE id_ruta = ? AND estado = 'En Transito'";
        String stopsSql = "UPDATE ventas_ruta SET fecha_entrega = NOW(), recibido_por = ? "
                + "WHERE id_ruta = ?";
        String salesSql = "UPDATE ventas SET id_estado = 4 WHERE id_estado = 3 AND id_venta IN "
                + "(SELECT id_venta FROM ventas_ruta WHERE id_ruta = ?)";

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                int changed;
                try (PreparedStatement ps = conn.prepareStatement(routeSql)) {
                    ps.setInt(1, routeId);
                    changed = ps.executeUpdate();
                }
                if (changed == 0) {
                    conn.rollback();
                    return false;
                }
                try (PreparedStatement ps = conn.prepareStatement(stopsSql)) {
                    ps.setString(1, receivedBy);
                    ps.setInt(2, routeId);
                    ps.executeUpdate();
                }
                try (PreparedStatement ps = conn.prepareStatement(salesSql)) {
                    ps.setInt(1, routeId);
                    ps.executeUpdate();
                }
                conn.commit();
                return true;

            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            System.out.println("Error completing route: " + e.getMessage());
            return false;
        }
    }
}