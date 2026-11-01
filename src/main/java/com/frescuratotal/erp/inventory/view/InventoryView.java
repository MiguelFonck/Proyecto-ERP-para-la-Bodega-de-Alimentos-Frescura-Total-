package com.frescuratotal.erp.inventory.view;

import java.time.LocalDate;
import java.util.Scanner;

public class InventoryView {

    private final Scanner scanner = new Scanner(System.in);

    public String askText(String question) {
        System.out.print(question);
        return scanner.nextLine();
    }

    public int askInt(String question) {
        System.out.print(question);
        return Integer.parseInt(scanner.nextLine());
    }

    public double askDouble(String question) {
        System.out.print(question);
        return Double.parseDouble(scanner.nextLine());
    }

    public LocalDate askDate(String question) {
        System.out.print(question);
        return LocalDate.parse(scanner.nextLine());
    }

    public void showMessage(String message) {
        System.out.println(message);
    }
}