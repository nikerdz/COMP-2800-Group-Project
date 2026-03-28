package com.coursely.db;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class DatabaseManager {

    private static final Path DB_DIR = Paths.get(
            System.getProperty("user.home"), "Coursely"
    );
    private static final String DB_FILE = "coursely.db";
    private static final String JDBC_URL = "jdbc:sqlite:" + DB_DIR.resolve(DB_FILE).toString();

    private DatabaseManager() { }

    public static Connection getConnection() throws SQLException {
        ensureDbDirectoryExists();
        return DriverManager.getConnection(JDBC_URL);
    }

    public static String getJdbcUrl() {
        return JDBC_URL;
    }

    private static void ensureDbDirectoryExists() {
        try {
            if (!Files.exists(DB_DIR)) {
                Files.createDirectories(DB_DIR);
            }
        } catch (java.io.IOException | SecurityException e) {
            throw new RuntimeException("Failed to create DB directory: " + DB_DIR, e);
        }
    }
}