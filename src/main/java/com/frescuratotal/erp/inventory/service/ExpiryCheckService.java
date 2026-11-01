package com.frescuratotal.erp.inventory.service;

import com.frescuratotal.erp.inventory.model.Lot;
import com.frescuratotal.erp.inventory.model.LotDao;
import com.frescuratotal.erp.inventory.model.LotStatus;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

public class ExpiryCheckService {

    private final LotDao lotDao = new LotDao();

    public int checkExpiry() {
        LocalDate today = LocalDate.now();
        List<Lot> lots = lotDao.findAll();
        int changes = 0;

        for (Lot lot : lots) {
            if (lot.getStatus() == LotStatus.IN_QUARANTINE
                    || lot.getStatus() == LotStatus.EXPIRED) {
                continue;
            }

            long days = ChronoUnit.DAYS.between(today, lot.getExpiryDate());

            LotStatus newStatus;
            if (days < 0) {
                newStatus = LotStatus.EXPIRED;
            } else if (days <= 7) {
                newStatus = LotStatus.NEAR_EXPIRY;
            } else {
                newStatus = LotStatus.AVAILABLE;
            }

            if (newStatus != lot.getStatus()) {
                lotDao.updateStatus(lot.getId(), newStatus);
                changes++;
            }
        }
        return changes;
    }
}