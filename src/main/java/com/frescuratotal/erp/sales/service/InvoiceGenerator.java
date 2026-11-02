package com.frescuratotal.erp.sales.service;

import com.frescuratotal.erp.sales.model.Company;
import com.frescuratotal.erp.sales.model.Customer;
import com.frescuratotal.erp.sales.model.InvoiceDao;
import com.frescuratotal.erp.sales.model.Sale;
import com.frescuratotal.erp.sales.model.SaleItem;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

public class InvoiceGenerator {

    private static final String OUTPUT_FOLDER = "invoices";
    private final InvoiceDao invoiceDao = new InvoiceDao();

    // Saves the invoice in the database and creates the PNG. Returns the file path, or null if it failed.
    public String generate(int saleId, Sale sale, Customer customer, List<SaleItem> items) {
        Company company = invoiceDao.findCompany();
        if (company == null) {
            return null;
        }

        String invoiceNumber = "FE-" + String.format("%06d", saleId);
        String cufe = buildCufe(sale.saleNumber() + company.nit() + customer.document() + sale.total());
        String qrLink = "https://catalogo-vpfe.dian.gov.co/document/searchqr?documentkey=" + cufe;

        if (!invoiceDao.insert(saleId, invoiceNumber, cufe, qrLink, sale)) {
            return null;
        }

        BufferedImage image = draw(company, sale, customer, items, invoiceNumber, cufe, qrLink);

        File folder = new File(OUTPUT_FOLDER);
        folder.mkdirs();
        File file = new File(folder, invoiceNumber + ".png");
        try {
            ImageIO.write(image, "png", file);
            return file.getPath();
        } catch (IOException e) {
            System.out.println("Error writing invoice image: " + e.getMessage());
            return null;
        }
    }

    // Simulated CUFE: a SHA-384 code (96 hex characters) built from the sale data
    private String buildCufe(String text) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-384");
            byte[] hash = digest.digest(text.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (byte b : hash) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-384 is not available", e);
        }
    }

    private BufferedImage draw(Company company, Sale sale, Customer customer, List<SaleItem> items,
                               String invoiceNumber, String cufe, String qrLink) {
        int width = 850;
        int height = 720 + items.size() * 26;

        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, width, height);
        g.setColor(Color.BLACK);

        Font bold = new Font("SansSerif", Font.BOLD, 14);
        Font normal = new Font("SansSerif", Font.PLAIN, 14);

        // Company data (left side)
        g.setFont(new Font("SansSerif", Font.BOLD, 24));
        g.drawString(company.name(), 40, 50);
        g.setFont(normal);
        g.drawString("NIT: " + company.nit(), 40, 74);
        g.drawString(company.address() + ", " + company.city(), 40, 96);
        g.drawString("Tel: " + company.phone() + "  " + company.email(), 40, 118);

        // Invoice data (right side)
        g.setFont(bold);
        g.drawString("ELECTRONIC SALES INVOICE", 520, 50);
        g.setFont(normal);
        g.drawString("No. " + invoiceNumber, 520, 74);
        g.drawString("Sale: " + sale.saleNumber(), 520, 96);
        String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        g.drawString("Date: " + date, 520, 118);

        g.drawLine(40, 140, width - 40, 140);

        // Customer data
        g.setFont(bold);
        g.drawString("CUSTOMER", 40, 166);
        g.setFont(normal);
        g.drawString("Name: " + customer.name(), 40, 190);
        g.drawString("Document: " + customer.document() + "  (" + customer.typeName() + ")", 40, 212);
        g.drawString("Address: " + customer.address(), 40, 234);

        // Table header
        g.drawLine(40, 254, width - 40, 254);
        g.setFont(bold);
        g.drawString("Lot", 40, 276);
        g.drawString("Product", 190, 276);
        g.drawString("Qty", 400, 276);
        g.drawString("Unit price", 460, 276);
        g.drawString("Disc.", 580, 276);
        g.drawString("Total", 700, 276);
        g.drawLine(40, 286, width - 40, 286);

        // One row per sale line
        g.setFont(normal);
        int y = 310;
        for (SaleItem item : items) {
            g.drawString(item.lotNumber(), 40, y);
            g.drawString(cut(item.productName(), 24), 190, y);
            g.drawString(String.valueOf(item.quantity()), 400, y);
            g.drawString(money(item.unitPrice()), 460, y);
            g.drawString(Math.round(item.discountRate() * 100) + "%", 580, y);
            g.drawString(money(item.lineTotal()), 700, y);
            y = y + 26;
        }

        // Totals (aligned to the right)
        g.drawLine(40, y - 8, width - 40, y - 8);
        y = y + 20;
        y = drawTotal(g, "Subtotal:", sale.subtotal(), y, width, normal);
        y = drawTotal(g, "Discount:", sale.totalDiscount(), y, width, normal);
        y = drawTotal(g, "Tax (19%):", sale.totalTax(), y, width, normal);
        y = drawTotal(g, "TOTAL:", sale.total(), y, width, bold);

        // QR code and CUFE
        y = y + 30;
        drawQr(g, qrLink, 40, y, 160);
        g.setFont(bold);
        g.drawString("CUFE (simulated)", 230, y + 20);
        g.setFont(new Font("Monospaced", Font.PLAIN, 12));
        g.drawString(cufe.substring(0, 48), 230, y + 44);
        g.drawString(cufe.substring(48), 230, y + 62);
        g.setFont(normal);
        g.drawString("Scan the QR code to verify the invoice.", 230, y + 100);
        g.drawString("Simulated document for academic purposes.", 230, y + 122);

        g.dispose();
        return image;
    }

    private int drawTotal(Graphics2D g, String label, double value, int y, int width, Font font) {
        g.setFont(font);
        FontMetrics metrics = g.getFontMetrics();
        String text = money(value);
        g.drawString(label, 560, y);
        g.drawString(text, width - 40 - metrics.stringWidth(text), y);
        return y + 24;
    }

    private void drawQr(Graphics2D g, String text, int startX, int startY, int size) {
        try {
            BitMatrix matrix = new QRCodeWriter().encode(text, BarcodeFormat.QR_CODE, size, size);
            for (int x = 0; x < size; x++) {
                for (int y = 0; y < size; y++) {
                    if (matrix.get(x, y)) {
                        g.fillRect(startX + x, startY + y, 1, 1);
                    }
                }
            }
        } catch (WriterException e) {
            g.drawString("QR error", startX, startY + 20);
        }
    }

    private String money(double value) {
        return String.format(Locale.US, "$%,.2f", value);
    }

    private String cut(String text, int max) {
        return text.length() <= max ? text : text.substring(0, max - 1) + ".";
    }
}