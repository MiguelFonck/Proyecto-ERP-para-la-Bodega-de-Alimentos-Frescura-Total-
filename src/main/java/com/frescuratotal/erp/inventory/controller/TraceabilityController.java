package com.frescuratotal.erp.inventory.controller;

import com.frescuratotal.erp.common.util.AppLogger;
import com.frescuratotal.erp.inventory.model.LotSale;
import com.frescuratotal.erp.inventory.model.LotTrace;
import com.frescuratotal.erp.inventory.model.TraceabilityDao;
import com.frescuratotal.erp.inventory.view.InventoryView;

import java.util.List;

public class TraceabilityController {

    private final InventoryView view = new InventoryView();
    private final TraceabilityDao traceabilityDao = new TraceabilityDao();

    public void start() {
        int lotId = -1;
        while (lotId != 0) {
            view.showMessage("\n=== LOT TRACEABILITY ===");
            lotId = view.askInt("Lot id to trace (0 = back): ");
            if (lotId != 0) {
                traceLot(lotId);
            }
        }
    }

    private void traceLot(int lotId) {
        LotTrace lot = traceabilityDao.findLot(lotId);
        if (lot == null) {
            view.showMessage("Lot not found.");
            return;
        }
        AppLogger.info("Lot traced: " + lot.lotNumber());

        view.showMessage("--- Origin ---");
        view.showMessage("Lot: " + lot.lotNumber() + " (id " + lot.lotId() + ")");
        view.showMessage("Product: " + lot.productName());
        view.showMessage("Supplier: " + lot.supplierName() + " | NIT " + lot.supplierNit());
        view.showMessage("Entry date: " + lot.entryDate());
        view.showMessage("Expiry date: " + lot.expiryDate());

        view.showMessage("--- Location and stock ---");
        view.showMessage("Aisle " + lot.aisle() + ", shelf " + lot.shelf());
        view.showMessage("Initial quantity: " + lot.initialQuantity());
        view.showMessage("Remaining quantity: " + lot.remainingQuantity());
        view.showMessage("Status: " + lot.status());

        view.showMessage("--- Sold to ---");
        List<LotSale> sales = traceabilityDao.findSalesOfLot(lotId);
        if (sales.isEmpty()) {
            view.showMessage("This lot has not been sold yet.");
            return;
        }
        sales.forEach(s -> view.showMessage(s.saleNumber() + " | " + s.saleDate() + " | "
                + s.customerName() + " | qty " + s.quantity()));

        // Stream: add up the quantity sold from this lot
        double totalSold = sales.stream()
                .mapToDouble(LotSale::quantity)
                .sum();
        view.showMessage("Total sold from this lot: " + totalSold);
    }
}