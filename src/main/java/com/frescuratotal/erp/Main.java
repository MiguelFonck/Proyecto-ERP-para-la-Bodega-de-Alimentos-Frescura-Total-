package com.frescuratotal.erp;

import com.frescuratotal.erp.inventory.model.*;

import java.time.LocalDate;

public class Main {
    public static void main(String[] args) {
        Lot lot = new Lot();
        lot.setLotNumber("L-20261101-0001");
        lot.setProductId(1);
        lot.setSupplierId(1);
        lot.setExpiryDate(LocalDate.of(2026, 11, 10));
        lot.setInitialQuantity(200);
        lot.setQuantity(200);
        lot.setPurchasePrice(2500);
        lot.setSalePrice(3500);
        lot.setAisle("A");
        lot.setShelf("05");
        lot.setStatus(LotStatus.AVAILABLE);

        LotDao dao = new LotDao();
        System.out.println(dao.insert(lot) ? "Lot saved!" : "Could not save lot");
    }
}