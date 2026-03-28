package com.coursely.ui;

import java.awt.Color;
import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.MatteBorder;

/**
 * Static background grid for the timetable view.
 * Renders day headers, time labels, and body cells.
 */
public class TimetableGridPanel extends JPanel {

    // Grid line color for internal cell borders.
    private static final Color GRID_LINE_COLOR = new Color(205, 216, 229);

    // Outer border color for the timetable grid.
    private static final Color GRID_BORDER_COLOR = new Color(176, 196, 218);

    // Background color for the top header row.
    private static final Color HEADER_BG_COLOR = new Color(236, 246, 247);

    // Background color for the left time column.
    private static final Color TIME_COL_BG_COLOR = new Color(236, 246, 247);

    /**
     * Builds the timetable grid using the supplied day and time labels.
     *
     * @param days the day column labels
     * @param timeSlots the time row labels
     */
    public TimetableGridPanel(String[] days, String[] timeSlots) {
        setLayout(new GridLayout(timeSlots.length + 1, days.length + 1));
        setBorder(BorderFactory.createLineBorder(GRID_BORDER_COLOR));
        setBackground(Theme.BRAND_OFFWHITE);

        // Top-left corner cell remains blank.
        add(createHeaderCell(""));

        // Build the day header row.
        for (String day : days) {
            add(createHeaderCell(day));
        }

        // Build the time column and empty timetable body cells.
        for (String timeSlot : timeSlots) {
            add(createTimeCell(timeSlot));

            for (int i = 0; i < days.length; i++) {
                add(createBodyCell());
            }
        }
    }

    /**
     * Creates a styled header cell for the day row.
     *
     * @param text the label text
     * @return the configured header label
     */
    private JLabel createHeaderCell(String text) {
        JLabel label = createBaseCell(text);
        label.setOpaque(true);
        label.setBackground(HEADER_BG_COLOR);
        label.setForeground(Theme.BRAND_BROWN);
        label.setFont(Theme.FONT_HEADING.deriveFont(16f));
        return label;
    }

    /**
     * Creates a styled cell for the time column.
     *
     * @param text the time label
     * @return the configured time label
     */
    private JLabel createTimeCell(String text) {
        JLabel label = createBaseCell(text);
        label.setOpaque(true);
        label.setBackground(TIME_COL_BG_COLOR);
        label.setForeground(Theme.BRAND_BROWN);
        label.setFont(Theme.FONT_BODY.deriveFont(25f));
        return label;
    }

    /**
     * Creates an empty body cell for the timetable interior.
     *
     * @return the configured empty cell
     */
    private JLabel createBodyCell() {
        JLabel label = createBaseCell("");
        label.setOpaque(true);
        label.setBackground(Theme.BRAND_OFFWHITE);
        return label;
    }

    /**
     * Creates a base cell with centered text and standard grid border styling.
     *
     * @param text the cell text
     * @return the configured label
     */
    private JLabel createBaseCell(String text) {
        JLabel label = new JLabel(text, SwingConstants.CENTER);
        label.setBorder(new MatteBorder(0, 0, 1, 1, GRID_LINE_COLOR));
        return label;
    }
}