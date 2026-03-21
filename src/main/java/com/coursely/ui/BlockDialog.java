package com.coursely.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.coursely.model.SectionType;

public final class BlockDialog {

    private static final DateTimeFormatter DISPLAY_TIME_FORMAT =
            DateTimeFormatter.ofPattern("h:mm a", Locale.ENGLISH);

    private static final Map<String, Color> PRESET_COLORS = createPresetColors();

    private BlockDialog() {}

    public static BlockFormData show(Component parent, String dialogTitle, BlockFormData initial, String[] days) {
        JTextField titleField = new JTextField(initial.title, 16);
        JTextField typeField = new JTextField(initial.typeText, 16);

        Map<String, JCheckBox> dayCheckboxes = createDayCheckboxes(days);
        for (String d : initial.days) {
            JCheckBox cb = dayCheckboxes.get(d);
            if (cb != null) cb.setSelected(true);
        }

        JComboBox<String> startField = new JComboBox<>(createTimeOptions());
        JComboBox<String> endField = new JComboBox<>(createTimeOptions());

        startField.setSelectedItem(initial.start.format(DISPLAY_TIME_FORMAT));
        endField.setSelectedItem(initial.end.format(DISPLAY_TIME_FORMAT));

        JComboBox<String> colorField = new JComboBox<>(PRESET_COLORS.keySet().toArray(new String[0]));
        String initialColorName = findColorName(initial.color);
        if (initialColorName != null) {
            colorField.setSelectedItem(initialColorName);
        }

        JPanel colorPreview = createColorPreviewSwatch((String) colorField.getSelectedItem());
        colorField.addActionListener(e -> updateColorPreview(colorPreview, (String) colorField.getSelectedItem()));

        JPanel formPanel = buildFormPanel(
                titleField, dayCheckboxes, startField, endField, colorField, colorPreview, typeField, days
        );

        while (true) {
            int choice = JOptionPane.showConfirmDialog(
                    parent,
                    formPanel,
                    dialogTitle,
                    JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.PLAIN_MESSAGE
            );

            if (choice != JOptionPane.OK_OPTION) {
                return null;
            }

            String title = titleField.getText().trim();
            List<String> selectedDays = getSelectedDays(dayCheckboxes, days);
            String startInput = (String) startField.getSelectedItem();
            String endInput = (String) endField.getSelectedItem();
            String colorName = (String) colorField.getSelectedItem();
            String typeText = typeField.getText().trim();

            String validationError = validateBlock(title, selectedDays, startInput, endInput);
            if (validationError != null) {
                JOptionPane.showMessageDialog(parent, validationError, "Validation Error", JOptionPane.ERROR_MESSAGE);
                continue;
            }

            LocalTime start = parseTimeFlexible(startInput);
            LocalTime end = parseTimeFlexible(endInput);
            Color color = getColorByName(colorName);

            return new BlockFormData(title, selectedDays, start, end, typeText, color);
        }
    }

    public static SectionType parseSectionTypeOrDefault(String input) {
        if (input == null || input.isBlank()) return SectionType.LECTURE;
        try {
            return SectionType.fromString(input);
        } catch (Exception ignored) {
            return SectionType.LECTURE;
        }
    }

    private static JPanel buildFormPanel(
            JTextField titleField,
            Map<String, JCheckBox> dayCheckboxes,
            JComboBox<String> startField,
            JComboBox<String> endField,
            JComboBox<String> colorField,
            JPanel colorPreview,
            JTextField typeField,
            String[] days
    ) {
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Theme.BRAND_OFFWHITE);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        titleField.setFont(Theme.FONT_BODY.deriveFont(20f));
        startField.setFont(Theme.FONT_BODY.deriveFont(20f));
        endField.setFont(Theme.FONT_BODY.deriveFont(20f));
        colorField.setFont(Theme.FONT_BODY.deriveFont(20f));
        typeField.setFont(Theme.FONT_BODY.deriveFont(20f));

        addRow(form, gbc, 0, "Title / Course Code", titleField);
        addRow(form, gbc, 1, "Days", createDaySelectionPanel(dayCheckboxes, days));
        addRow(form, gbc, 2, "Start Time", startField);
        addRow(form, gbc, 3, "End Time", endField);
        addRow(form, gbc, 4, "Color", createColorPickerRow(colorField, colorPreview));
        addRow(form, gbc, 5, "Type (Optional)", typeField);

