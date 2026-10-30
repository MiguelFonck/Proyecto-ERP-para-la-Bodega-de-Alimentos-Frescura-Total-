package com.frescuratotal.erp.inventory.model;

public enum LotStatus {
    AVAILABLE(1, "Disponible"),
    NEAR_EXPIRY(2, "Proximo a Vencer"),
    EXPIRED(3, "Vencido"),
    IN_QUARANTINE(4, "En Cuarentena");

    private final int dbId;
    private final String dbName;

    LotStatus(int dbId, String dbName) {
        this.dbId = dbId;
        this.dbName = dbName;
    }

    public int getDbId() { return dbId; }
    public String getDbName() { return dbName; }

    public static LotStatus fromDbId(int id) {
        for (LotStatus s : values()) {
            if (s.dbId == id) return s;
        }
        throw new IllegalArgumentException("Unknown status id: " + id);
    }
}