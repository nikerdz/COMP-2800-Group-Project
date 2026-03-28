package com.coursely.db;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

// Manages the SQLite database file location and provides connections
// The database is stored in a Coursely folder in the user's home directory
public final class DatabaseManager {

    // Resolves to ~/Coursely/ on all platforms via the user.home system property
    private static final Path DB_DIR = Paths.get(
            System.getProperty("user.home"), "Coursely"
    );
    private static final String DB_FILE = "coursely.db";
    private static final String JDBC_URL = "jdbc:sqlite:" + DB_DIR.resolve(DB_FILE).toString();

    private DatabaseManager() { }

    // Returns a new connection to the SQLite database, creating the directory first if needed
    public static Connection getConnection() throws SQLException {
        ensureDbDirectoryExists();
        return DriverManager.getConnection(JDBC_URL);
    }

    // Exposes the JDBC URL, primarily for external configuration or testing
    public static String getJdbcUrl() {
        return JDBC_URL;
    }

    // Creates the database directory if it does not already exist
    // Throws a RuntimeException if the directory cannot be created due to permissions or IO errors
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