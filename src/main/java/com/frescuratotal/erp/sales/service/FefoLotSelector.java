package com.frescuratotal.erp.sales.service;

import com.frescuratotal.erp.inventory.model.Lot;
import com.frescuratotal.erp.inventory.model.LotDao;
import com.frescuratotal.erp.sales.model.LotAllocation;

import java.util.ArrayList;
import java.util.List;

public class FefoLotSelector {

    private final LotDao lotDao = new LotDao();

    public double availableStock(int productId) {
        double total = 0;
        for (Lot lot : lotDao.findSellableByProduct(productId)) {
            total += lot.getQuantity();
        }
        return total;
    }

    // Takes from the lot that expires first, then the next one, until the quantity is covered.
    // Returns an empty list if there is not enough stock.
    public List<LotAllocation> select(int productId, double quantity) {
        List<LotAllocation> result = new ArrayList<>();
        double remaining = quantity;

        for (Lot lot : lotDao.findSellableByProduct(productId)) {
            if (remaining <= 0.0001) {
                break;
            }
            double take = Math.min(remaining, lot.getQuantity());
            result.add(new LotAllocation(lot, take));
            remaining -= take;
        }

        if (remaining > 0.0001) {
            return new ArrayList<>();
        }
        return result;
    }
}