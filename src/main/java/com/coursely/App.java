package com.coursely;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import com.coursely.db.DatabaseInitializer;
import com.coursely.db.DatabaseManager;
import com.coursely.ui.MainFrame;

public class App {
    public static void main(String[] args) {

        // 1) Initialize DB (creates data/ folder, opens SQLite file, runs schema)
        try {
            DatabaseInitializer.initialize();
            DatabaseInitializer.verifyTablesExist(
                "courses", "sections", "time_blocks", "schedules", "schedule_sections"
            );
            System.out.println("Database initialized successfully.");
            System.out.println("DB URL: " + DatabaseManager.getJdbcUrl());
        } catch (RuntimeException e) {
            System.err.println("ERROR: Database initialization failed.");
            e.printStackTrace();
            return;
        }
        
        // 2) Launch UI
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) { } // Fall back to default look and feel if the system one is unavailable.

            MainFrame frame = new MainFrame();
            frame.setVisible(true);
        });
    }
}