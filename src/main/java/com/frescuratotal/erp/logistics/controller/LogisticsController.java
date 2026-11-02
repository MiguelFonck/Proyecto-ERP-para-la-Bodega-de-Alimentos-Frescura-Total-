package com.frescuratotal.erp.logistics.controller;

import com.frescuratotal.erp.logistics.model.DeliveryDao;
import com.frescuratotal.erp.logistics.model.DeliveryRoute;
import com.frescuratotal.erp.logistics.model.DispatchOrder;
import com.frescuratotal.erp.logistics.model.Driver;
import com.frescuratotal.erp.logistics.model.Vehicle;
import com.frescuratotal.erp.logistics.view.LogisticsView;

import java.util.ArrayList;
import java.util.List;

public class LogisticsController {

    private final LogisticsView view = new LogisticsView();
    private final DeliveryDao deliveryDao = new DeliveryDao();

    public void start() {
        int option = -1;
        while (option != 0) {
            view.showMessage("\n=== LOGISTICS MENU ===");
            view.showMessage("1. Send a registered sale to preparation");
            view.showMessage("2. Dispatch panel (sales in preparation)");
            view.showMessage("3. Create delivery route");
            view.showMessage("4. Start route (In Transit)");
            view.showMessage("5. Complete route (Delivered)");
            view.showMessage("0. Back");
            option = view.askInt("Option: ");

            if (option == 1) {
                sendToPreparation();
            } else if (option == 2) {
                showDispatchPanel();
            } else if (option == 3) {
                createRoute();
            } else if (option == 4) {
                startRoute();
            } else if (option == 5) {
                completeRoute();
            }
        }
    }

    private void sendToPreparation() {
        List<DispatchOrder> registered = deliveryDao.findRegisteredSales();
        if (registered.isEmpty()) {
            view.showMessage("There are no registered sales.");
            return;
        }
        showOrders(registered);
        int saleId = view.askInt("Sale id to send to preparation (0 = cancel): ");
        if (saleId == 0) {
            return;
        }
        if (deliveryDao.sendToPreparation(saleId)) {
            view.showMessage("Sale " + saleId + " is now In Preparation.");
        } else {
            view.showMessage("Sale not found or it is not in Registered status.");
        }
    }

    private void showDispatchPanel() {
        view.showMessage("=== DISPATCH PANEL ===");
        List<DispatchOrder> pending = deliveryDao.findPendingDispatch();
        if (pending.isEmpty()) {
            view.showMessage("No sales waiting for a route.");
        } else {
            showOrders(pending);
        }
    }

    private void createRoute() {
        List<DispatchOrder> pending = deliveryDao.findPendingDispatch();
        if (pending.isEmpty()) {
            view.showMessage("No sales waiting for a route.");
            return;
        }
        view.showMessage("=== Create delivery route ===");
        showOrders(pending);

        // 1. Choose the sales
        List<Integer> chosen = new ArrayList<>();
        while (true) {
            int saleId = view.askInt("Sale id to add (0 = finish): ");
            if (saleId == 0) {
                break;
            }
            if (!orderIsInList(pending, saleId)) {
                view.showMessage("That sale is not in the dispatch panel.");
            } else if (chosen.contains(saleId)) {
                view.showMessage("That sale is already in the route.");
            } else {
                chosen.add(saleId);
            }
        }
        if (chosen.isEmpty()) {
            view.showMessage("No sales selected. Cancelled.");
            return;
        }

        // 2. Choose the vehicle
        List<Vehicle> vehicles = deliveryDao.findFreeVehicles();
        if (vehicles.isEmpty()) {
            view.showMessage("There are no free vehicles.");
            return;
        }
        view.showMessage("Free vehicles:");
        for (Vehicle v : vehicles) {
            view.showMessage(v.id() + " - " + v.plate() + " | " + v.model() + " | capacity " + v.capacity());
        }
        int vehicleId = view.askInt("Vehicle id: ");
        boolean vehicleFound = false;
        for (Vehicle v : vehicles) {
            if (v.id() == vehicleId) {
                vehicleFound = true;
            }
        }
        if (!vehicleFound) {
            view.showMessage("That vehicle is not available.");
            return;
        }

        // 3. Choose the driver
        List<Driver> drivers = deliveryDao.findFreeDrivers();
        if (drivers.isEmpty()) {
            view.showMessage("There are no free drivers.");
            return;
        }
        view.showMessage("Free drivers:");
        for (Driver d : drivers) {
            view.showMessage(d.id() + " - " + d.name() + " | license " + d.license());
        }
        int driverId = view.askInt("Driver id: ");
        boolean driverFound = false;
        for (Driver d : drivers) {
            if (d.id() == driverId) {
                driverFound = true;
            }
        }
        if (!driverFound) {
            view.showMessage("That driver is not available.");
            return;
        }

        // 4. Save
        String routeName = view.askText("Route name: ");
        String username = view.askText("Your name: ");
        int routeId = deliveryDao.createRoute(routeName, username, vehicleId, driverId, chosen);
        if (routeId > 0) {
            view.showMessage("Route " + routeId + " created with " + chosen.size() + " sale(s).");
        } else {
            view.showMessage("Could not create the route. Nothing was changed.");
        }
    }

    private void startRoute() {
        List<DeliveryRoute> routes = deliveryDao.findRoutesByStatus("Planificada");
        if (routes.isEmpty()) {
            view.showMessage("There are no planned routes.");
            return;
        }
        showRoutes(routes);
        int routeId = view.askInt("Route id to start (0 = cancel): ");
        if (routeId == 0) {
            return;
        }
        if (deliveryDao.startRoute(routeId)) {
            view.showMessage("Route " + routeId + " is now In Transit. Its sales are In Transit.");
        } else {
            view.showMessage("Route not found or it is not planned.");
        }
    }

    private void completeRoute() {
        List<DeliveryRoute> routes = deliveryDao.findRoutesByStatus("En Transito");
        if (routes.isEmpty()) {
            view.showMessage("There are no routes in transit.");
            return;
        }
        showRoutes(routes);
        int routeId = view.askInt("Route id to complete (0 = cancel): ");
        if (routeId == 0) {
            return;
        }
        String receivedBy = view.askText("Received by: ");
        if (deliveryDao.completeRoute(routeId, receivedBy)) {
            view.showMessage("Route " + routeId + " completed. Its sales are Delivered.");
        } else {
            view.showMessage("Route not found or it is not in transit.");
        }
    }

    private void showOrders(List<DispatchOrder> orders) {
        for (DispatchOrder o : orders) {
            view.showMessage(o.saleId() + " - " + o.saleNumber() + " | " + o.customerName()
                    + " | " + o.address() + " | total " + o.total());
        }
    }

    private void showRoutes(List<DeliveryRoute> routes) {
        for (DeliveryRoute r : routes) {
            view.showMessage(r.id() + " - " + r.name() + " | vehicle " + r.plate()
                    + " | driver " + r.driverName());
        }
    }

    private boolean orderIsInList(List<DispatchOrder> orders, int saleId) {
        for (DispatchOrder o : orders) {
            if (o.saleId() == saleId) {
                return true;
            }
        }
        return false;
    }
}