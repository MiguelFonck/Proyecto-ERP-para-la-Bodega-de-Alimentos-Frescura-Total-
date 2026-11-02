package com.frescuratotal.erp.logistics.model;

public record DispatchOrder(int saleId, String saleNumber, String customerName,
                            String address, double total) {
}