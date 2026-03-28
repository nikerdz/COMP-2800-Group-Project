package com.coursely.db;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public final class DatabaseInitializer {

    private DatabaseInitializer() { }

    public static void initialize() {
        try (Connection conn = DatabaseManager.getConnection()) {
            runSchema(conn);
            runMigrations(conn);
        } catch (SQLException | IOException e) {
            throw new RuntimeException("Database initialization failed", e);
        }
    }

    private static void runMigrations(Connection conn) throws SQLException {
        try (ResultSet rs = conn.getMetaData().getColumns(null, null, "sections", "color")) {
            if (!rs.next()) {
                try (Statement s = conn.createStatement()) {
                    s.execute("ALTER TABLE sections ADD COLUMN color TEXT");
                }
            }
        }
    }

    private static void runSchema(Connection conn) throws IOException, SQLException {
        InputStream is = DatabaseInitializer.class.getResourceAsStream("/database/schema.sql");
        if (is == null) {
            throw new RuntimeException("schema.sql not found in JAR resources");
        }

        String sql = new String(is.readAllBytes(), StandardCharsets.UTF_8);

        // Remove SQL line comments
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
                        "SELECT name FROM sqlite_master WHERE type='table' AND name='" + table + "';")) {
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