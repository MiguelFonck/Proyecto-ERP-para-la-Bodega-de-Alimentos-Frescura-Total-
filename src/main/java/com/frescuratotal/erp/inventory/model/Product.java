package com.frescuratotal.erp.inventory.model;

public class Product {
    private int id;
    private String name;
    private String description;
    private int categoryId;
    private String unit;
    private double basePrice;

    public Product() {
    }

    public Product(int id, String name, String description,
                   int categoryId, String unit, double basePrice) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.categoryId = categoryId;
        this.unit = unit;
        this.basePrice = basePrice;
    }

    // getters and setters here

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public double getBasePrice() {
        return basePrice;
    }

    public void setBasePrice(double basePrice) {
        this.basePrice = basePrice;
    }
}