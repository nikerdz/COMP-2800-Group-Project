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

/**
 * Main UI panel for displaying and managing the weekly timetable.
 * Handles schedule actions such as adding, editing, deleting, saving,
 * loading, and exporting timetable blocks.
 */
public class TimetablePanel extends JPanel {

    // Ordered list of day labels used by the timetable grid and dialogs.
    public static final String[] DAYS = {
            "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"
    };

    // Visible hourly labels shown along the timetable grid.
    public static final String[] TIME_SLOTS = {
            "8:00 AM", "9:00 AM", "10:00 AM", "11:00 AM", "12:00 PM", "1:00 PM",
            "2:00 PM", "3:00 PM", "4:00 PM", "5:00 PM", "6:00 PM"
    };

    // Background color used for the outer panel.
    private static final Color PANEL_BG_COLOR = Theme.BRAND_OFFWHITE;

    // Service responsible for timetable-related persistence and business logic.
    private final TimetableService timetableService = new TimetableService();

    // DAO used to resolve course information for loaded sections.
    private final CourseDao courseDao = new CourseDao();

    // The schedule currently being viewed and edited.
    private Schedule schedule = new Schedule("Weekly Timetable", null);

    // Maps UI section ids to course codes for display purposes.
    private final Map<String, String> courseCodeByUiId = new LinkedHashMap<>();

    // Tracks the currently selected section in the UI.
    private String selectedSectionUiId;

    // Tracks sections currently highlighted as conflicts.
    private final Set<String> conflictSectionUiIds = new HashSet<>();

    // Header label showing the current schedule title.
    private JLabel timetableTitleLabel;

    // Base timetable grid containing days and time slots.
    private TimetableGridPanel gridPanel;

    // Overlay layer that renders timetable blocks.
    private TimetableBlockLayer blockLayer;

    // Side/details panel used to show information about the selected section.
    private SectionDetailsPanel detailsPanel;

    // Layered pane combining grid, blocks, and details panel.
    private TimetableLayeredPane layeredPane;

    /**
     * Creates the timetable panel and builds the main UI regions.
     */
    public TimetablePanel() {
        setLayout(new BorderLayout(0, 12));
        setBackground(PANEL_BG_COLOR);

        add(buildHeaderPanel(), BorderLayout.NORTH);
        add(buildTimetableArea(), BorderLayout.CENTER);
    }

    /**
     * Builds the header area containing the schedule title and action buttons.
     *
     * @return the assembled header panel
     */
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

        // Icon button used to rename the current schedule.
        JButton editTitleButton = createIconButton("/images/edit-btn.png", 25, 25);
        editTitleButton.setToolTipText("Edit schedule name");
        editTitleButton.addActionListener(e -> editTimetableTitle());

        leftPanel.add(timetableTitleLabel);
        leftPanel.add(editTitleButton);

