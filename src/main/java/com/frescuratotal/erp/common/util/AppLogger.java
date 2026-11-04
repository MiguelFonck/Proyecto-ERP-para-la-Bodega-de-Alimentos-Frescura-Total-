package com.frescuratotal.erp.common.util;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class AppLogger {

    private static final Path LOG_DIR = Paths.get("logs");
    private static final Path LOG_FILE = LOG_DIR.resolve("erp.log");
    private static final DateTimeFormatter FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // Saves an information line in the log file
    public static void info(String message) {
        write("INFO", message);
    }

    // Shows the error in the console AND saves it in the log file
    public static void error(String message) {
        System.out.println(message);
        write("ERROR", message);
    }

    private static void write(String level, String message) {
        try {
            Files.createDirectories(LOG_DIR);
            try (PrintWriter out = new PrintWriter(new FileWriter(LOG_FILE.toFile(), true))) {
                out.println(LocalDateTime.now().format(FORMAT) + " [" + level + "] " + message);
            }
        } catch (IOException e) {
            System.out.println("Could not write the log: " + e.getMessage());
        }
    }
}