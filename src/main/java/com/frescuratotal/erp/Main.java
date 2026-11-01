package com.frescuratotal.erp;

import com.frescuratotal.erp.inventory.controller.InventoryController;
import com.frescuratotal.erp.inventory.model.Lot;
import com.frescuratotal.erp.inventory.model.LotDao;
import com.frescuratotal.erp.inventory.service.ExpiryCheckService;

public class Main {
    public static void main(String[] args) {
        ExpiryCheckService service = new ExpiryCheckService();
        int changed = service.checkExpiry();
        System.out.println("Lots updated: " + changed);

        LotDao dao = new LotDao();
        for (Lot lot : dao.findAll()) {
            System.out.println(lot.getLotNumber() + " - " + lot.getExpiryDate() + " - " + lot.getStatus());
        }
    }
}