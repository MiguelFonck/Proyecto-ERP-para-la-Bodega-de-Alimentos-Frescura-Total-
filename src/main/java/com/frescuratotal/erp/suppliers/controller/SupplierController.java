package com.frescuratotal.erp.suppliers.controller;

import com.frescuratotal.erp.common.util.AppLogger;
import com.frescuratotal.erp.suppliers.model.Supplier;
import com.frescuratotal.erp.suppliers.model.SupplierDao;
import com.frescuratotal.erp.suppliers.model.SupplierPurchase;
import com.frescuratotal.erp.suppliers.view.SupplierView;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
            view.showMessage("6. Search suppliers by name");
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
            } else if (option == 6) {
                searchSuppliers();
            }
        }
    }

    private void listSuppliers() {
        List<Supplier> suppliers = supplierDao.findAll();
        suppliers.forEach(s -> view.showMessage(s.id() + " - " + s.name() + " | NIT " + s.nit()
                + " | " + s.contact() + " | " + s.email() + " | " + s.phone()
                + " | " + (s.active() ? "Active" : "Inactive")));
    }

    // Stream: filter by name, sort alphabetically, collect into a list
    private void searchSuppliers() {
        String text = view.askText("Name contains: ").toLowerCase();
        List<Supplier> found = supplierDao.findAll().stream()
                .filter(s -> s.name().toLowerCase().contains(text))
                .sorted(Comparator.comparing(Supplier::name))
                .toList();

        if (found.isEmpty()) {
            view.showMessage("No suppliers match that name.");
            return;
        }
        view.showMessage(found.size() + " supplier(s) found:");
        found.forEach(s -> view.showMessage(s.id() + " - " + s.name() + " | NIT " + s.nit()));
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
            AppLogger.info("Supplier registered: id " + id + ", name " + name);
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
        catalog.forEach(view::showMessage);
    }

    private void addProductToCatalog() {
        Supplier supplier = askSupplier();
        if (supplier == null) {
            return;
        }
        int productId = view.askInt("Product id to add: ");
        if (supplierDao.addProductToCatalog(supplier.id(), productId)) {
            view.showMessage("Product " + productId + " added to " + supplier.name() + ".");
            AppLogger.info("Product " + productId + " added to catalog of supplier " + supplier.id());
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

        purchases.forEach(p -> view.showMessage("Lot " + p.lotId() + " (" + p.lotNumber() + ") | "
                + p.productName() + " | in: " + p.entryDate() + " | expires: " + p.expiryDate()
                + " | qty " + p.initialQuantity() + " x " + p.purchasePrice()
                + " = " + (p.initialQuantity() * p.purchasePrice())));

        // Stream: add up the cost of every lot
        double totalSpent = purchases.stream()
                .mapToDouble(p -> p.initialQuantity() * p.purchasePrice())
                .sum();
        view.showMessage("Total purchased: " + totalSpent);

        // Stream: group the lots by product and add up the cost of each product
        Map<String, Double> totalByProduct = purchases.stream()
                .collect(Collectors.groupingBy(SupplierPurchase::productName,
                        Collectors.summingDouble(p -> p.initialQuantity() * p.purchasePrice())));
        view.showMessage("Total by product:");
        totalByProduct.forEach((product, total) -> view.showMessage("  " + product + ": " + total));
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