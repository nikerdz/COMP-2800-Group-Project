package com.coursely.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
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
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import com.coursely.model.Schedule;
import com.coursely.model.Section;
import com.coursely.model.SectionType;
import com.coursely.model.TimeBlock;

public class TimetablePanel extends JPanel {

    public static final String[] DAYS = {
            "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"
    };

    public static final String[] TIME_SLOTS = {
            "8:00 AM", "9:00 AM", "10:00 AM", "11:00 AM", "12:00 PM", "1:00 PM",
            "2:00 PM", "3:00 PM", "4:00 PM", "5:00 PM", "6:00 PM"
    };

    public static final DateTimeFormatter SLOT_FORMAT =
            DateTimeFormatter.ofPattern("h:mm a", Locale.ENGLISH);

    public static final DateTimeFormatter DISPLAY_TIME_FORMAT =
            DateTimeFormatter.ofPattern("h:mm a", Locale.ENGLISH);

    private static final Color PANEL_BG_COLOR = Theme.BRAND_OFFWHITE;
    private static final Color BLOCK_DEFAULT_COLOR = Theme.BLOCK_BLUE;

    private static final Map<String, Color> PRESET_COLORS = createPresetColors();

    private final Schedule schedule = new Schedule();
    private final Map<String, String> displayTypeByUiId = new LinkedHashMap<>();

    private String selectedSectionUiId;

    private TimetableGridPanel gridPanel;
    private TimetableBlockLayer blockLayer;
    private SectionDetailsPanel detailsPanel;

    public TimetablePanel() {
        setLayout(new BorderLayout(0, 12));
        setBackground(PANEL_BG_COLOR);

        JPanel headerPanel = buildHeaderPanel();
        add(headerPanel, BorderLayout.NORTH);

        add(buildTimetableArea(), BorderLayout.CENTER);
    }

    private JPanel buildHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        headerPanel.setBorder(new EmptyBorder(4, 2, 8, 2));

        // Left side: title + edit icon
        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        leftPanel.setOpaque(false);

        JLabel title = new JLabel("Weekly Schedule");
        title.setFont(Theme.FONT_HEADING.deriveFont(Theme.SIZE_HEADING));
        title.setForeground(Theme.BRAND_BROWN);

        JButton editTitleButton = createIconButton("/images/edit-btn.png");
        editTitleButton.setToolTipText("Edit timetable title");
        editTitleButton.addActionListener(e -> editTimetableTitle());

        leftPanel.add(title);
        leftPanel.add(editTitleButton);

        // Right side: action buttons
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        rightPanel.setOpaque(false);

        JButton loadScheduleButton = new JButton("Load Schedule");
        stylePrimaryButton(loadScheduleButton);
        loadScheduleButton.addActionListener(e -> loadSchedule());

        JButton saveScheduleButton = new JButton("Save Schedule");
        stylePrimaryButton(saveScheduleButton);
        saveScheduleButton.addActionListener(e -> saveSchedule());

        JButton exportScheduleButton = new JButton("Export Schedule");
        stylePrimaryButton(exportScheduleButton);
        exportScheduleButton.addActionListener(e -> exportSchedule());

        JButton addBlockButton = new JButton("Add Block");
        stylePrimaryButton(addBlockButton);
        addBlockButton.addActionListener(e -> addBlock());

        rightPanel.add(saveScheduleButton);
        rightPanel.add(loadScheduleButton);
        rightPanel.add(exportScheduleButton);
        rightPanel.add(addBlockButton);

        headerPanel.add(leftPanel, BorderLayout.WEST);
        headerPanel.add(rightPanel, BorderLayout.EAST);

