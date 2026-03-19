package com.coursely.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

public class MainFrame extends JFrame {
    public MainFrame() {
        super("Coursely - Weekly Timetable");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1100, 700);
        setLocationRelativeTo(null);

        JPanel content = new JPanel(new BorderLayout(0, 14));
        content.setBackground(Theme.BRAND_OFFWHITE);
        content.setBorder(new EmptyBorder(14, 14, 14, 14));
        content.add(createBrandHeader(), BorderLayout.NORTH);
        content.add(new TimetablePanel(), BorderLayout.CENTER);

        setContentPane(content);
    }

    private JPanel createBrandHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(4, 0, 8, 0));

        JPanel brandLeft = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        brandLeft.setBackground(Theme.BRAND_BLUE);
        brandLeft.setBorder(new EmptyBorder(8, 12, 8, 12));

        JLabel logoMark = new JLabel("C", SwingConstants.CENTER);
        logoMark.setOpaque(true);
        logoMark.setBackground(Theme.BRAND_OFFWHITE);
        logoMark.setForeground(Theme.BRAND_BLUE.darker());
        logoMark.setFont(Theme.FONT_HEADING.deriveFont(18f));
        logoMark.setBorder(new EmptyBorder(3, 10, 3, 10));

        JLabel brandText = new JLabel("Coursely");
        brandText.setForeground(Theme.BRAND_OFFWHITE);
        brandText.setFont(Theme.FONT_HEADING.deriveFont(24f));

        brandLeft.add(logoMark);
        brandLeft.add(brandText);
        header.add(brandLeft, BorderLayout.WEST);

        JLabel tagline = new JLabel("Plan your week with clarity");
        tagline.setOpaque(true);
        tagline.setBackground(Theme.BRAND_BLUE);
        tagline.setForeground(new Color(232, 248, 255));
        tagline.setFont(Theme.FONT_BODY.deriveFont(16f));
        tagline.setBorder(new EmptyBorder(8, 12, 8, 12));
        header.add(tagline, BorderLayout.EAST);

        return header;
    }
}
