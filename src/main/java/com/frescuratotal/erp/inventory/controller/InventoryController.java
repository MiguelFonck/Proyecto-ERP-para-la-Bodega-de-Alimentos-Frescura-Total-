package com.frescuratotal.erp.inventory.controller;

import com.frescuratotal.erp.inventory.model.*;
import com.frescuratotal.erp.inventory.service.ExpiryCheckService;
import com.frescuratotal.erp.inventory.view.InventoryView;

public class InventoryController {

    private final InventoryView view = new InventoryView();
    private final LotDao lotDao = new LotDao();
    private final QuarantineDao quarantineDao = new QuarantineDao();
    private final ShrinkageDao shrinkageDao = new ShrinkageDao();
    private final InspectionDao inspectionDao = new InspectionDao();

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

    public void putInQuarantine() {
        view.showMessage("=== Put lot in quarantine ===");
        int lotId = view.askInt("Lot id: ");
        String reason = view.askText("Reason (damaged package, bad smell...): ");
        String username = view.askText("Your name: ");

        if (lotDao.updateStatus(lotId, LotStatus.IN_QUARANTINE)) {
            quarantineDao.insert(lotId, reason, username);
            view.showMessage("Lot " + lotId + " is now in quarantine.");
        } else {
            view.showMessage("Lot not found.");
        }
    }

    public void registerShrinkage() {
        view.showMessage("=== Register shrinkage ===");
        int lotId = view.askInt("Lot id: ");
        Lot lot = lotDao.findById(lotId);
        if (lot == null) {
            view.showMessage("Lot not found.");
            return;
        }
        double quantity = view.askDouble("Quantity lost: ");
        if (quantity <= 0 || quantity > lot.getQuantity()) {
            view.showMessage("Invalid quantity. The lot has " + lot.getQuantity() + ".");
            return;
        }
        String reason = view.askText("Reason (expired, damaged...): ");
        String username = view.askText("Your name: ");

        shrinkageDao.insert(lotId, quantity, reason, username);
        lotDao.updateQuantity(lotId, lot.getQuantity() - quantity);
        view.showMessage("Shrinkage registered.");
    }

    public void inspectLot() {
        view.showMessage("=== Quality inspection ===");
        int lotId = view.askInt("Lot id: ");
        Lot lot = lotDao.findById(lotId);
        if (lot == null || lot.getStatus() != LotStatus.IN_QUARANTINE) {
            view.showMessage("Lot not found or not in quarantine.");
            return;
        }
        String inspector = view.askText("Inspector name: ");
        int decision = view.askInt("Decision (1 = Approve, 2 = Reject): ");
        String notes = view.askText("Notes: ");

        if (decision == 1) {
            inspectionDao.insert(lotId, "Aprobado", inspector, null, notes);
            quarantineDao.markResolved(lotId, notes);
            lotDao.updateStatus(lotId, LotStatus.AVAILABLE);
            view.showMessage("Lot approved. It is AVAILABLE again.");
        } else if (decision == 2) {
            inspectionDao.insert(lotId, "Rechazado", inspector, notes, notes);
            shrinkageDao.insert(lotId, lot.getQuantity(), "Rejected in inspection: " + notes, inspector);
            quarantineDao.markResolved(lotId, notes);
            lotDao.updateQuantity(lotId, 0);
            lotDao.updateStatus(lotId, LotStatus.EXPIRED);
            view.showMessage("Lot rejected. Registered as shrinkage and blocked.");
        } else {
            view.showMessage("Invalid option.");
        }
    }

    public void start() {
        ExpiryCheckService expiryService = new ExpiryCheckService();
        int option = -1;
        while (option != 0) {
            view.showMessage("\n=== INVENTORY MENU ===");
            view.showMessage("1. Receive merchandise");
            view.showMessage("2. Run expiry check");
            view.showMessage("3. Put lot in quarantine");
            view.showMessage("4. Register shrinkage");
            view.showMessage("5. Quality inspection");
            view.showMessage("0. Exit");
            option = view.askInt("Option: ");

            if (option == 1) {
                receiveMerchandise();
            } else if (option == 2) {
                view.showMessage("Lots updated: " + expiryService.checkExpiry());
            } else if (option == 3) {
                putInQuarantine();
            } else if (option == 4) {
                registerShrinkage();
            } else if (option == 5) {
                inspectLot();
            }
        }
    }
}