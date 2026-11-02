package com.frescuratotal.erp.inventory.model;

public record LotTrace(int lotId, String lotNumber, String productName,
                       String supplierName, String supplierNit,
                       String entryDate, String expiryDate,
                       double initialQuantity, double remainingQuantity,
                       String aisle, String shelf, String status) {
}