package com.frescuratotal.erp.sales.model;

public record SaleItem(int lotId, String lotNumber, String productName, double quantity,
                       double unitPrice, double discountRate, double discountAmount,
                       double taxRate, double taxAmount, double subtotal, double lineTotal) {
}