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

/**
 * Utility class for displaying and validating the timetable block form dialog.
 * Collects user input for creating or editing a timetable block.
 */
public final class BlockDialog {

    // Display format used for time values shown in the dialog.
    private static final DateTimeFormatter DISPLAY_TIME_FORMAT =
            DateTimeFormatter.ofPattern("h:mm a", Locale.ENGLISH);

    // Named preset colors available in the block color dropdown.
    private static final Map<String, Color> PRESET_COLORS = createPresetColors();

    /**
     * Prevents instantiation of this utility class.
     */
    private BlockDialog() {}

    /**
     * Shows the block dialog, validates the entered data,
     * and returns a populated BlockFormData object if confirmed.
     *
     * @param parent the parent component for the dialog
     * @param dialogTitle the dialog title
     * @param initial the initial values used to pre-fill the form
     * @param days the ordered list of selectable day names
     * @return the completed BlockFormData, or null if the dialog is cancelled
     */
    public static BlockFormData show(Component parent, String dialogTitle, BlockFormData initial, String[] days) {
        JTextField courseCodeField = new JTextField(initial.courseCode, 30);
        JTextField courseNameField = new JTextField(initial.courseName, 30);
        JTextField facultyField = new JTextField(initial.faculty, 30);
        JTextField termField = new JTextField(initial.term, 30);

        JTextField sectionCodeField = new JTextField(initial.sectionCode, 16);

        JComboBox<String> sectionTypeField = new JComboBox<>(new String[] {
                "LECTURE", "LAB", "TUTORIAL", "SEMINAR"
        });
        sectionTypeField.setSelectedItem(
                initial.sectionType == null || initial.sectionType.isBlank()
                        ? "LECTURE"
                        : initial.sectionType.toUpperCase(Locale.ENGLISH)
        );

        JTextField instructorField = new JTextField(initial.instructor, 16);
        JTextField locationField = new JTextField(initial.location, 16);

        // Build the day checkbox map and pre-select any days from the initial data.
        Map<String, JCheckBox> dayCheckboxes = createDayCheckboxes(days);
        for (String d : initial.days) {
            JCheckBox cb = dayCheckboxes.get(d);
            if (cb != null) {
                cb.setSelected(true);
            }
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

        // Small swatch used to preview the currently selected block color.
        JPanel colorPreview = createColorPreviewSwatch((String) colorField.getSelectedItem());
        colorField.addActionListener(e -> updateColorPreview(colorPreview, (String) colorField.getSelectedItem()));

        JPanel formPanel = buildFormPanel(
                courseCodeField,
                courseNameField,
                facultyField,
                termField,
                sectionCodeField,
                sectionTypeField,
                instructorField,
                locationField,
                dayCheckboxes,
                startField,
                endField,
                colorField,
                colorPreview,
                days
        );

        // Keep showing the dialog until valid input is entered or the user cancels.
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

            String courseCode = courseCodeField.getText().trim();
            String courseName = courseNameField.getText().trim();
            String faculty = facultyField.getText().trim();
            String term = termField.getText().trim();

            String sectionCode = sectionCodeField.getText().trim();
            String sectionType = ((String) sectionTypeField.getSelectedItem()).trim();
            String instructor = instructorField.getText().trim();
            String location = locationField.getText().trim();

            List<String> selectedDays = getSelectedDays(dayCheckboxes, days);
            String startInput = (String) startField.getSelectedItem();
            String endInput = (String) endField.getSelectedItem();
            String colorName = (String) colorField.getSelectedItem();

            String validationError = validateBlock(
                    courseCode,
                    courseName,
                    sectionCode,
                    term,
                    selectedDays,
                    startInput,
                    endInput
            );

            if (validationError != null) {
                JOptionPane.showMessageDialog(parent, validationError, "Validation Error", JOptionPane.ERROR_MESSAGE);
                continue;
            }

            LocalTime start = parseTimeFlexible(startInput);
            LocalTime end = parseTimeFlexible(endInput);
            Color color = getColorByName(colorName);

            return new BlockFormData(
                    courseCode,
                    courseName,
                    faculty,
                    term,
                    sectionCode,
                    sectionType,
                    instructor,
                    location,
                    selectedDays,
                    start,
                    end,
                    color
            );
        }
    }

    /**
     * Converts a string into a SectionType, defaulting to LECTURE
     * if the input is null, blank, or invalid.
     *
     * @param input the input section type text
     * @return the resolved SectionType
     */
    public static SectionType parseSectionTypeOrDefault(String input) {
        if (input == null || input.isBlank()) return SectionType.LECTURE;
        try {
            return SectionType.fromString(input);
        } catch (Exception ignored) {
            return SectionType.LECTURE;
        }
    }

