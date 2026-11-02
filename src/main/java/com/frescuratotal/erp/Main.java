package com.frescuratotal.erp;

import com.frescuratotal.erp.common.util.ConsoleView;
import com.frescuratotal.erp.inventory.controller.InventoryController;
import com.frescuratotal.erp.sales.controller.SalesController;

public class Main {
    public static void main(String[] args) {
        ConsoleView view = new ConsoleView();
        int option = -1;
        while (option != 0) {
            view.showMessage("\n=== FRESCURA TOTAL ERP ===");
            view.showMessage("1. Inventory");
            view.showMessage("2. Sales");
            view.showMessage("0. Exit");
            option = view.askInt("Option: ");

            if (option == 1) {
                new InventoryController().start();
            } else if (option == 2) {
                new SalesController().start();
            }
        }
    }
}