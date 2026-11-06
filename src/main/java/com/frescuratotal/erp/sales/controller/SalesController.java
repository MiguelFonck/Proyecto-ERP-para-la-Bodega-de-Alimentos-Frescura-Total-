package com.frescuratotal.erp.sales.controller;

import com.frescuratotal.erp.common.util.AppLogger;
import com.frescuratotal.erp.inventory.model.Product;
import com.frescuratotal.erp.inventory.model.ProductDao;
import com.frescuratotal.erp.inventory.service.ExpiryCheckService;
import com.frescuratotal.erp.sales.model.Customer;
import com.frescuratotal.erp.sales.model.CustomerDao;
import com.frescuratotal.erp.sales.model.LotAllocation;
import com.frescuratotal.erp.sales.model.Sale;
import com.frescuratotal.erp.sales.model.SaleDao;
import com.frescuratotal.erp.sales.model.SaleItem;
import com.frescuratotal.erp.sales.service.DiscountService;
import com.frescuratotal.erp.sales.service.FefoLotSelector;
import com.frescuratotal.erp.sales.view.SalesView;
import com.frescuratotal.erp.sales.service.InvoiceGenerator;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SalesController {

    private final SalesView view = new SalesView();
    private final CustomerDao customerDao = new CustomerDao();
    private final ProductDao productDao = new ProductDao();
    private final SaleDao saleDao = new SaleDao();
    private final FefoLotSelector fefoSelector = new FefoLotSelector();
    private final DiscountService discountService = new DiscountService();
    private final InvoiceGenerator invoiceGenerator = new InvoiceGenerator();

    public void start() {
        // Simulates the daily process: refresh lot statuses before selling
        new ExpiryCheckService().checkExpiry();

        int option = -1;
        while (option != 0) {
            view.showMessage("\n=== SALES MENU ===");
            view.showMessage("1. New sale");
            view.showMessage("2. Register customer");
            view.showMessage("3. List customers");
            view.showMessage("4. List products with stock");
            view.showMessage("5. Print invoice of an existing sale");
            view.showMessage("0. Back");
            option = view.askInt("Option: ");

            if (option == 1) {
                newSale();
            } else if (option == 2) {
                registerCustomer();
            } else if (option == 3) {
                listCustomers();
            } else if (option == 4) {
                listProducts();
            } else if (option == 5) {
                printInvoice();
            }
        }
    }

    // Creates (again) the invoice PNG of a sale that is already saved in the database
    private void printInvoice() {
        int saleId = view.askInt("Sale id: ");
        Sale sale = saleDao.findById(saleId);
        if (sale == null) {
            view.showMessage("Sale not found.");
            return;
        }
        Customer customer = customerDao.findById(sale.customerId());
        List<SaleItem> items = saleDao.findItems(saleId);
        if (customer == null || items.isEmpty()) {
            view.showMessage("The sale has no customer or no products. Cannot print the invoice.");
            return;
        }

        String path = invoiceGenerator.reprint(saleId, sale, customer, items);
        if (path != null) {
            view.showMessage("Invoice created: " + path);
            AppLogger.info("Invoice printed: " + path);
        } else {
            view.showMessage("The invoice could not be created.");
        }
    }

        private void listCustomers() {
        for (Customer c : customerDao.findAll()) {
            view.showMessage(c.id() + " - " + c.name() + " (" + c.typeName() + ")");
        }
    }

    private void listProducts() {
        for (Product p : productDao.findAll()) {
            double stock = fefoSelector.availableStock(p.getId());
            view.showMessage(p.getId() + " - " + p.getName() + " | stock: " + stock + " " + p.getUnit());
        }
    }

    private void registerCustomer() {
        view.showMessage("=== Register customer ===");
        String name = view.askText("Name: ");
        String document = view.askText("Document (NIT or ID): ");
        String email = view.askText("Email: ");
        String phone = view.askText("Phone: ");
        String address = view.askText("Address: ");
        int typeId = view.askInt("Type (1 = Wholesale, 2 = Retail): ");

        if (typeId != 1 && typeId != 2) {
            view.showMessage("Invalid type.");
            return;
        }
        Customer customer = new Customer(0, name, document, email, phone, address, typeId);
        view.showMessage(customerDao.insert(customer) ? "Customer saved." : "Could not save the customer.");
    }

    private void newSale() {
        view.showMessage("=== New sale ===");

        Customer customer = null;
        while (customer == null) {
            int id = view.askInt("Customer id (0 = show list, -1 = cancel): ");
            if (id == -1) {
                return;
            }
            if (id == 0) {
                listCustomers();
                continue;
            }
            customer = customerDao.findById(id);
            if (customer == null) {
                view.showMessage("Customer not found.");
            }
        }

        List<SaleItem> items = new ArrayList<>();
        Set<Integer> productsInSale = new HashSet<>();

        while (true) {
            int productId = view.askInt("Product id (0 = finish, -1 = show products): ");
            if (productId == 0) {
                break;
            }
            if (productId == -1) {
                listProducts();
                continue;
            }
            Product product = productDao.findById(productId);
            if (product == null) {
                view.showMessage("Product not found.");
                continue;
            }
            if (productsInSale.contains(productId)) {
                view.showMessage("That product is already in the sale.");
                continue;
            }

            double available = fefoSelector.availableStock(productId);
            view.showMessage(product.getName() + " - available: " + available + " " + product.getUnit());
            double quantity = view.askDouble("Quantity: ");
            if (quantity <= 0) {
                view.showMessage("Invalid quantity.");
                continue;
            }

            List<LotAllocation> allocations = fefoSelector.select(productId, quantity);
            if (allocations.isEmpty()) {
                view.showMessage("Not enough stock. Available: " + available);
                continue;
            }

            for (LotAllocation a : allocations) {
                SaleItem item = discountService.buildItem(a.lot(), product.getName(), a.quantity());
                items.add(item);
                String promo = item.discountRate() > 0 ? " [50% OFF - near expiry]" : "";
                view.showMessage("  Lot " + item.lotNumber() + " (expires " + a.lot().getExpiryDate() + "): "
                        + item.quantity() + " x " + item.unitPrice() + " = " + item.lineTotal() + promo);
            }
            productsInSale.add(productId);
        }

        if (items.isEmpty()) {
            view.showMessage("The sale has no products. Cancelled.");
            return;
        }

        double subtotal = 0, discount = 0, tax = 0, total = 0;
        for (SaleItem item : items) {
            subtotal += item.subtotal();
            discount += item.discountAmount();
            tax += item.taxAmount();
            total += item.lineTotal();
        }
        view.showMessage("\nCustomer: " + customer.name());
        view.showMessage("Subtotal: " + round(subtotal));
        view.showMessage("Discount: " + round(discount));
        view.showMessage("Tax (19%): " + round(tax));
        view.showMessage("TOTAL: " + round(total));

        int confirm = view.askInt("Confirm sale? (1 = yes, 2 = no): ");
        if (confirm != 1) {
            view.showMessage("Sale cancelled.");
            AppLogger.info("Sale cancelled by the user");
            return;
        }

        String username = view.askText("Salesperson name: ");
        Sale sale = new Sale(0, saleDao.generateSaleNumber(), customer.id(),
                round(subtotal), round(discount), round(tax), round(total), 1, username);

        int saleId = saleDao.save(sale, items);
        if (saleId > 0) {
            view.showMessage("Sale saved: " + sale.saleNumber() + ". Stock updated.");
            AppLogger.info("Sale saved: " + sale.saleNumber());
            String path = invoiceGenerator.generate(saleId, sale, customer, items);
            if (path != null) {
                view.showMessage("Invoice created: " + path);
                AppLogger.info("Invoice created: " + path);
            } else {
                view.showMessage("The sale was saved, but the invoice could not be created.");
            }
        } else {
            view.showMessage("Could not save the sale. Nothing was changed.");
        }
    }

    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}