        return headerPanel;
    }

     private JButton createIconButton(String imagePath) {
        java.net.URL resource = getClass().getResource(imagePath);
        JButton button = new JButton();

        if (resource != null) {
            javax.swing.ImageIcon originalIcon = new javax.swing.ImageIcon(resource);
            java.awt.Image scaled = originalIcon.getImage().getScaledInstance(25, 25, java.awt.Image.SCALE_SMOOTH);
            button.setIcon(new javax.swing.ImageIcon(scaled));
        }

        button.setFocusPainted(false);
        button.setOpaque(false);
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        button.setMargin(new Insets(0, 0, 0, 0));
        button.setBorder(BorderFactory.createEmptyBorder());

        return button;
    }


    private void editTimetableTitle() {
        String newTitle = JOptionPane.showInputDialog(
                this,
                "Enter new schedule title:",
                "Edit Schedule Title",
                JOptionPane.PLAIN_MESSAGE
        );

        if (newTitle != null && !newTitle.trim().isBlank()) {
            // for now just show confirmation or store it in a field
            JOptionPane.showMessageDialog(this, "Title updated to: " + newTitle.trim());
        }
    }

    private void saveSchedule() {
        JOptionPane.showMessageDialog(this, "Save Schedule clicked.");
    }

    private void loadSchedule() {
        JOptionPane.showMessageDialog(this, "Load Schedule clicked.");
    }

    private void exportSchedule() {
        JOptionPane.showMessageDialog(this, "Export Schedule clicked.");
    }

    private JPanel buildTimetableArea() {
        JPanel area = new JPanel(new BorderLayout());
        area.setOpaque(false);

        gridPanel = new TimetableGridPanel(DAYS, TIME_SLOTS);

        blockLayer = new TimetableBlockLayer(
                DAYS,
                TIME_SLOTS,
                () -> schedule.getSections(),
                this::getDisplayTypeForSection,
                this::isSelected,
                this::selectSection,
                this::clearSelection,
                this::editBlock,
                this::deleteBlock
        );

        detailsPanel = new SectionDetailsPanel(this::editBlock, this::deleteBlock);
        detailsPanel.setVisible(false);

        JLayeredPane layeredPane = new JLayeredPane() {
            @Override
            public void doLayout() {
                int width = getWidth();
                int height = getHeight();

                if (gridPanel != null) {
                    gridPanel.setBounds(0, 0, width, height);
                }

                if (blockLayer != null) {
                    blockLayer.setBounds(0, 0, width, height);
                }

                if (detailsPanel != null) {
                    int panelWidth = 250;
                    int panelHeight = 185;
                    int margin = 14;

                    detailsPanel.setBounds(
                            width - panelWidth - margin,
                            height - panelHeight - margin,
                            panelWidth,
                            panelHeight
                    );
                }
            }

            @Override
            public Dimension getPreferredSize() {
                return gridPanel.getPreferredSize();
            }
        };

        layeredPane.add(gridPanel, Integer.valueOf(0));
        layeredPane.add(blockLayer, Integer.valueOf(1));
        layeredPane.add(detailsPanel, Integer.valueOf(2));

        area.add(layeredPane, BorderLayout.CENTER);
        return area;
    }

    private void stylePrimaryButton(JButton btn) {
        btn.setBackground(Theme.BRAND_OFFWHITE);
        btn.setForeground(Theme.BRAND_BROWN);
        btn.setFocusPainted(false);
        btn.setOpaque(true);
        btn.setContentAreaFilled(true);
        btn.setFont(Theme.FONT_BODY.deriveFont(25f));
        btn.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(Theme.BRAND_BLUE, 2, true),
                BorderFactory.createEmptyBorder(6, 14, 6, 14)
        ));
    }

    private void addBlock() {
        BlockFormData data = showBlockDialog("Add Timetable Block", BlockFormData.empty());
        if (data == null) return;

        Section section = new Section(
                null,
                null,
                data.title,
                parseSectionTypeOrDefault(data.typeText),
                null,
                null,
                data.color
        );

        for (String day : data.days) {
            section.addTimeBlock(new TimeBlock(day, data.start, data.end));
        }

        schedule.addSection(section);
        displayTypeByUiId.put(section.getUiId(), data.typeText);

        selectedSectionUiId = section.getUiId();
        refreshView();
    }

    private void editBlock(String sectionUiId) {
        Section section = schedule.findSectionByUiId(sectionUiId).orElse(null);
        if (section == null) return;

        BlockFormData initial = BlockFormData.fromSection(
                section,
                displayTypeByUiId.getOrDefault(sectionUiId, "")
        );

        BlockFormData updated = showBlockDialog("Edit Timetable Block", initial);
        if (updated == null) return;

        section.setSectionCode(updated.title);
        section.setColor(updated.color);
        section.setSectionType(parseSectionTypeOrDefault(updated.typeText));
        displayTypeByUiId.put(sectionUiId, updated.typeText);

        section.clearTimeBlocks();
        for (String day : updated.days) {
            section.addTimeBlock(new TimeBlock(day, updated.start, updated.end));
        }

        refreshView();
    }

    private void deleteBlock(String sectionUiId) {
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Delete this block?",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION
        );

        if (confirm != JOptionPane.YES_OPTION) return;

        schedule.removeSectionByUiId(sectionUiId);
        displayTypeByUiId.remove(sectionUiId);

        if (sectionUiId.equals(selectedSectionUiId)) {
            selectedSectionUiId = null;
        }

        refreshView();
    }

    private void selectSection(String sectionUiId) {
        selectedSectionUiId = sectionUiId;
        refreshView();
    }

    private void clearSelection() {
        selectedSectionUiId = null;
        refreshView();
    }

    private boolean isSelected(String sectionUiId) {
        return sectionUiId != null && sectionUiId.equals(selectedSectionUiId);
    }

    private String getDisplayTypeForSection(String sectionUiId) {
        return displayTypeByUiId.getOrDefault(sectionUiId, "");
    }

    private void refreshView() {
        blockLayer.rebuildBlocks();
        updateDetailsPanel();
        revalidate();
        repaint();
    }

    private void updateDetailsPanel() {
        if (selectedSectionUiId == null) {
            detailsPanel.setVisible(false);
            return;
        }

        Section section = schedule.findSectionByUiId(selectedSectionUiId).orElse(null);
        if (section == null) {
            detailsPanel.setVisible(false);
            return;
        }

        detailsPanel.bind(section, displayTypeByUiId.getOrDefault(selectedSectionUiId, ""));
        detailsPanel.setVisible(true);
    }

    private BlockFormData showBlockDialog(String dialogTitle, BlockFormData initial) {
        javax.swing.JTextField titleField = new javax.swing.JTextField(initial.title, 16);
        javax.swing.JTextField typeField = new javax.swing.JTextField(initial.typeText, 16);

        Map<String, JCheckBox> dayCheckboxes = createDayCheckboxes();
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
                titleField,
                dayCheckboxes,
                startField,
                endField,
                colorField,
                colorPreview,
                typeField
        );

        while (true) {
            int choice = JOptionPane.showConfirmDialog(
                    this,
                    formPanel,
                    dialogTitle,
                    JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.PLAIN_MESSAGE
            );

            if (choice != JOptionPane.OK_OPTION) return null;

            String title = titleField.getText().trim();
            List<String> days = getSelectedDays(dayCheckboxes);
            String startInput = (String) startField.getSelectedItem();
            String endInput = (String) endField.getSelectedItem();
            String colorName = (String) colorField.getSelectedItem();
            String typeText = typeField.getText().trim();

            String validationError = validateBlock(title, days, startInput, endInput);
            if (validationError != null) {
                JOptionPane.showMessageDialog(this, validationError, "Validation Error", JOptionPane.ERROR_MESSAGE);
                continue;
            }

            LocalTime start = parseTimeFlexible(startInput);
            LocalTime end = parseTimeFlexible(endInput);
            Color color = getColorByName(colorName);

            return new BlockFormData(title, days, start, end, typeText, color);
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

        titleField.setFont(Theme.FONT_BODY.deriveFont(20f));
        startField.setFont(Theme.FONT_BODY.deriveFont(20f));
        endField.setFont(Theme.FONT_BODY.deriveFont(20f));
        colorField.setFont(Theme.FONT_BODY.deriveFont(20f));
        typeField.setFont(Theme.FONT_BODY.deriveFont(20f));

        addRow(form, gbc, 0, "Title / Course Code", titleField);
        addRow(form, gbc, 1, "Days", createDaySelectionPanel(dayCheckboxes));
        addRow(form, gbc, 2, "Start Time", startField);
        addRow(form, gbc, 3, "End Time", endField);
        addRow(form, gbc, 4, "Color", createColorPickerRow(colorField, colorPreview));
        addRow(form, gbc, 5, "Type (Optional)", typeField);

        return form;
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
        if (colorName == null) return BLOCK_DEFAULT_COLOR;
        Color color = PRESET_COLORS.get(colorName);
        return color == null ? BLOCK_DEFAULT_COLOR : color;
    }

    private String findColorName(Color color) {
        if (color == null) return null;
        for (Map.Entry<String, Color> e : PRESET_COLORS.entrySet()) {
            if (e.getValue().equals(color)) return e.getKey();
        }
        return null;
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
            checkBox.setFont(Theme.FONT_BODY.deriveFont(20f));
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

    private void addRow(JPanel panel, GridBagConstraints gbc, int row, String label, Component field) {
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

    private String validateBlock(String title, List<String> selectedDays, String startInput, String endInput) {
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

    public static String formatRange(LocalTime start, LocalTime end) {
        return start.format(DISPLAY_TIME_FORMAT) + " - " + end.format(DISPLAY_TIME_FORMAT);
    }

    private LocalTime parseTimeFlexible(String time) {
        LocalTime parsed = tryParseTime(time, "H:mm");
        if (parsed == null) parsed = tryParseTime(time, "h:mm");
        if (parsed == null) parsed = tryParseTime(time, "h:mm a");
        return parsed;
    }

    private LocalTime tryParseTime(String value, String pattern) {
        try {
            return LocalTime.parse(
                    value.toUpperCase(Locale.ENGLISH),
                    DateTimeFormatter.ofPattern(pattern, Locale.ENGLISH)
            );
        } catch (DateTimeParseException ignored) {
            return null;
        }
    }

    private SectionType parseSectionTypeOrDefault(String input) {
        if (input == null || input.isBlank()) return SectionType.LECTURE;

        try {
            return SectionType.fromString(input);
        } catch (Exception ignored) {
            return SectionType.LECTURE;
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

    private static final class BlockFormData {
        final String title;
        final List<String> days;
        final LocalTime start;
        final LocalTime end;
        final String typeText;
        final Color color;

        BlockFormData(String title, List<String> days, LocalTime start, LocalTime end, String typeText, Color color) {
            this.title = title;
            this.days = days;
            this.start = start;
            this.end = end;
            this.typeText = typeText;
            this.color = color;
        }

        static BlockFormData empty() {
            return new BlockFormData(
                    "",
                    new ArrayList<>(),
                    LocalTime.of(9, 0),
                    LocalTime.of(10, 0),
                    "",
                    Theme.BLOCK_BLUE
            );
        }

        static BlockFormData fromSection(Section section, String typeText) {
            List<String> days = new ArrayList<>();
            LocalTime start = LocalTime.of(9, 0);
            LocalTime end = LocalTime.of(10, 0);

            if (!section.getTimeBlocks().isEmpty()) {
                start = section.getTimeBlocks().get(0).getStartTime();
                end = section.getTimeBlocks().get(0).getEndTime();
            }

            for (TimeBlock tb : section.getTimeBlocks()) {
                days.add(tb.getDayOfWeek());
            }

            Color color = section.getColor() == null ? Theme.BLOCK_BLUE : section.getColor();

            return new BlockFormData(
                    section.getSectionCode(),
                    days,
                    start,
                    end,
                    typeText == null ? "" : typeText,
                    color
            );
        }
    }
}