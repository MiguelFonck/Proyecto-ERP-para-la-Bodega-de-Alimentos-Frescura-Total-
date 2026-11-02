package com.frescuratotal.erp.suppliers.model;

public record Supplier(int id, String name, String nit, String contact,
                       String email, String phone, String address, boolean active) {
}