package com.coursely.db;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

public final class DatabaseInitializer {

    private static final Path SCHEMA_PATH = Path.of("database", "schema.sql");

    private DatabaseInitializer() { }

    public static void initialize() {
        if (!Files.exists(SCHEMA_PATH)) {
            throw new RuntimeException("schema.sql not found at: " + SCHEMA_PATH.toAbsolutePath());
        }

        try (Connection conn = DatabaseManager.getConnection()) {
            runSchema(conn);
        } catch (SQLException | IOException e) {
            throw new RuntimeException("Database initialization failed", e);
        }
    }

    private static void runSchema(Connection conn) throws IOException, SQLException {
        List<String> lines = Files.readAllLines(SCHEMA_PATH, StandardCharsets.UTF_8);

        // Join lines and split by semicolon to get statements.
        // This assumes schema.sql uses ';' to end each statement.
        String sql = String.join("\n", lines);

        // Remove simple SQL line comments
        sql = sql.replaceAll("(?m)^\\s*--.*$", "");

        String[] statements = sql.split(";");
        for (String raw : statements) {
            String stmt = raw.trim();
            if (stmt.isEmpty()) continue;

            try (Statement s = conn.createStatement()) {
                s.execute(stmt);
            }
        }
    }

    public static void verifyTablesExist(String... expectedTables) {
    try (Connection conn = DatabaseManager.getConnection();
         Statement st = conn.createStatement()) {

        for (String table : expectedTables) {
            try (ResultSet rs = st.executeQuery(
                    "SELECT name FROM sqlite_master WHERE type='table' AND name='" + table + "';")) {   // Sprint 2: Update to use prepared statement to prevent SQL injection
                if (!rs.next()) {
                    throw new RuntimeException("Expected table missing: " + table);
                }
            }
        }

        System.out.println("Verified tables exist: " + String.join(", ", expectedTables));

    } catch (SQLException e) {
        throw new RuntimeException("Table verification failed", e);
    }
}
}