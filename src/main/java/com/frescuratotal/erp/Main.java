package com.frescuratotal.erp;

import com.frescuratotal.erp.inventory.controller.InventoryController;
import com.frescuratotal.erp.inventory.model.Lot;
import com.frescuratotal.erp.inventory.model.LotDao;
import com.frescuratotal.erp.inventory.service.ExpiryCheckService;

public class Main {
    public static void main(String[] args) {
        new InventoryController().start();
    }
}