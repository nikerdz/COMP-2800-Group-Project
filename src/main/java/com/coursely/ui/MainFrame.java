package com.coursely.ui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

/**
 * Main application window for the Coursely timetable interface.
 * Sets up the frame, branding header, and central timetable panel.
 */
public class MainFrame extends JFrame {

    /**
     * Creates and configures the main application window.
     */
    public MainFrame() {
        super("Coursely - Plan your week with clarity!");

        // Set the application window icon.
        setIconImage(ResourceUtils.loadImage("/images/logo3.png"));

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1280, 800);
        setLocationRelativeTo(null);
        setBackground(Theme.BRAND_OFFWHITE);

        // Main content container for the frame.
        JPanel content = new JPanel(new BorderLayout(0, 14));
        content.setBackground(Theme.BRAND_OFFWHITE);
        content.setBorder(new EmptyBorder(14, 14, 14, 14));

        content.add(createBrandHeader(), BorderLayout.NORTH);
        content.add(new TimetablePanel(), BorderLayout.CENTER);

        setContentPane(content);
    }

    /**
     * Builds the branded header shown at the top of the main window.
     *
     * @return the header panel
     */
    private JPanel createBrandHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Theme.BRAND_OFFWHITE);
        header.setOpaque(true);
        header.setBorder(new EmptyBorder(4, 0, 8, 0));

        // Left-aligned branding area containing the application logo.
        JPanel brandLeft = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        brandLeft.setOpaque(true);
        brandLeft.setBackground(Theme.BRAND_OFFWHITE);
        brandLeft.setBorder(new EmptyBorder(8, 12, 8, 12));

        JLabel logoMark = new JLabel(ResourceUtils.loadIconKeepAspect("/images/logo2.png", 64));
        logoMark.setBorder(new EmptyBorder(0, 0, 0, 6));

        brandLeft.add(logoMark);
        header.add(brandLeft, BorderLayout.WEST);

        return header;
    }
}