        JPanel rightPanel = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 10, 0));
        rightPanel.setOpaque(false);

        // Icon button used to add a new timetable block.
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

    /**
     * Builds the main timetable display area, including the grid,
     * block layer, and section details panel.
     *
     * @return the assembled timetable area
     */
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

    /**
     * Opens the add-block dialog, validates conflicts, persists the new block,
     * and refreshes the UI.
     */
    private void addBlock() {
        BlockFormData data = BlockDialog.show(
                this,
                "Add Timetable Block",
                BlockFormData.empty(),
                DAYS
        );
        if (data == null) return;

        // Prevent insertion if the proposed block overlaps an existing one.
        if (showConflictFeedbackIfNeeded(data, null)) return;

        ensureScheduleHasBasicInfo();
        Section savedSection = timetableService.addBlockToSchedule(schedule, data);

        clearConflictHighlights();
        courseCodeByUiId.put(savedSection.getUiId(), data.courseCode);
        selectedSectionUiId = savedSection.getUiId();

        refreshView();
    }

    /**
     * Opens the edit dialog for a section, applies changes if confirmed,
     * and refreshes the view.
     *
     * @param sectionUiId the UI id of the section to edit
     */
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

        // Use the first time block as the default time range shown in the form.
        if (!section.getTimeBlocks().isEmpty()) {
            start = section.getTimeBlocks().get(0).getStartTime();
            end = section.getTimeBlocks().get(0).getEndTime();
        }

        // Collect all scheduled meeting days for the section.
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

        // Prevent updates that would create a conflict with other sections.
        if (showConflictFeedbackIfNeeded(updated, sectionUiId)) return;

        ensureScheduleHasBasicInfo();
        timetableService.updateBlock(section, updated, schedule);
        clearConflictHighlights();
        courseCodeByUiId.put(sectionUiId, updated.courseCode);
        refreshView();
    }

    /**
     * Deletes a section after confirmation and refreshes the UI.
     *
     * @param sectionUiId the UI id of the section to delete
     */
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

        // Use the service layer when both schedule and section already exist in the database.
        // Otherwise remove only from the in-memory schedule.
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

    /**
     * Marks a section as selected and refreshes the display.
     *
     * @param sectionUiId the selected section UI id
     */
    private void selectSection(String sectionUiId) {
        selectedSectionUiId = sectionUiId;
        refreshView();
    }

    /**
     * Clears the current section selection and refreshes the display.
     */
    private void clearSelection() {
        selectedSectionUiId = null;
        refreshView();
    }

    /**
     * Checks whether the given section is currently selected.
     *
     * @param sectionUiId the section UI id
     * @return true if selected, otherwise false
     */
    private boolean isSelected(String sectionUiId) {
        return sectionUiId != null && sectionUiId.equals(selectedSectionUiId);
    }

    /**
     * Checks whether a section is currently highlighted as conflicting.
     *
     * @param sectionUiId the section UI id
     * @return true if highlighted as a conflict, otherwise false
     */
    private boolean isConflictHighlighted(String sectionUiId) {
        return sectionUiId != null && conflictSectionUiIds.contains(sectionUiId);
    }

    /**
     * Returns the course code associated with a section UI id.
     *
     * @param sectionUiId the section UI id
     * @return the course code, or an empty string if none is mapped
     */
    private String getCourseCodeForSection(String sectionUiId) {
        return courseCodeByUiId.getOrDefault(sectionUiId, "");
    }

    /**
     * Rebuilds block visuals, updates the details panel, and repaints the UI.
     */
    private void refreshView() {
        blockLayer.rebuildBlocks();
        updateDetailsPanel();
        revalidate();
        repaint();
    }

    /**
     * Builds a temporary candidate section from form data and checks whether it
     * conflicts with existing scheduled sections.
     *
     * @param data the proposed block form data
     * @param sectionUiIdToIgnore the existing section UI id to ignore during edit mode
     * @return true if a conflict was found and feedback was shown, otherwise false
     */
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

        // Build time blocks for the candidate section using each selected day.
        for (String day : data.days) {
            candidate.addTimeBlock(new TimeBlock(day, data.start, data.end));
        }

        List<Section> conflicts = schedule.findConflicts(candidate);

        // Ignore the section being edited so it does not conflict with itself.
        if (sectionUiIdToIgnore != null && !sectionUiIdToIgnore.isBlank()) {
            conflicts.removeIf(s -> sectionUiIdToIgnore.equals(s.getUiId()));
        }

        if (conflicts.isEmpty()) {
            clearConflictHighlights();
            return false;
        }

        // Highlight all conflicting sections and select the first one for visibility.
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

    /**
     * Clears all conflict highlights.
     */
    private void clearConflictHighlights() {
        if (conflictSectionUiIds.isEmpty()) return;
        conflictSectionUiIds.clear();
    }

    /**
     * Updates the section details panel based on the current selection.
     * Hides the panel if nothing valid is selected.
     */
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

    /**
     * Prompts the user to rename the current schedule title.
     */
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

    /**
     * Prompts for a schedule name and saves the current schedule details.
     */
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

    /**
     * Loads a previously saved schedule selected by the user.
     */
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

        // Build readable labels for the schedule chooser dialog.
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

        // Extract the schedule id from the display string.
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

    /**
     * Exports the current timetable view as a PNG image.
     */
    private void exportSchedule() {

        // Hide the details panel temporarily so only the timetable itself is exported.
        boolean wasVisible = detailsPanel.isVisible();
        detailsPanel.setVisible(false);
        layeredPane.revalidate();
        layeredPane.repaint();

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Export Schedule as PNG");
        fileChooser.setFileFilter(new FileNameExtensionFilter("PNG Image (*.png)", "png"));

        // Generate a file-safe default name from the schedule title.
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

        // Paint the layered pane into an off-screen image buffer.
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
            // Restore the details panel visibility after export finishes or fails.
            detailsPanel.setVisible(wasVisible);
        }
    }

    /**
     * Rebuilds the course-code lookup map after a schedule is loaded from storage.
     */
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

    /**
     * Ensures the current schedule has a usable name before save or block operations.
     */
    private void ensureScheduleHasBasicInfo() {
        if (schedule.getScheduleName() == null || schedule.getScheduleName().isBlank()) {
            schedule.setScheduleName("Weekly Schedule");
        }

        if (timetableTitleLabel != null &&
                (timetableTitleLabel.getText() == null || timetableTitleLabel.getText().isBlank())) {
            timetableTitleLabel.setText(schedule.getScheduleName());
        }
    }

    /**
     * Applies consistent visual styling to header buttons.
     *
     * @param btn the button to style
     */
    private void styleHeaderButton(JButton btn) {
        btn.setForeground(Theme.BRAND_OFFWHITE);
        btn.setFont(Theme.FONT_BODY.deriveFont(20f));
    }

    /**
     * Creates a transparent icon-only button using an image resource.
     *
     * @param imagePath the icon resource path
     * @param width the desired icon width
     * @param height the desired icon height
     * @return the configured button
     */
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

    /**
     * Formats a time range for display using a 12-hour clock.
     *
     * @param start the start time
     * @param end the end time
     * @return the formatted time range string
     */
    public static String formatRange(LocalTime start, LocalTime end) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("h:mm a", Locale.ENGLISH);
        return start.format(formatter) + " - " + end.format(formatter);
    }
}