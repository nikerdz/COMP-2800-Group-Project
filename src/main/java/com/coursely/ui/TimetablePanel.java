package com.coursely.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Insets;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import com.coursely.db.CourseDao;
import com.coursely.model.Course;
import com.coursely.model.Schedule;
import com.coursely.model.Section;
import com.coursely.model.TimeBlock;
import com.coursely.service.TimetableService;

public class TimetablePanel extends JPanel {

    public static final String[] DAYS = {
            "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"
    };

    public static final String[] TIME_SLOTS = {
            "8:00 AM", "9:00 AM", "10:00 AM", "11:00 AM", "12:00 PM", "1:00 PM",
            "2:00 PM", "3:00 PM", "4:00 PM", "5:00 PM", "6:00 PM"
    };

    private static final Color PANEL_BG_COLOR = Theme.BRAND_OFFWHITE;

    private final TimetableService timetableService = new TimetableService();
    private final CourseDao courseDao = new CourseDao();

    private Schedule schedule = new Schedule("Weekly Timetable", null);

    // Temporary UI-only mapping until Course objects are wired directly into loaded sections.
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

        timetableTitleLabel = new JLabel(schedule.getScheduleName());
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

        ensureScheduleHasBasicInfo();
        Section savedSection = timetableService.addBlockToSchedule(schedule, data);

        courseCodeByUiId.put(savedSection.getUiId(), data.courseCode);
        selectedSectionUiId = savedSection.getUiId();

        refreshView();
    }

    private void editBlock(String sectionUiId) {
        Section section = schedule.findSectionByUiId(sectionUiId).orElse(null);
        if (section == null) return;

        String courseCode = courseCodeByUiId.getOrDefault(sectionUiId, "");
        Course course = section.getCourseId() != null
                ? courseDao.findById(section.getCourseId())
                : null;

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

        BlockFormData initial = new BlockFormData(
                courseCode,
                course != null ? course.getCourseName() : "",
                course != null && course.getFaculty() != null ? course.getFaculty() : "",
                course != null && course.getTerm() != null ? course.getTerm() : "",
                section.getSectionCode() == null ? "" : section.getSectionCode(),
                section.getSectionType() == null ? "LECTURE" : section.getSectionType().name(),
                section.getInstructor() == null ? "" : section.getInstructor(),
                section.getLocation() == null ? "" : section.getLocation(),
                days,
                start,
                end,
                section.getColor() == null ? Theme.BLOCK_BLUE : section.getColor()
        );

        BlockFormData updated = BlockDialog.show(this, "Edit Timetable Block", initial, DAYS);
        if (updated == null) return;

        ensureScheduleHasBasicInfo();
        timetableService.updateBlock(section, updated, schedule);
        courseCodeByUiId.put(sectionUiId, updated.courseCode);
        refreshView();
    }

    private void deleteBlock(String sectionUiId) {
        Section section = schedule.findSectionByUiId(sectionUiId).orElse(null);
        if (section == null) return;

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Delete this block?",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION
        );
        if (confirm != JOptionPane.YES_OPTION) return;

        if (schedule.getScheduleId() != null && section.getSectionId() != null) {
            timetableService.deleteBlock(schedule, section);
        } else {
            schedule.removeSectionByUiId(sectionUiId);
        }

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
            schedule.setScheduleName(newTitle.trim());
            timetableTitleLabel.setText(newTitle.trim());
            revalidate();
            repaint();
        }
    }

    private void saveSchedule() {
        ensureScheduleHasBasicInfo();

        try {
            timetableService.saveScheduleDetails(schedule);

            if (schedule.getScheduleId() == null) {
                JOptionPane.showMessageDialog(this, "Schedule saved.");
            } else {
                JOptionPane.showMessageDialog(
                        this,
                        "Schedule saved.\nSchedule ID: " + schedule.getScheduleId()
                );
            }
        } catch (RuntimeException e) {
            JOptionPane.showMessageDialog(
                    this,
                    "Failed to save schedule:\n" + e.getMessage(),
                    "Save Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void loadSchedule() {
        String input = JOptionPane.showInputDialog(
                this,
                "Enter schedule ID to load:",
                "Load Schedule",
                JOptionPane.PLAIN_MESSAGE
        );

        if (input == null || input.trim().isBlank()) return;

        try {
            int scheduleId = Integer.parseInt(input.trim());
            Schedule loaded = timetableService.loadSchedule(scheduleId);

            if (loaded == null) {
                JOptionPane.showMessageDialog(
                        this,
                        "No schedule found with ID " + scheduleId,
                        "Not Found",
                        JOptionPane.WARNING_MESSAGE
                );
                return;
            }

            schedule = loaded;
            selectedSectionUiId = null;
            timetableTitleLabel.setText(
                    schedule.getScheduleName() == null || schedule.getScheduleName().isBlank()
                            ? "Weekly Timetable"
                            : schedule.getScheduleName()
            );

            rebuildCourseCodeMapFromLoadedSchedule();
            refreshView();

            JOptionPane.showMessageDialog(
                    this,
                    "Loaded schedule: " + timetableTitleLabel.getText(),
                    "Load Complete",
                    JOptionPane.INFORMATION_MESSAGE
            );
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(
                    this,
                    "Please enter a valid numeric schedule ID.",
                    "Invalid Input",
                    JOptionPane.ERROR_MESSAGE
            );
        } catch (RuntimeException e) {
            JOptionPane.showMessageDialog(
                    this,
                    "Failed to load schedule:\n" + e.getMessage(),
                    "Load Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void exportSchedule() {
        JOptionPane.showMessageDialog(this, "Export Schedule clicked.");
    }

    private void rebuildCourseCodeMapFromLoadedSchedule() {
        courseCodeByUiId.clear();

        for (Section section : schedule.getSections()) {
            if (section.getCourseId() == null) continue;

            Course course = courseDao.findById(section.getCourseId());
            if (course != null && course.getCourseCode() != null) {
                courseCodeByUiId.put(section.getUiId(), course.getCourseCode());
            }
        }
    }

    private void ensureScheduleHasBasicInfo() {
        if (schedule.getScheduleName() == null || schedule.getScheduleName().isBlank()) {
            schedule.setScheduleName("Weekly Schedule");
        }

        if (timetableTitleLabel != null &&
                (timetableTitleLabel.getText() == null || timetableTitleLabel.getText().isBlank())) {
            timetableTitleLabel.setText(schedule.getScheduleName());
        }
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

    public static String formatRange(LocalTime start, LocalTime end) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("h:mm a", Locale.ENGLISH);
        return start.format(formatter) + " - " + end.format(formatter);
    }
}