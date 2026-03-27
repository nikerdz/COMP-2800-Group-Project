package com.coursely.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;

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

    private final Map<String, String> courseCodeByUiId = new LinkedHashMap<>();

    private String selectedSectionUiId;
    private final Set<String> conflictSectionUiIds = new HashSet<>();

    private JLabel timetableTitleLabel;
    private TimetableGridPanel gridPanel;
    private TimetableBlockLayer blockLayer;
    private SectionDetailsPanel detailsPanel;
    private TimetableLayeredPane layeredPane;

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

        // Small icon button for editing the title — 25x25 is fine here
        JButton editTitleButton = createIconButton("/images/edit-btn.png", 25, 25);
        editTitleButton.setToolTipText("Edit schedule name");
        editTitleButton.addActionListener(e -> editTimetableTitle());

        leftPanel.add(timetableTitleLabel);
        leftPanel.add(editTitleButton);

        JPanel rightPanel = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 10, 0));
        rightPanel.setOpaque(false);

        // Add block button — match the height of the other header buttons (36px min)
        JButton addBlockButton = createIconButton("/images/add-btn.png", 50, 50);
        addBlockButton.setToolTipText("Add Block");
        addBlockButton.addActionListener(e -> addBlock());

        JButton saveScheduleButton = new RoundedButton("Save Schedule", 30);
        styleHeaderButton(saveScheduleButton);
        saveScheduleButton.addActionListener(e -> saveSchedule());

        JButton loadScheduleButton = new RoundedButton("Load Schedule", 30);
        styleHeaderButton(loadScheduleButton);
        loadScheduleButton.addActionListener(e -> loadSchedule());

        JButton exportScheduleButton = new RoundedButton("Export Schedule", 30);
        styleHeaderButton(exportScheduleButton);
        exportScheduleButton.addActionListener(e -> exportSchedule());

        rightPanel.add(addBlockButton);
        rightPanel.add(saveScheduleButton);
        rightPanel.add(loadScheduleButton);
        rightPanel.add(exportScheduleButton);

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

        layeredPane = new TimetableLayeredPane(gridPanel, blockLayer, detailsPanel);
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
        if (showConflictFeedbackIfNeeded(data, null)) return;

        ensureScheduleHasBasicInfo();
        Section savedSection = timetableService.addBlockToSchedule(schedule, data);

        clearConflictHighlights();
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
        if (showConflictFeedbackIfNeeded(updated, sectionUiId)) return;

        ensureScheduleHasBasicInfo();
        timetableService.updateBlock(section, updated, schedule);
        clearConflictHighlights();
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
        clearConflictHighlights();

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

    private boolean isConflictHighlighted(String sectionUiId) {
        return sectionUiId != null && conflictSectionUiIds.contains(sectionUiId);
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

    private boolean showConflictFeedbackIfNeeded(BlockFormData data, String sectionUiIdToIgnore) {
        Section candidate = new Section(
                null,
                null,
                data.sectionCode == null || data.sectionCode.isBlank() ? data.courseCode : data.sectionCode,
                BlockDialog.parseSectionTypeOrDefault(data.sectionType),
                data.instructor,
                data.location,
                data.color
        );
        for (String day : data.days) {
            candidate.addTimeBlock(new TimeBlock(day, data.start, data.end));
        }

        List<Section> conflicts = schedule.findConflicts(candidate);
        if (sectionUiIdToIgnore != null && !sectionUiIdToIgnore.isBlank()) {
            conflicts.removeIf(s -> sectionUiIdToIgnore.equals(s.getUiId()));
        }

        if (conflicts.isEmpty()) {
            clearConflictHighlights();
            return false;
        }

        conflictSectionUiIds.clear();
        for (Section conflict : conflicts) {
            conflictSectionUiIds.add(conflict.getUiId());
        }
        selectedSectionUiId = conflicts.get(0).getUiId();
        refreshView();

        JOptionPane.showMessageDialog(
                this,
                "Time conflict detected. This block overlaps an existing block.\n"
                        + "Please adjust the day/time and try again.",
                "Scheduling Conflict",
                JOptionPane.WARNING_MESSAGE
        );
        return true;
    }

    private void clearConflictHighlights() {
        if (conflictSectionUiIds.isEmpty()) return;
        conflictSectionUiIds.clear();
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
        String currentName = schedule.getScheduleName() == null || schedule.getScheduleName().isBlank()
                ? "Weekly Schedule"
                : schedule.getScheduleName();

        String name = (String) JOptionPane.showInputDialog(
                this,
                "Enter a name for this schedule:",
                "Save Schedule",
                JOptionPane.PLAIN_MESSAGE,
                null,
                null,
                currentName
        );

        if (name == null || name.trim().isBlank()) return;

        schedule.setScheduleName(name.trim());
        timetableTitleLabel.setText(name.trim());

        try {
            timetableService.saveScheduleDetails(schedule);
            JOptionPane.showMessageDialog(
                    this,
                    "Schedule \"" + schedule.getScheduleName() + "\" saved.\nID: " + schedule.getScheduleId(),
                    "Saved",
                    JOptionPane.INFORMATION_MESSAGE
            );
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
        List<Schedule> all;
        try {
            all = timetableService.getAllSchedules();
        } catch (RuntimeException e) {
            JOptionPane.showMessageDialog(
                    this,
                    "Failed to load schedules:\n" + e.getMessage(),
                    "Load Error",
                    JOptionPane.ERROR_MESSAGE
            );
            return;
        }

        if (all.isEmpty()) {
            JOptionPane.showMessageDialog(
                    this,
                    "No saved schedules found.",
                    "Load Schedule",
                    JOptionPane.INFORMATION_MESSAGE
            );
            return;
        }

        String[] options = all.stream()
                .map(s -> s.getScheduleId() + "  —  " + s.getScheduleName()
                        + (s.getCreatedAt() != null ? "  (" + s.getCreatedAt() + ")" : ""))
                .toArray(String[]::new);

        String choice = (String) JOptionPane.showInputDialog(
                this,
                "Select a schedule to load:",
                "Load Schedule",
                JOptionPane.PLAIN_MESSAGE,
                null,
                options,
                options[0]
        );

        if (choice == null) return;

        int scheduleId = Integer.parseInt(choice.split("  —  ")[0].trim());

        try {
            Schedule loaded = timetableService.loadSchedule(scheduleId);
            if (loaded == null) {
                JOptionPane.showMessageDialog(this, "Schedule not found.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            schedule = loaded;
            selectedSectionUiId = null;
            timetableTitleLabel.setText(
                    loaded.getScheduleName() == null || loaded.getScheduleName().isBlank()
                            ? "Weekly Timetable"
                            : loaded.getScheduleName()
            );

            rebuildCourseCodeMapFromLoadedSchedule();
            refreshView();

            JOptionPane.showMessageDialog(
                    this,
                    "Loaded: " + timetableTitleLabel.getText(),
                    "Load Complete",
                    JOptionPane.INFORMATION_MESSAGE
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
        boolean wasVisible = detailsPanel.isVisible();
        detailsPanel.setVisible(false);
        layeredPane.revalidate();
        layeredPane.repaint();

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Export Schedule as PNG");
        fileChooser.setFileFilter(new FileNameExtensionFilter("PNG Image (*.png)", "png"));

        String defaultName = (schedule.getScheduleName() == null || schedule.getScheduleName().isBlank()
                ? "schedule"
                : schedule.getScheduleName().replaceAll("[^a-zA-Z0-9_\\-]", "_"))
                + ".png";
        fileChooser.setSelectedFile(new File(defaultName));

        int result = fileChooser.showSaveDialog(this);

        if (result != JFileChooser.APPROVE_OPTION) {
            detailsPanel.setVisible(wasVisible);
            return;
        }

        File file = fileChooser.getSelectedFile();
        if (!file.getName().toLowerCase(Locale.ENGLISH).endsWith(".png")) {
            file = new File(file.getAbsolutePath() + ".png");
        }

        int w = layeredPane.getWidth();
        int h = layeredPane.getHeight();

        BufferedImage image = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = image.createGraphics();
        layeredPane.paintAll(g2);
        g2.dispose();

        try {
            ImageIO.write(image, "png", file);
            JOptionPane.showMessageDialog(
                    this,
                    "Schedule exported to:\n" + file.getAbsolutePath(),
                    "Export Complete",
                    JOptionPane.INFORMATION_MESSAGE
            );
        } catch (IOException e) {
            JOptionPane.showMessageDialog(
                    this,
                    "Failed to export schedule:\n" + e.getMessage(),
                    "Export Error",
                    JOptionPane.ERROR_MESSAGE
            );
        } finally {
            detailsPanel.setVisible(wasVisible);
        }
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
        btn.setForeground(Theme.BRAND_OFFWHITE);
        btn.setFont(Theme.FONT_BODY.deriveFont(20f));
    }

    private JButton createIconButton(String imagePath, int width, int height) {
        JButton button = new JButton();
        button.setIcon(ResourceUtils.loadIcon(imagePath, width, height));
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