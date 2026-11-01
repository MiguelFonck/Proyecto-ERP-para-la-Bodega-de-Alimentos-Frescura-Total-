package com.frescuratotal.erp;

import com.frescuratotal.erp.inventory.model.*;

import java.time.LocalDate;

public class Main {
    public static void main(String[] args) {
        Lot lot = new Lot();

        LotDao dao = new LotDao();                  // Automatizacion de
        lot.setLotNumber(dao.generateLotNumber());  // el numero de lote

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

        System.out.println(dao.insert(lot) ? "Lot saved!" : "Could not save lot");
    }
}