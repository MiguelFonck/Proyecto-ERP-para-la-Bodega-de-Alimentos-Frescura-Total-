package com.frescuratotal.erp.sales.service;

import com.frescuratotal.erp.inventory.model.Lot;
import com.frescuratotal.erp.inventory.model.LotStatus;
import com.frescuratotal.erp.sales.model.SaleItem;

public class DiscountService {

    private static final double NEAR_EXPIRY_DISCOUNT = 0.50;
    private static final double TAX_RATE = 0.19;

    public SaleItem buildItem(Lot lot, String productName, double quantity) {
        double rate = (lot.getStatus() == LotStatus.NEAR_EXPIRY) ? NEAR_EXPIRY_DISCOUNT : 0.0;
        double price = lot.getSalePrice();

        double subtotal = round(quantity * price);
        double discount = round(subtotal * rate);
        double tax = round((subtotal - discount) * TAX_RATE);
        double lineTotal = round(subtotal - discount + tax);

        return new SaleItem(lot.getId(), lot.getLotNumber(), productName, quantity,
                price, rate, discount, TAX_RATE, tax, subtotal, lineTotal);
    }

    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}