package com.frescuratotal.erp.sales.model;

import java.time.LocalDateTime;

public record Invoice(String invoiceNumber, String cufe, String qrLink, LocalDateTime issuedAt) {
}
