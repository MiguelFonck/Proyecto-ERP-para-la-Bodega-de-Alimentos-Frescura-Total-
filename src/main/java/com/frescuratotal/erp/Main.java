package com.frescuratotal.erp;

import com.frescuratotal.erp.inventory.controller.InventoryController;

public class Main {
    public static void main(String[] args) {
        InventoryController controller = new InventoryController();
        controller.receiveMerchandise();
    }
}