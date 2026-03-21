package com.coursely.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;
import javax.swing.border.MatteBorder;

import com.coursely.model.Schedule;
import com.coursely.model.Section;
import com.coursely.model.SectionType;
import com.coursely.model.TimeBlock;

public class TimetablePanel extends JPanel {
    private static final String[] DAYS = {
            "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"
    };

    private static final String[] TIME_SLOTS = createHourSlots();
    private static final String[] TIME_OPTIONS = createTimeOptions();

    private static final DateTimeFormatter SLOT_FORMAT = DateTimeFormatter.ofPattern("h:mm a", Locale.ENGLISH);
    private static final DateTimeFormatter DISPLAY_TIME_FORMAT = DateTimeFormatter.ofPattern("h:mm a", Locale.ENGLISH);

    private static final Color GRID_LINE_COLOR = new Color(205, 216, 229);
    private static final Color GRID_BORDER_COLOR = new Color(176, 196, 218);
    private static final Color HEADER_BG_COLOR = new Color(226, 239, 251);
    private static final Color TIME_COL_BG_COLOR = new Color(244, 248, 252);

    private static final Color PANEL_BG_COLOR = Theme.BRAND_OFFWHITE;
    private static final Color BLOCK_DEFAULT_COLOR = Theme.BLOCK_BLUE;

    private static final Map<String, Color> PRESET_COLORS = createPresetColors();

    private final Map<String, TimetableBodyCell> bodyCells = new HashMap<>();

    // ✅ DB-friendly in-memory model
    private final Schedule schedule = new Schedule();

    // Preserve user-entered type for display even if it doesn't match the enum
    private final Map<String, String> displayTypeByUiId = new HashMap<>();

