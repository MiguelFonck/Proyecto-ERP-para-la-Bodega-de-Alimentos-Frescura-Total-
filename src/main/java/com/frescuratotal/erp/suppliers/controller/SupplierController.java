package com.frescuratotal.erp.suppliers.controller;

import com.frescuratotal.erp.suppliers.model.Supplier;
import com.frescuratotal.erp.suppliers.model.SupplierDao;
import com.frescuratotal.erp.suppliers.model.SupplierPurchase;
import com.frescuratotal.erp.suppliers.view.SupplierView;

import java.util.List;

public class SupplierController {

    private final SupplierView view = new SupplierView();
    private final SupplierDao supplierDao = new SupplierDao();

    public void start() {
        int option = -1;
        while (option != 0) {
            view.showMessage("\n=== SUPPLIERS MENU ===");
            view.showMessage("1. List suppliers");
            view.showMessage("2. Register supplier");
            view.showMessage("3. Supplier catalog (products they sell)");
            view.showMessage("4. Add product to a supplier catalog");
            view.showMessage("5. Supplier purchase history (lots received)");
            view.showMessage("0. Back");
            option = view.askInt("Option: ");

            if (option == 1) {
                listSuppliers();
            } else if (option == 2) {
                registerSupplier();
            } else if (option == 3) {
                showCatalog();
            } else if (option == 4) {
                addProductToCatalog();
            } else if (option == 5) {
                showPurchases();
            }
        }
    }

    private void listSuppliers() {
        List<Supplier> suppliers = supplierDao.findAll();
        for (Supplier s : suppliers) {
            String state = s.active() ? "Active" : "Inactive";
            view.showMessage(s.id() + " - " + s.name() + " | NIT " + s.nit()
                    + " | " + s.contact() + " | " + s.email() + " | " + s.phone() + " | " + state);
        }
    }

    private void registerSupplier() {
        view.showMessage("=== Register supplier ===");
        String name = view.askText("Name: ");
        String nit = view.askText("NIT: ");
        String contact = view.askText("Contact person: ");
        String email = view.askText("Email: ");
        String phone = view.askText("Phone: ");
        String address = view.askText("Address: ");

        int id = supplierDao.insert(name, nit, contact, email, phone, address);
        if (id > 0) {
            view.showMessage("Supplier saved with id " + id + ".");
        } else {
            view.showMessage("Could not save the supplier (is the NIT already registered?).");
        }
    }

    private void showCatalog() {
        Supplier supplier = askSupplier();
        if (supplier == null) {
            return;
        }
        List<String> catalog = supplierDao.findCatalog(supplier.id());
        view.showMessage("Catalog of " + supplier.name() + ":");
        if (catalog.isEmpty()) {
            view.showMessage("This supplier has no products in its catalog.");
        }
        for (String line : catalog) {
            view.showMessage(line);
        }
    }

    private void addProductToCatalog() {
        Supplier supplier = askSupplier();
        if (supplier == null) {
            return;
        }
        int productId = view.askInt("Product id to add: ");
        if (supplierDao.addProductToCatalog(supplier.id(), productId)) {
            view.showMessage("Product " + productId + " added to " + supplier.name() + ".");
        } else {
            view.showMessage("Nothing was added (does the product exist? is it already in the catalog?).");
        }
    }

    private void showPurchases() {
        Supplier supplier = askSupplier();
        if (supplier == null) {
            return;
        }
        List<SupplierPurchase> purchases = supplierDao.findPurchases(supplier.id());
        view.showMessage("Purchases from " + supplier.name() + ":");
        if (purchases.isEmpty()) {
            view.showMessage("No lots received from this supplier.");
            return;
        }
        double totalSpent = 0;
        for (SupplierPurchase p : purchases) {
            double lineTotal = p.initialQuantity() * p.purchasePrice();
            totalSpent += lineTotal;
            view.showMessage("Lot " + p.lotId() + " (" + p.lotNumber() + ") | " + p.productName()
                    + " | in: " + p.entryDate() + " | expires: " + p.expiryDate()
                    + " | qty " + p.initialQuantity() + " x " + p.purchasePrice()
                    + " = " + lineTotal);
        }
        view.showMessage("Total purchased: " + totalSpent);
    }

    // Asks for a supplier id and returns the supplier, or null if it does not exist
    private Supplier askSupplier() {
        int id = view.askInt("Supplier id: ");
        Supplier supplier = supplierDao.findById(id);
        if (supplier == null) {
            view.showMessage("Supplier not found.");
        }
        return supplier;
    }
}