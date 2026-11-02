package com.frescuratotal.erp.sales.model;

import com.frescuratotal.erp.inventory.model.Lot;

public record LotAllocation(Lot lot, double quantity) {
}