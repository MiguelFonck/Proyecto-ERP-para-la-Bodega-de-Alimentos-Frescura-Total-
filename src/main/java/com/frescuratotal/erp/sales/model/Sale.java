package com.frescuratotal.erp.sales.model;

public record Sale(int id, String saleNumber, int customerId, double subtotal,
                   double totalDiscount, double totalTax, double total,
                   int statusId, String username) {
}