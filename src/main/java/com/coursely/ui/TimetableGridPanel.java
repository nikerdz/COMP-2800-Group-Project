package com.coursely.ui;

import java.awt.Color;
import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.MatteBorder;

public class TimetableGridPanel extends JPanel {

    private static final Color GRID_LINE_COLOR = new Color(205, 216, 229);
    private static final Color GRID_BORDER_COLOR = new Color(176, 196, 218);
    private static final Color HEADER_BG_COLOR = new Color(236, 246, 247);
    private static final Color TIME_COL_BG_COLOR = new Color(236, 246, 247);

    public TimetableGridPanel(String[] days, String[] timeSlots) {
        setLayout(new GridLayout(timeSlots.length + 1, days.length + 1));
        setBorder(BorderFactory.createLineBorder(GRID_BORDER_COLOR));
        setBackground(Theme.BRAND_OFFWHITE);

        add(createHeaderCell(""));
        for (String day : days) {
            add(createHeaderCell(day));
        }

        for (String timeSlot : timeSlots) {
            add(createTimeCell(timeSlot));

            for (int i = 0; i < days.length; i++) {
                add(createBodyCell());
            }
        }
    }

    private JLabel createHeaderCell(String text) {
        JLabel label = createBaseCell(text);
        label.setOpaque(true);
        label.setBackground(HEADER_BG_COLOR);
        label.setForeground(Theme.BRAND_BROWN);
        label.setFont(Theme.FONT_HEADING.deriveFont(16f));
        return label;
    }

    private JLabel createTimeCell(String text) {
        JLabel label = createBaseCell(text);
        label.setOpaque(true);
        label.setBackground(TIME_COL_BG_COLOR);
        label.setForeground(Theme.BRAND_BROWN);
        label.setFont(Theme.FONT_BODY.deriveFont(25f));
        return label;
    }

    private JLabel createBodyCell() {
        JLabel label = createBaseCell("");
        label.setOpaque(true);
        label.setBackground(Theme.BRAND_OFFWHITE);
        return label;
    }

    private JLabel createBaseCell(String text) {
        JLabel label = new JLabel(text, SwingConstants.CENTER);
        label.setBorder(new MatteBorder(0, 0, 1, 1, GRID_LINE_COLOR));
        return label;
    }
}