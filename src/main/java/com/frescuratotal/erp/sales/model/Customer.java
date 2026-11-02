package com.frescuratotal.erp.sales.model;

public record Customer(int id, String name, String document, String email,
                       String phone, String address, int typeId) {

    public String typeName() {
        return typeId == 1 ? "Wholesale" : "Retail";
    }
}