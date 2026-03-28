package com.coursely.db;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

// Handles first-time database setup and schema migrations on startup
public final class DatabaseInitializer {

    private DatabaseInitializer() { }

    // Entry point for database initialization — runs the schema and any pending migrations
    public static void initialize() {
        try (Connection conn = DatabaseManager.getConnection()) {
            runSchema(conn);
            runMigrations(conn);
        } catch (SQLException | IOException e) {
            throw new RuntimeException("Database initialization failed", e);
        }
    }

    // Applies any schema changes that are not covered by the base schema.sql
    // Each migration checks whether the change is needed before applying it
    private static void runMigrations(Connection conn) throws SQLException {
        // Adds the color column to sections if it was not present in an earlier version of the schema
        try (ResultSet rs = conn.getMetaData().getColumns(null, null, "sections", "color")) {
            if (!rs.next()) {
                try (Statement s = conn.createStatement()) {
                    s.execute("ALTER TABLE sections ADD COLUMN color TEXT");
                }
            }
        }
    }

    // Reads and executes schema.sql from the JAR's resources to create tables if they do not exist
    private static void runSchema(Connection conn) throws IOException, SQLException {
        InputStream is = DatabaseInitializer.class.getResourceAsStream("/database/schema.sql");
        if (is == null) {
            throw new RuntimeException("schema.sql not found in JAR resources");
        }

        String sql = new String(is.readAllBytes(), StandardCharsets.UTF_8);

        // Strip SQL line comments before splitting, as they can interfere with statement parsing
        sql = sql.replaceAll("(?m)^\\s*--.*$", "");

        // Split on semicolons to execute each statement individually, as JDBC does not support batched DDL
        String[] statements = sql.split(";");
        for (String raw : statements) {
            String stmt = raw.trim();
            if (stmt.isEmpty()) continue;

            try (Statement s = conn.createStatement()) {
                s.execute(stmt);
            }
        }
    }

    // Queries sqlite_master to confirm each expected table exists, throwing if any are missing
    // Used as a sanity check after initialization to catch setup failures early
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