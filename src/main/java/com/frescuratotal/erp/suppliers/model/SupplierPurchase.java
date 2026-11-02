package com.frescuratotal.erp.suppliers.model;

public record SupplierPurchase(int lotId, String lotNumber, String productName,
                               String entryDate, String expiryDate,
                               double initialQuantity, double purchasePrice) {
}