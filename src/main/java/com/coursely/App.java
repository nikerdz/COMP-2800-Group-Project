package com.coursely;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import com.coursely.db.DatabaseInitializer;
import com.coursely.db.DatabaseManager;
import com.coursely.ui.MainFrame;
import com.coursely.ui.Theme;

/**
 * Application entry point for Coursely.
 * Initializes the database and launches the Swing user interface.
 */
public class App {

    /**
     * Starts the application.
     *
     * @param args command-line arguments
     */
    public static void main(String[] args) {

        // Initialize the database before launching the UI.
        // This creates the data folder, opens the SQLite file,
        // and ensures the required schema is available.
        try {
            DatabaseInitializer.initialize();
            DatabaseInitializer.verifyTablesExist(
                "courses", "sections", "time_blocks", "schedules", "schedule_sections"
            );

            System.out.println("Database initialized successfully.");
            System.out.println("DB URL: " + DatabaseManager.getJdbcUrl());
        } catch (RuntimeException e) {
            // Stop the application early if the database cannot be initialized.
            System.err.println("ERROR: Database initialization failed.");
            e.printStackTrace();
            return;
        }

        // Launch the Swing UI on the Event Dispatch Thread.
        SwingUtilities.invokeLater(() -> {
            try {
                // Use the system look and feel when available.
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {
                // Fall back to the default look and feel if the system one is unavailable.
            }

            // Apply shared theme fonts to common Swing components.
            UIManager.put("Label.font", Theme.FONT_BODY);
            UIManager.put("Button.font", Theme.FONT_BODY);
            UIManager.put("ComboBox.font", Theme.FONT_BODY);
            UIManager.put("TextField.font", Theme.FONT_BODY);
            UIManager.put("CheckBox.font", Theme.FONT_BODY);
            UIManager.put("OptionPane.messageFont", Theme.FONT_BODY);
            UIManager.put("OptionPane.buttonFont", Theme.FONT_BODY);

            MainFrame frame = new MainFrame();
            frame.setVisible(true);
        });
    }
}