        return form;
    }

    private static JPanel createColorPickerRow(JComboBox<String> colorField, JPanel colorPreview) {
        JPanel row = new JPanel(new BorderLayout(8, 0));
        row.setOpaque(false);
        row.add(colorField, BorderLayout.CENTER);
        row.add(colorPreview, BorderLayout.EAST);
        return row;
    }

    private static JPanel createColorPreviewSwatch(String colorName) {
        JPanel swatch = new JPanel();
        swatch.setPreferredSize(new Dimension(28, 20));
        swatch.setBorder(BorderFactory.createLineBorder(new Color(120, 120, 120)));
        updateColorPreview(swatch, colorName);
        return swatch;
    }

    private static void updateColorPreview(JPanel swatch, String colorName) {
        swatch.setBackground(getColorByName(colorName));
    }

    private static Color getColorByName(String colorName) {
        if (colorName == null) return Theme.BLOCK_BLUE;
        Color color = PRESET_COLORS.get(colorName);
        return color == null ? Theme.BLOCK_BLUE : color;
    }

    private static String findColorName(Color color) {
        if (color == null) return null;
        for (Map.Entry<String, Color> e : PRESET_COLORS.entrySet()) {
            if (e.getValue().equals(color)) return e.getKey();
        }
        return null;
    }

    private static Map<String, JCheckBox> createDayCheckboxes(String[] days) {
        Map<String, JCheckBox> checkboxes = new LinkedHashMap<>();
        for (String day : days) {
            checkboxes.put(day, new JCheckBox(day));
        }
        return checkboxes;
    }

    private static JPanel createDaySelectionPanel(Map<String, JCheckBox> dayCheckboxes, String[] days) {
        JPanel dayPanel = new JPanel(new GridLayout(0, 2, 6, 2));
        dayPanel.setOpaque(false);

        for (String day : days) {
            JCheckBox checkBox = dayCheckboxes.get(day);
            checkBox.setOpaque(false);
            checkBox.setFont(Theme.FONT_BODY.deriveFont(20f));
            checkBox.setForeground(Theme.BRAND_BROWN);
            dayPanel.add(checkBox);
        }

        return dayPanel;
    }

    private static List<String> getSelectedDays(Map<String, JCheckBox> dayCheckboxes, String[] days) {
        List<String> selectedDays = new ArrayList<>();
        for (String day : days) {
            JCheckBox checkBox = dayCheckboxes.get(day);
            if (checkBox != null && checkBox.isSelected()) {
                selectedDays.add(day);
            }
        }
        return selectedDays;
    }

    private static void addRow(JPanel panel, GridBagConstraints gbc, int row, String label, Component field) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0;

        JLabel labelComponent = new JLabel(label + ":");
        labelComponent.setFont(Theme.FONT_BODY.deriveFont(20f));
        labelComponent.setForeground(Theme.BRAND_BROWN);
        panel.add(labelComponent, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        panel.add(field, gbc);
    }

    private static String validateBlock(String title, List<String> selectedDays, String startInput, String endInput) {
        if (title.isBlank()) return "Title / Course Code is required.";
        if (selectedDays.isEmpty()) return "Please select at least one day.";
        if (selectedDays.size() > 2) return "Please select up to two days for this block.";
        if (startInput == null || endInput == null || startInput.isBlank() || endInput.isBlank()) {
            return "Start time and end time are required.";
        }

        LocalTime start = parseTimeFlexible(startInput);
        LocalTime end = parseTimeFlexible(endInput);

        if (start == null || end == null) {
            return "Invalid time format. Use values like 9:00 AM.";
        }

        if (!end.isAfter(start)) {
            return "End time must be after start time.";
        }

        return null;
    }

    private static LocalTime parseTimeFlexible(String time) {
        LocalTime parsed = tryParseTime(time, "H:mm");
        if (parsed == null) parsed = tryParseTime(time, "h:mm");
        if (parsed == null) parsed = tryParseTime(time, "h:mm a");
        return parsed;
    }

    private static LocalTime tryParseTime(String value, String pattern) {
        try {
            return LocalTime.parse(
                    value.toUpperCase(Locale.ENGLISH),
                    DateTimeFormatter.ofPattern(pattern, Locale.ENGLISH)
            );
        } catch (DateTimeParseException ignored) {
            return null;
        }
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
}