    public TimetablePanel() {
        setLayout(new BorderLayout(0, 12));
        setBackground(PANEL_BG_COLOR);

        JLabel title = new JLabel("Weekly Timetable");
        title.setFont(Theme.FONT_HEADING.deriveFont(Theme.SIZE_HEADING));
        title.setForeground(Theme.BRAND_BROWN);
        title.setHorizontalAlignment(SwingConstants.LEFT);

        JButton addBlockButton = new JButton("Add Block");
        addBlockButton.setBackground(Theme.BRAND_OFFWHITE);
        addBlockButton.setForeground(Theme.BRAND_BROWN);
        addBlockButton.setFocusPainted(false);
        addBlockButton.setOpaque(true);
        addBlockButton.setContentAreaFilled(true);
        addBlockButton.setFont(Theme.FONT_BODY.deriveFont(18f));
        addBlockButton.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(Theme.BRAND_BLUE, 2, true),
                BorderFactory.createEmptyBorder(6, 14, 6, 14)
        ));
        addBlockButton.addActionListener(e -> showAddBlockDialog());

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        headerPanel.setBorder(new javax.swing.border.EmptyBorder(4, 2, 4, 2));
        headerPanel.add(title, BorderLayout.WEST);
        headerPanel.add(addBlockButton, BorderLayout.EAST);
        add(headerPanel, BorderLayout.NORTH);

        JPanel grid = new JPanel(new GridLayout(TIME_SLOTS.length + 1, DAYS.length + 1));
        grid.setBorder(BorderFactory.createLineBorder(GRID_BORDER_COLOR));
        grid.setBackground(Theme.BRAND_OFFWHITE);

        grid.add(createHeaderCell(""));
        for (String day : DAYS) {
            grid.add(createHeaderCell(day));
        }

        for (String timeSlot : TIME_SLOTS) {
            grid.add(createTimeCell(timeSlot));
            for (String day : DAYS) {
                TimetableBodyCell bodyCell = createBodyCell();
                bodyCells.put(createCellKey(day, timeSlot), bodyCell);
                grid.add(bodyCell);
            }
        }

        add(grid, BorderLayout.CENTER);
    }

    private JLabel createHeaderCell(String text) {
        JLabel label = createBaseCell(text);
        label.setOpaque(true);
        label.setBackground(HEADER_BG_COLOR);
        label.setForeground(Theme.BRAND_BROWN);
        // Use heading font for day headers
        label.setFont(Theme.FONT_HEADING.deriveFont(16f));
        return label;
    }

    private JLabel createTimeCell(String text) {
        JLabel label = createBaseCell(text);
        label.setOpaque(true);
        label.setBackground(TIME_COL_BG_COLOR);
        label.setForeground(Theme.BRAND_BROWN);
        // ✅ Use Dongle/body font for times (avoids weird bold digits from Cooper)
        label.setFont(Theme.FONT_BODY.deriveFont(25f));
        return label;
    }

    private TimetableBodyCell createBodyCell() {
        TimetableBodyCell label = new TimetableBodyCell();
        label.setBackground(Theme.BRAND_OFFWHITE);
        label.setVerticalAlignment(SwingConstants.TOP);
        label.setHorizontalAlignment(SwingConstants.CENTER);
        label.setBorder(new MatteBorder(0, 0, 1, 1, GRID_LINE_COLOR));
        label.setFont(Theme.FONT_BODY.deriveFont(15f));
        return label;
    }

    private JLabel createBaseCell(String text) {
        JLabel label = new JLabel(text, SwingConstants.CENTER);
        label.setBorder(new MatteBorder(0, 0, 1, 1, GRID_LINE_COLOR));
        return label;
    }

    private void showAddBlockDialog() {
        javax.swing.JTextField titleField = new javax.swing.JTextField(16);
        Map<String, JCheckBox> dayCheckboxes = createDayCheckboxes();

        JComboBox<String> startField = new JComboBox<>(TIME_OPTIONS);
        JComboBox<String> endField = new JComboBox<>(TIME_OPTIONS);

        JComboBox<String> colorField = new JComboBox<>(PRESET_COLORS.keySet().toArray(new String[0]));
        JPanel colorPreview = createColorPreviewSwatch((String) colorField.getSelectedItem());

        javax.swing.JTextField typeField = new javax.swing.JTextField(16);

        endField.setSelectedIndex(Math.min(1, TIME_OPTIONS.length - 1));
        colorField.addActionListener(e -> updateColorPreview(colorPreview, (String) colorField.getSelectedItem()));

        JPanel formPanel = buildFormPanel(titleField, dayCheckboxes, startField, endField, colorField, colorPreview, typeField);

        while (true) {
            int choice = JOptionPane.showConfirmDialog(
                    this,
                    formPanel,
                    "Add Timetable Block",
                    JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.PLAIN_MESSAGE
            );
            if (choice != JOptionPane.OK_OPTION) {
                return;
            }

            String title = titleField.getText().trim();
            List<String> selectedDays = getSelectedDays(dayCheckboxes);
            String startInput = (String) startField.getSelectedItem();
            String endInput = (String) endField.getSelectedItem();
            String colorName = (String) colorField.getSelectedItem();
            String typeText = typeField.getText().trim(); // optional free text

            String validationError = validateBlock(title, selectedDays, startInput, endInput);
            if (validationError != null) {
                JOptionPane.showMessageDialog(this, validationError, "Validation Error", JOptionPane.ERROR_MESSAGE);
                continue;
            }

            LocalTime start = parseTimeFlexible(startInput);
            LocalTime end = parseTimeFlexible(endInput);
            Color selectedColor = getColorByName(colorName);

            // ✅ Create ONE Section, add multiple TimeBlocks (one per selected day)
            SectionType parsedType = parseSectionTypeOrDefault(typeText);
            Section section = new Section(null, null, title, parsedType, null, null, selectedColor);

            for (String day : selectedDays) {
                section.addTimeBlock(new TimeBlock(day, start, end));
            }

            schedule.addSection(section);
            displayTypeByUiId.put(section.getUiId(), typeText); // keep original for display
            refreshGrid();
            return;
        }
    }

    private SectionType parseSectionTypeOrDefault(String input) {
        if (input == null || input.isBlank()) {
            return SectionType.LECTURE;
        }
        try {
            return SectionType.fromString(input);
        } catch (Exception ignored) {
            // If user typed something custom like "Office Hours", keep display text but store safe enum
            return SectionType.LECTURE;
        }
    }

    private JPanel buildFormPanel(
            javax.swing.JTextField titleField,
            Map<String, JCheckBox> dayCheckboxes,
            JComboBox<String> startField,
            JComboBox<String> endField,
            JComboBox<String> colorField,
            JPanel colorPreview,
            javax.swing.JTextField typeField
    ) {
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(PANEL_BG_COLOR);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        titleField.setFont(Theme.FONT_BODY.deriveFont(15f));
        startField.setFont(Theme.FONT_BODY.deriveFont(15f));
        endField.setFont(Theme.FONT_BODY.deriveFont(15f));
        colorField.setFont(Theme.FONT_BODY.deriveFont(15f));
        typeField.setFont(Theme.FONT_BODY.deriveFont(15f));

        addRow(form, gbc, 0, "Title / Course Code", titleField);
        addRow(form, gbc, 1, "Days", createDaySelectionPanel(dayCheckboxes));
        addRow(form, gbc, 2, "Start Time", startField);
        addRow(form, gbc, 3, "End Time", endField);
        addRow(form, gbc, 4, "Color", createColorPickerRow(colorField, colorPreview));
        addRow(form, gbc, 5, "Type (Optional)", typeField);

        return form;
    }

    private static Map<String, Color> createPresetColors() {
        Map<String, Color> colors = new LinkedHashMap<>();
        colors.put("Blue", Theme.BLOCK_BLUE);
        colors.put("Aqua", Theme.BLOCK_AQUA);
        colors.put("Lemon", Theme.BLOCK_LEMON);
        colors.put("Peach", Theme.BLOCK_PEACH);
        colors.put("Pink", Theme.BLOCK_PINK);
        colors.put("Lilac", Theme.BLOCK_LILAC);
        return colors;
    }

    private JPanel createColorPickerRow(JComboBox<String> colorField, JPanel colorPreview) {
        JPanel row = new JPanel(new BorderLayout(8, 0));
        row.setOpaque(false);
        row.add(colorField, BorderLayout.CENTER);
        row.add(colorPreview, BorderLayout.EAST);
        return row;
    }

    private JPanel createColorPreviewSwatch(String colorName) {
        JPanel swatch = new JPanel();
        swatch.setPreferredSize(new Dimension(28, 20));
        swatch.setBorder(BorderFactory.createLineBorder(new Color(120, 120, 120)));
        updateColorPreview(swatch, colorName);
        return swatch;
    }

    private void updateColorPreview(JPanel swatch, String colorName) {
        swatch.setBackground(getColorByName(colorName));
    }

    private Color getColorByName(String colorName) {
        if (colorName == null) {
            return BLOCK_DEFAULT_COLOR;
        }
        Color color = PRESET_COLORS.get(colorName);
        return color == null ? BLOCK_DEFAULT_COLOR : color;
    }

    private static String[] createTimeOptions() {
        List<String> options = new ArrayList<>();
        DateTimeFormatter dropdownTimeFormat = DateTimeFormatter.ofPattern("h:mm a", Locale.ENGLISH);
        LocalTime current = LocalTime.of(8, 0);
        LocalTime end = LocalTime.of(19, 0);
        while (!current.isAfter(end)) {
            options.add(current.format(dropdownTimeFormat));
            current = current.plusMinutes(30);
        }
        return options.toArray(new String[0]);
    }

    private static String[] createHourSlots() {
        return new String[]{
                "8:00 AM", "9:00 AM", "10:00 AM", "11:00 AM", "12:00 PM", "1:00 PM", "2:00 PM",
                "3:00 PM", "4:00 PM", "5:00 PM", "6:00 PM"
        };
    }

    private Map<String, JCheckBox> createDayCheckboxes() {
        Map<String, JCheckBox> checkboxes = new LinkedHashMap<>();
        for (String day : DAYS) {
            checkboxes.put(day, new JCheckBox(day));
        }
        return checkboxes;
    }

    private JPanel createDaySelectionPanel(Map<String, JCheckBox> dayCheckboxes) {
        JPanel dayPanel = new JPanel(new GridLayout(0, 2, 6, 2));
        dayPanel.setOpaque(false);
        for (String day : DAYS) {
            JCheckBox checkBox = dayCheckboxes.get(day);
            checkBox.setOpaque(false);
            checkBox.setFont(Theme.FONT_BODY.deriveFont(15f));
            checkBox.setForeground(Theme.BRAND_BROWN);
            dayPanel.add(checkBox);
        }
        return dayPanel;
    }

    private List<String> getSelectedDays(Map<String, JCheckBox> dayCheckboxes) {
        List<String> selectedDays = new ArrayList<>();
        for (String day : DAYS) {
            JCheckBox checkBox = dayCheckboxes.get(day);
            if (checkBox != null && checkBox.isSelected()) {
                selectedDays.add(day);
            }
        }
        return selectedDays;
    }

    private void addRow(JPanel panel, GridBagConstraints gbc, int row, String label, java.awt.Component field) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0;

        JLabel labelComponent = new JLabel(label + ":");
        labelComponent.setFont(Theme.FONT_BODY.deriveFont(15f));
        labelComponent.setForeground(Theme.BRAND_BROWN);
        panel.add(labelComponent, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        panel.add(field, gbc);
    }

    private String validateBlock(String title, List<String> selectedDays, String startInput, String endInput) {
        if (title.isBlank()) {
            return "Title / Course Code is required.";
        }
        if (selectedDays.isEmpty()) {
            return "Please select at least one day.";
        }
        if (selectedDays.size() > 2) {
            return "Please select up to two days for this block.";
        }
        if (startInput == null || endInput == null || startInput.isBlank() || endInput.isBlank()) {
            return "Start time and end time are required.";
        }

        LocalTime start = parseTimeFlexible(startInput);
        LocalTime end = parseTimeFlexible(endInput);
        if (start == null || end == null) {
            return "Invalid time format. Use values like 9:00, 09:00, or 9:00 AM.";
        }
        if (!end.isAfter(start)) {
            return "End time must be after start time.";
        }
        return null;
    }

    private void refreshGrid() {
        clearGrid();

        // Track first slot per (section, day) so we only write text once
        Set<String> wroteTextFor = new HashSet<>();

        for (Section section : schedule.getSections()) {
            Color sectionColor = (section.getColor() == null) ? BLOCK_DEFAULT_COLOR : section.getColor();

            for (TimeBlock tb : section.getTimeBlocks()) {
                boolean firstCoveredSlotForDay = true;

                for (String slotLabel : TIME_SLOTS) {
                    LocalTime slotStart = LocalTime.parse(slotLabel, SLOT_FORMAT);
                    LocalTime slotEnd = slotStart.plusHours(1);

                    LocalTime overlapStart = tb.getStartTime().isAfter(slotStart) ? tb.getStartTime() : slotStart;
                    LocalTime overlapEnd = tb.getEndTime().isBefore(slotEnd) ? tb.getEndTime() : slotEnd;
                    if (!overlapStart.isBefore(overlapEnd)) {
                        continue;
                    }

                    TimetableBodyCell cell = bodyCells.get(createCellKey(tb.getDayOfWeek(), slotLabel));
                    if (cell == null) continue;

                    double slotDurationSeconds = slotEnd.toSecondOfDay() - slotStart.toSecondOfDay();
                    double fillStart = (overlapStart.toSecondOfDay() - slotStart.toSecondOfDay()) / slotDurationSeconds;
                    double fillEnd = (overlapEnd.toSecondOfDay() - slotStart.toSecondOfDay()) / slotDurationSeconds;

                    cell.setFillRange(fillStart, fillEnd, sectionColor);

                    String key = section.getUiId() + "|" + tb.getDayOfWeek();
                    if (firstCoveredSlotForDay && !wroteTextFor.contains(key)) {
                        String displayType = displayTypeByUiId.getOrDefault(section.getUiId(), "");
                        String optionalType = displayType.isBlank() ? "" : "<br/>" + escapeHtml(displayType);

                        cell.setText("<html><b>" + escapeHtml(section.getSectionCode()) + "</b><br/>"
                                + escapeHtml(formatRange(tb.getStartTime(), tb.getEndTime()))
                                + optionalType + "</html>");

                        wroteTextFor.add(key);
                        firstCoveredSlotForDay = false;
                    }
                }
            }
        }
    }

    private void clearGrid() {
        for (TimetableBodyCell cell : bodyCells.values()) {
            cell.setText("");
            cell.clearFill();
        }
    }

    private String createCellKey(String day, String time) {
        return day + "|" + time;
    }

    private LocalTime parseTimeFlexible(String time) {
        LocalTime parsed = tryParseTime(time, "H:mm");
        if (parsed == null) parsed = tryParseTime(time, "h:mm");
        if (parsed == null) parsed = tryParseTime(time, "h:mm a");
        return parsed;
    }

    private LocalTime tryParseTime(String value, String pattern) {
        try {
            return LocalTime.parse(value.toUpperCase(Locale.ENGLISH),
                    DateTimeFormatter.ofPattern(pattern, Locale.ENGLISH));
        } catch (DateTimeParseException ignored) {
            return null;
        }
    }

    private String formatRange(LocalTime start, LocalTime end) {
        return start.format(DISPLAY_TIME_FORMAT) + " - " + end.format(DISPLAY_TIME_FORMAT);
    }

    private String escapeHtml(String value) {
        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");
    }

    private static class TimetableBodyCell extends JLabel {
        private double fillStartFraction = 0;
        private double fillEndFraction = 0;
        private Color fillColor = BLOCK_DEFAULT_COLOR;

        private final Border defaultBorder = new MatteBorder(0, 0, 1, 1, GRID_LINE_COLOR);

        TimetableBodyCell() {
            super("");
            setOpaque(false);
            setBorder(defaultBorder);
        }

        void setFillRange(double startFraction, double endFraction, Color color) {
            fillStartFraction = clamp(startFraction);
            fillEndFraction = clamp(endFraction);
            fillColor = color == null ? BLOCK_DEFAULT_COLOR : color;
            setBorder(new MatteBorder(0, 0, 1, 1, fillColor));
            repaint();
        }

        void clearFill() {
            fillStartFraction = 0;
            fillEndFraction = 0;
            setBorder(defaultBorder);
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            g.setColor(Theme.BRAND_OFFWHITE);
            g.fillRect(0, 0, getWidth(), getHeight());

            if (fillEndFraction > fillStartFraction) {
                int y = (int) Math.round(getHeight() * fillStartFraction);
                int h = (int) Math.round(getHeight() * (fillEndFraction - fillStartFraction));
                g.setColor(fillColor);
                g.fillRect(0, y, getWidth(), h);
            }

            super.paintComponent(g);
        }

        private double clamp(double value) {
            if (value < 0) return 0;
            if (value > 1) return 1;
            return value;
        }
    }
}