    /**
     * Builds the full form panel used inside the dialog.
     *
     * @param courseCodeField the course code field
     * @param courseNameField the course name field
     * @param facultyField the faculty field
     * @param termField the term field
     * @param sectionCodeField the section code field
     * @param sectionTypeField the section type dropdown
     * @param instructorField the instructor field
     * @param locationField the location field
     * @param dayCheckboxes the day checkbox map
     * @param startField the start time dropdown
     * @param endField the end time dropdown
     * @param colorField the color dropdown
     * @param colorPreview the color preview panel
     * @param days the ordered list of day names
     * @return the assembled form panel
     */
    private static JPanel buildFormPanel(
            JTextField courseCodeField,
            JTextField courseNameField,
            JTextField facultyField,
            JTextField termField,
            JTextField sectionCodeField,
            JComboBox<String> sectionTypeField,
            JTextField instructorField,
            JTextField locationField,
            Map<String, JCheckBox> dayCheckboxes,
            JComboBox<String> startField,
            JComboBox<String> endField,
            JComboBox<String> colorField,
            JPanel colorPreview,
            String[] days
    ) {
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Theme.BRAND_OFFWHITE);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        // Apply shared font styling to the text fields and dropdowns.
        courseCodeField.setFont(Theme.FONT_BODY.deriveFont(20f));
        courseNameField.setFont(Theme.FONT_BODY.deriveFont(20f));
        facultyField.setFont(Theme.FONT_BODY.deriveFont(20f));
        termField.setFont(Theme.FONT_BODY.deriveFont(20f));

        sectionCodeField.setFont(Theme.FONT_BODY.deriveFont(20f));
        sectionTypeField.setFont(Theme.FONT_BODY.deriveFont(20f));
        instructorField.setFont(Theme.FONT_BODY.deriveFont(20f));
        locationField.setFont(Theme.FONT_BODY.deriveFont(20f));

        startField.setFont(Theme.FONT_BODY.deriveFont(20f));
        endField.setFont(Theme.FONT_BODY.deriveFont(20f));
        colorField.setFont(Theme.FONT_BODY.deriveFont(20f));

        addRow(form, gbc, 0, "Course Code", courseCodeField);
        addRow(form, gbc, 1, "Course Name", courseNameField);
        addRow(form, gbc, 2, "Faculty", facultyField);
        addRow(form, gbc, 3, "Term", termField);

        addRow(form, gbc, 4, "Section Code", sectionCodeField);
        addRow(form, gbc, 5, "Section Type", sectionTypeField);
        addRow(form, gbc, 6, "Instructor", instructorField);
        addRow(form, gbc, 7, "Location", locationField);

        addRow(form, gbc, 8, "Days", createDaySelectionPanel(dayCheckboxes, days));
        addRow(form, gbc, 9, "Start Time", startField);
        addRow(form, gbc, 10, "End Time", endField);
        addRow(form, gbc, 11, "Color", createColorPickerRow(colorField, colorPreview));

