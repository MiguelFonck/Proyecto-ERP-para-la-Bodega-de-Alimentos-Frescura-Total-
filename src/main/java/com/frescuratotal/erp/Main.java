package com.frescuratotal.erp;

import com.frescuratotal.erp.inventory.model.Product;
import com.frescuratotal.erp.inventory.model.ProductDao;

public class Main {
    public static void main(String[] args) {
        ProductDao dao = new ProductDao();
        for (Product p : dao.findAll()) {
            System.out.println(p.getId() + " - " + p.getName());
        }
    }
}