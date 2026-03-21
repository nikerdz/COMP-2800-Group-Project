package com.coursely.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Insets;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import com.coursely.model.Schedule;
import com.coursely.model.Section;
import com.coursely.model.TimeBlock;

public class TimetablePanel extends JPanel {

    public static final String[] DAYS = {
            "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"
    };

    public static final String[] TIME_SLOTS = {
            "8:00 AM", "9:00 AM", "10:00 AM", "11:00 AM", "12:00 PM", "1:00 PM",
            "2:00 PM", "3:00 PM", "4:00 PM", "5:00 PM", "6:00 PM"
    };

    private static final Color PANEL_BG_COLOR = Theme.BRAND_OFFWHITE;

    private final Schedule schedule = new Schedule();

    // Temporary UI-only mapping until Course objects / DB wiring are integrated.
    private final Map<String, String> courseCodeByUiId = new LinkedHashMap<>();

    private String selectedSectionUiId;

    private JLabel timetableTitleLabel;
    private TimetableGridPanel gridPanel;
    private TimetableBlockLayer blockLayer;
    private SectionDetailsPanel detailsPanel;

    public TimetablePanel() {
        setLayout(new BorderLayout(0, 12));
        setBackground(PANEL_BG_COLOR);

        add(buildHeaderPanel(), BorderLayout.NORTH);
        add(buildTimetableArea(), BorderLayout.CENTER);
    }

    private JPanel buildHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        headerPanel.setBorder(new EmptyBorder(4, 2, 8, 2));

        JPanel leftPanel = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 8, 0));
        leftPanel.setOpaque(false);

        timetableTitleLabel = new JLabel("Weekly Timetable");
        timetableTitleLabel.setFont(Theme.FONT_HEADING.deriveFont(Theme.SIZE_HEADING));
        timetableTitleLabel.setForeground(Theme.BRAND_BROWN);
        timetableTitleLabel.setHorizontalAlignment(SwingConstants.LEFT);

        JButton editTitleButton = createIconButton("/images/edit-btn.png");
        editTitleButton.setToolTipText("Edit schedule title");
        editTitleButton.addActionListener(e -> editTimetableTitle());

        leftPanel.add(timetableTitleLabel);
        leftPanel.add(editTitleButton);

        JPanel rightPanel = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 10, 0));
        rightPanel.setOpaque(false);

        JButton saveScheduleButton = new JButton("Save Schedule");
        styleHeaderButton(saveScheduleButton);
        saveScheduleButton.addActionListener(e -> saveSchedule());

        JButton loadScheduleButton = new JButton("Load Schedule");
        styleHeaderButton(loadScheduleButton);
        loadScheduleButton.addActionListener(e -> loadSchedule());

        JButton exportScheduleButton = new JButton("Export Schedule");
        styleHeaderButton(exportScheduleButton);
        exportScheduleButton.addActionListener(e -> exportSchedule());

        JButton addBlockButton = new JButton("Add Block");
        styleHeaderButton(addBlockButton);
        addBlockButton.addActionListener(e -> addBlock());

        rightPanel.add(saveScheduleButton);
        rightPanel.add(loadScheduleButton);
        rightPanel.add(exportScheduleButton);
        rightPanel.add(addBlockButton);

        headerPanel.add(leftPanel, BorderLayout.WEST);
        headerPanel.add(rightPanel, BorderLayout.EAST);

        return headerPanel;
    }

    private JPanel buildTimetableArea() {
        JPanel area = new JPanel(new BorderLayout());
        area.setOpaque(false);

        gridPanel = new TimetableGridPanel(DAYS, TIME_SLOTS);

        blockLayer = new TimetableBlockLayer(
                DAYS,
                TIME_SLOTS,
                () -> schedule.getSections(),
                this::getCourseCodeForSection,
                this::isSelected,
                this::selectSection,
                this::clearSelection,
                this::editBlock,
                this::deleteBlock
        );

        detailsPanel = new SectionDetailsPanel(this::editBlock, this::deleteBlock);
        detailsPanel.setVisible(false);

        TimetableLayeredPane layeredPane = new TimetableLayeredPane(gridPanel, blockLayer, detailsPanel);
        area.add(layeredPane, BorderLayout.CENTER);

        return area;
    }

    private void addBlock() {
        BlockFormData data = BlockDialog.show(
                this,
                "Add Timetable Block",
                BlockFormData.empty(),
                DAYS
        );
        if (data == null) return;

        Section section = new Section(
                null,
                null,
                data.sectionCode,
                BlockDialog.parseSectionTypeOrDefault(data.sectionType),
                data.instructor,
                data.location,
                data.color
        );

        for (String day : data.days) {
            section.addTimeBlock(new TimeBlock(day, data.start, data.end));
        }

        schedule.addSection(section);

        // Temporary until full Course model / DB wiring is in place.
        courseCodeByUiId.put(section.getUiId(), data.courseCode);

        selectedSectionUiId = section.getUiId();
        refreshView();
    }

    private void editBlock(String sectionUiId) {
        Section section = schedule.findSectionByUiId(sectionUiId).orElse(null);
        if (section == null) return;

        BlockFormData initial = BlockFormData.fromSection(
                section,
                courseCodeByUiId.getOrDefault(sectionUiId, "")
        );

        BlockFormData updated = BlockDialog.show(
                this,
                "Edit Timetable Block",
                initial,
                DAYS
        );
        if (updated == null) return;

        section.setSectionCode(updated.sectionCode);
        section.setColor(updated.color);
        section.setSectionType(BlockDialog.parseSectionTypeOrDefault(updated.sectionType));
        section.setInstructor(updated.instructor);
        section.setLocation(updated.location);

        courseCodeByUiId.put(sectionUiId, updated.courseCode);

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
        courseCodeByUiId.remove(sectionUiId);

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

    private String getCourseCodeForSection(String sectionUiId) {
        return courseCodeByUiId.getOrDefault(sectionUiId, "");
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

        String courseCode = courseCodeByUiId.getOrDefault(selectedSectionUiId, "");
        detailsPanel.bind(section, courseCode);
        detailsPanel.setVisible(true);
    }

    private void editTimetableTitle() {
        String newTitle = JOptionPane.showInputDialog(
                this,
                "Enter new schedule title:",
                timetableTitleLabel.getText()
        );

        if (newTitle != null && !newTitle.trim().isBlank()) {
            timetableTitleLabel.setText(newTitle.trim());
            revalidate();
            repaint();
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

    private void styleHeaderButton(JButton btn) {
        btn.setBackground(Theme.BRAND_OFFWHITE);
        btn.setForeground(Theme.BRAND_BROWN);
        btn.setFocusPainted(false);
        btn.setOpaque(true);
        btn.setContentAreaFilled(true);
        btn.setFont(Theme.FONT_BODY.deriveFont(22f));
        btn.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(Theme.BRAND_BLUE, 2, true),
                BorderFactory.createEmptyBorder(10, 20, 10, 20)
        ));
    }

    private JButton createIconButton(String imagePath) {
        JButton button = new JButton();
        button.setIcon(ResourceUtils.loadIcon(imagePath, 18, 18));
        button.setFocusPainted(false);
        button.setOpaque(false);
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        button.setMargin(new Insets(0, 0, 0, 0));
        button.setBorder(BorderFactory.createEmptyBorder());
        return button;
    }

    public static String formatRange(java.time.LocalTime start, java.time.LocalTime end) {
        java.time.format.DateTimeFormatter formatter =
                java.time.format.DateTimeFormatter.ofPattern("h:mm a", java.util.Locale.ENGLISH);
        return start.format(formatter) + " - " + end.format(formatter);
    }
}
