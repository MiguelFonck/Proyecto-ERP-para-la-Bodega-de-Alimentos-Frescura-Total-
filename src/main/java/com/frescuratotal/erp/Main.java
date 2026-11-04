package com.frescuratotal.erp;

import com.frescuratotal.erp.common.util.AppLogger;
import com.frescuratotal.erp.common.util.ConsoleView;
import com.frescuratotal.erp.inventory.controller.InventoryController;
import com.frescuratotal.erp.inventory.controller.TraceabilityController;
import com.frescuratotal.erp.logistics.controller.LogisticsController;
import com.frescuratotal.erp.sales.controller.SalesController;
import com.frescuratotal.erp.suppliers.controller.SupplierController;

public class Main {
    public static void main(String[] args) {
        AppLogger.info("Application started");
        ConsoleView view = new ConsoleView();
        int option = -1;
        while (option != 0) {
            view.showMessage("\n=== FRESCURA TOTAL ERP ===");
            view.showMessage("1. Inventory");
            view.showMessage("2. Sales");
            view.showMessage("3. Logistics");
            view.showMessage("4. Suppliers");
            view.showMessage("5. Lot traceability");
            view.showMessage("0. Exit");
            option = view.askInt("Option: ");

            if (option == 1) {
                new InventoryController().start();
            } else if (option == 2) {
                new SalesController().start();
            } else if (option == 3) {
                new LogisticsController().start();
            } else if (option == 4) {
                new SupplierController().start();
            } else if (option == 5) {
                new TraceabilityController().start();
            }
        }
        AppLogger.info("Application closed");
    }
}