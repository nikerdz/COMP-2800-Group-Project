package com.coursely;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import com.coursely.ui.MainFrame;

public class App {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {
                // Fall back to default look and feel if the system one is unavailable.
            }

            MainFrame frame = new MainFrame();
            frame.setVisible(true);
        });
    }
}
