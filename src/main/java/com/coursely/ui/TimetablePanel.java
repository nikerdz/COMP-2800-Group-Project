package com.coursely.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.MatteBorder;

public class TimetablePanel extends JPanel {
    private static final String[] DAYS = {
        "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"
    };

    private static final String[] TIME_SLOTS = {
        "8:00 AM", "9:00 AM", "10:00 AM", "11:00 AM", "12:00 PM", "1:00 PM", "2:00 PM",
        "3:00 PM", "4:00 PM", "5:00 PM", "6:00 PM"
    };

    public TimetablePanel() {
        setLayout(new BorderLayout(0, 10));

        JLabel title = new JLabel("Weekly Timetable");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 20f));
        title.setHorizontalAlignment(SwingConstants.LEFT);
        add(title, BorderLayout.NORTH);

        JPanel grid = new JPanel(new GridLayout(TIME_SLOTS.length + 1, DAYS.length + 1));
        grid.setBorder(BorderFactory.createLineBorder(new Color(190, 190, 190)));
        grid.setBackground(Color.WHITE);

        grid.add(createHeaderCell("Time"));
        for (String day : DAYS) {
            grid.add(createHeaderCell(day));
        }

        for (String timeSlot : TIME_SLOTS) {
            grid.add(createTimeCell(timeSlot));
            for (int i = 0; i < DAYS.length; i++) {
                grid.add(createBodyCell());
            }
        }

        add(grid, BorderLayout.CENTER);
    }

    private JLabel createHeaderCell(String text) {
        JLabel label = createBaseCell(text);
        label.setOpaque(true);
        label.setBackground(new Color(236, 243, 252));
        label.setFont(label.getFont().deriveFont(Font.BOLD));
        return label;
    }

    private JLabel createTimeCell(String text) {
        JLabel label = createBaseCell(text);
        label.setOpaque(true);
        label.setBackground(new Color(250, 250, 250));
        label.setFont(label.getFont().deriveFont(Font.BOLD));
        return label;
    }

    private JLabel createBodyCell() {
        JLabel label = createBaseCell("");
        label.setOpaque(true);
        label.setBackground(Color.WHITE);
        return label;
    }

    private JLabel createBaseCell(String text) {
        JLabel label = new JLabel(text, SwingConstants.CENTER);
        label.setBorder(new MatteBorder(0, 0, 1, 1, new Color(220, 220, 220)));
        return label;
    }
}
