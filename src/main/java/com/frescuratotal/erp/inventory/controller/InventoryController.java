package com.frescuratotal.erp.inventory.controller;

import com.frescuratotal.erp.inventory.model.Lot;
import com.frescuratotal.erp.inventory.model.LotDao;
import com.frescuratotal.erp.inventory.model.LotStatus;
import com.frescuratotal.erp.inventory.view.InventoryView;

public class InventoryController {

    private final InventoryView view = new InventoryView();
    private final LotDao lotDao = new LotDao();

    public void receiveMerchandise() {
        view.showMessage("=== Receive merchandise ===");

        int productId = view.askInt("Product id: ");
        int supplierId = view.askInt("Supplier id: ");
        double quantity = view.askDouble("Quantity received: ");
        var expiryDate = view.askDate("Expiry date (yyyy-mm-dd): ");
        double purchasePrice = view.askDouble("Purchase price: ");
        double salePrice = view.askDouble("Sale price: ");
        String aisle = view.askText("Aisle: ");
        String shelf = view.askText("Shelf: ");

        Lot lot = new Lot();
        lot.setLotNumber(lotDao.generateLotNumber());
        lot.setProductId(productId);
        lot.setSupplierId(supplierId);
        lot.setExpiryDate(expiryDate);
        lot.setInitialQuantity(quantity);
        lot.setQuantity(quantity);
        lot.setPurchasePrice(purchasePrice);
        lot.setSalePrice(salePrice);
        lot.setAisle(aisle);
        lot.setShelf(shelf);
        lot.setStatus(LotStatus.AVAILABLE);

        if (lotDao.insert(lot)) {
            view.showMessage("Lot saved with number: " + lot.getLotNumber());
        } else {
            view.showMessage("Could not save the lot.");
        }
    }
}