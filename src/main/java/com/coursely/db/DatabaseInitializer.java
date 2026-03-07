package com.coursely.db;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
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
        // This assumes schema.sql uses ';' to end each statement (yours does).
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
}