        return form;
    }

    /**
     * Builds the combined color picker row containing the dropdown
     * and the live preview swatch.
     *
     * @param colorField the color dropdown
     * @param colorPreview the preview swatch
     * @return the assembled color picker row
     */
    private static JPanel createColorPickerRow(JComboBox<String> colorField, JPanel colorPreview) {
        JPanel row = new JPanel(new BorderLayout(8, 0));
        row.setOpaque(false);
        row.add(colorField, BorderLayout.CENTER);
        row.add(colorPreview, BorderLayout.EAST);
        return row;
    }

    /**
     * Creates the preview swatch used to show the selected color.
     *
     * @param colorName the initial color name
     * @return the preview swatch panel
     */
    private static JPanel createColorPreviewSwatch(String colorName) {
        JPanel swatch = new JPanel();
        swatch.setPreferredSize(new Dimension(28, 20));
        swatch.setBorder(BorderFactory.createLineBorder(new Color(120, 120, 120)));
        updateColorPreview(swatch, colorName);
        return swatch;
    }

    /**
     * Updates the preview swatch background to match the selected color.
     *
     * @param swatch the preview panel
     * @param colorName the selected color name
     */
    private static void updateColorPreview(JPanel swatch, String colorName) {
        swatch.setBackground(getColorByName(colorName));
    }

    /**
     * Resolves a preset color by its display name.
     *
     * @param colorName the color name
     * @return the matching Color, or the default block blue if not found
     */
    private static Color getColorByName(String colorName) {
        if (colorName == null) return Theme.BLOCK_BLUE;
        Color color = PRESET_COLORS.get(colorName);
        return color == null ? Theme.BLOCK_BLUE : color;
    }

    /**
     * Finds the preset color name that matches a given Color object.
     *
     * @param color the color to match
     * @return the preset name, or null if none matches
     */
    private static String findColorName(Color color) {
        if (color == null) return null;
        for (Map.Entry<String, Color> entry : PRESET_COLORS.entrySet()) {
            if (entry.getValue().equals(color)) return entry.getKey();
        }
        return null;
    }

    /**
     * Creates a checkbox for each day name and stores them in order.
     *
     * @param days the ordered list of day names
     * @return a map of day names to checkboxes
     */
    private static Map<String, JCheckBox> createDayCheckboxes(String[] days) {
        Map<String, JCheckBox> checkboxes = new LinkedHashMap<>();
        for (String day : days) {
            checkboxes.put(day, new JCheckBox(day));
        }
        return checkboxes;
    }

    /**
     * Builds the day selection panel containing all day checkboxes.
     *
     * @param dayCheckboxes the day checkbox map
     * @param days the ordered list of day names
     * @return the assembled day selection panel
     */
    private static JPanel createDaySelectionPanel(Map<String, JCheckBox> dayCheckboxes, String[] days) {
        JPanel dayPanel = new JPanel(new GridLayout(0, 4, 6, 2));
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

    /**
     * Collects the currently selected day names from the checkbox map.
     *
     * @param dayCheckboxes the day checkbox map
     * @param days the ordered list of day names
     * @return a list of selected day names
     */
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

    /**
     * Adds a labeled row to the form layout.
     *
     * @param panel the form panel
     * @param gbc the shared GridBagConstraints object
     * @param row the row index
     * @param label the row label text
     * @param field the input component
     */
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

    // Valid academic terms accepted by the form validation.
    private static final List<String> VALID_TERMS = List.of(
            "fall", "winter", "spring", "summer"
    );

    /**
     * Validates the entered form data and returns an error message if invalid.
     *
     * @param courseCode the entered course code
     * @param courseName the entered course name
     * @param sectionCode the entered section code
     * @param term the entered term
     * @param selectedDays the selected days
     * @param startInput the selected start time text
     * @param endInput the selected end time text
     * @return an error message if invalid, otherwise null
     */
    private static String validateBlock(
            String courseCode,
            String courseName,
            String sectionCode,
            String term,
            List<String> selectedDays,
            String startInput,
            String endInput
    ) {
        if (courseCode.isBlank()) return "Course code is required.";
        if (!courseCode.matches(".*[a-zA-Z].*")) {
            return "Course code must contain at least one letter (e.g. COMP 1410).";
        }

        if (courseName.isBlank()) return "Course name is required.";
        if (!courseName.matches(".*[a-zA-Z].*")) {
            return "Course name must contain at least one letter.";
        }

        if (sectionCode.isBlank()) return "Section code is required.";
        if (!sectionCode.matches(".*[a-zA-Z0-9].*")) {
            return "Section code must contain letters or numbers.";
        }

        if (term != null && !term.isBlank()
                && !VALID_TERMS.contains(term.strip().toLowerCase(Locale.ENGLISH))) {
            return "Term must be one of: Fall, Winter, Spring, or Summer.";
        }

        if (selectedDays.isEmpty()) return "Please select at least one day.";

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

    /**
     * Attempts to parse a time string using several supported formats.
     *
     * @param time the input time string
     * @return the parsed LocalTime, or null if parsing fails
     */
    private static LocalTime parseTimeFlexible(String time) {
        LocalTime parsed = tryParseTime(time, "H:mm");
        if (parsed == null) parsed = tryParseTime(time, "h:mm");
        if (parsed == null) parsed = tryParseTime(time, "h:mm a");
        return parsed;
    }

    /**
     * Attempts to parse a time string using a specific pattern.
     *
     * @param value the input value
     * @param pattern the formatter pattern
     * @return the parsed LocalTime, or null if parsing fails
     */
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

    /**
     * Creates the dropdown time options shown in 30-minute intervals.
     *
     * @return an array of display time strings
     */
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

    /**
     * Creates the preset color map used in the color dropdown.
     *
     * @return a map of color names to Color values
     */
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