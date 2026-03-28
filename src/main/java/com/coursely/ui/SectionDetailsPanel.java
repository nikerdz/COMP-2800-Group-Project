package com.coursely.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.LineBorder;

import com.coursely.model.Section;
import com.coursely.model.TimeBlock;

/**
 * Panel that displays details for the currently selected timetable section.
 * Also provides edit and delete actions for that section.
 */
public class SectionDetailsPanel extends JPanel {

    // Labels used to display section information.
    private final JLabel courseCodeLabel = new JLabel();
    private final JLabel sectionCodeLabel = new JLabel();
    private final JLabel typeLabel = new JLabel();
    private final JLabel instructorLabel = new JLabel();
    private final JLabel locationLabel = new JLabel();
    private final JLabel daysLabel = new JLabel();
    private final JLabel timesLabel = new JLabel();

    // Stores the UI id of the section currently bound to this panel.
    private String currentUiId;

    /**
     * Creates the details panel and wires its action buttons.
     *
     * @param onEditSection callback used when the edit button is pressed
     * @param onDeleteSection callback used when the delete button is pressed
     */
    public SectionDetailsPanel(Consumer<String> onEditSection, Consumer<String> onDeleteSection) {
        setLayout(new BorderLayout(0, 14));
        setOpaque(true);
        setBackground(Theme.BRAND_OFFWHITE);
        setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(Theme.BRAND_BLUE, 2, true),
                BorderFactory.createEmptyBorder(16, 16, 16, 16)
        ));

        // Style the main course code label.
        courseCodeLabel.setFont(Theme.FONT_BODY.deriveFont(28f));
        courseCodeLabel.setForeground(Theme.BRAND_BROWN);

        // Style the section code label.
        sectionCodeLabel.setFont(Theme.FONT_BODY.deriveFont(22f));
        sectionCodeLabel.setForeground(Theme.BRAND_BROWN);

        // Apply shared styling to the remaining detail labels.
        for (JLabel label : new JLabel[]{ typeLabel, instructorLabel, locationLabel, daysLabel }) {
            label.setFont(Theme.FONT_BODY.deriveFont(20f));
            label.setForeground(Theme.BRAND_BROWN);
        }

        timesLabel.setFont(Theme.FONT_BODY.deriveFont(20f));
        timesLabel.setForeground(Theme.BRAND_BROWN);

        // Thin divider shown under the course code header.
        JPanel divider = new JPanel();
        divider.setBackground(new Color(
                Theme.BRAND_BLUE.getRed(),
                Theme.BRAND_BLUE.getGreen(),
                Theme.BRAND_BLUE.getBlue(), 80
        ));
        divider.setPreferredSize(new Dimension(0, 1));
        divider.setOpaque(true);

        // Header area containing the course code and divider.
        JPanel headerPanel = new JPanel(new BorderLayout(0, 8));
        headerPanel.setOpaque(false);
        headerPanel.add(courseCodeLabel, BorderLayout.NORTH);
        headerPanel.add(divider, BorderLayout.SOUTH);

        // Main vertical info area containing section details.
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setOpaque(false);
        infoPanel.add(sectionCodeLabel);
        infoPanel.add(Box.createRigidArea(new Dimension(0, 4)));
        infoPanel.add(typeLabel);
        infoPanel.add(Box.createRigidArea(new Dimension(0, 4)));
        infoPanel.add(instructorLabel);
        infoPanel.add(Box.createRigidArea(new Dimension(0, 4)));
        infoPanel.add(locationLabel);
        infoPanel.add(Box.createRigidArea(new Dimension(0, 4)));
        infoPanel.add(daysLabel);
        infoPanel.add(Box.createRigidArea(new Dimension(0, 4)));
        infoPanel.add(timesLabel);

        // Center panel groups the header and info content together.
        JPanel centrePanel = new JPanel(new BorderLayout(0, 10));
        centrePanel.setOpaque(false);
        centrePanel.add(headerPanel, BorderLayout.NORTH);
        centrePanel.add(infoPanel, BorderLayout.CENTER);

        // Action buttons for editing and deleting the selected section.
        JButton editButton = new RoundedButton("Edit", 30);
        JButton deleteButton = new RoundedButton("Delete", 30);
        styleActionButton(editButton);
        styleActionButton(deleteButton);

        // Only trigger callbacks when a valid section is currently bound.
        editButton.addActionListener(e -> {
            if (currentUiId != null) onEditSection.accept(currentUiId);
        });

        deleteButton.addActionListener(e -> {
            if (currentUiId != null) onDeleteSection.accept(currentUiId);
        });

        // Button row shown at the bottom of the panel.
        JPanel buttonPanel = new JPanel(new java.awt.GridLayout(1, 2, 8, 0));
        buttonPanel.setOpaque(false);
        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);

        add(centrePanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    /**
     * Populates the panel with data from the selected section.
     *
     * @param section the selected section
     * @param courseCode the course code associated with the section
     */
    public void bind(Section section, String courseCode) {
        currentUiId = section.getUiId();

        courseCodeLabel.setText(
                (courseCode == null || courseCode.isBlank()) ? "Course" : courseCode
        );
        sectionCodeLabel.setText("Section: " + safeValue(section.getSectionCode(), "N/A"));
        typeLabel.setText("Type: " + safeValue(enumName(section), "N/A"));
        instructorLabel.setText("Instructor: " + safeValue(section.getInstructor(), "N/A"));
        locationLabel.setText("Location: " + safeValue(section.getLocation(), "N/A"));

        List<String> days = new ArrayList<>();
        List<String> times = new ArrayList<>();

        // Build day and time display strings from each time block.
        for (TimeBlock tb : section.getTimeBlocks()) {
            days.add(tb.getDayOfWeek());
            times.add(tb.getDayOfWeek() + "  " +
                    TimetablePanel.formatRange(tb.getStartTime(), tb.getEndTime()));
        }

        daysLabel.setText("Days: " + (days.isEmpty() ? "N/A" : String.join(", ", days)));
        timesLabel.setText(
                "<html>Times:<br/>" + (times.isEmpty() ? "N/A" : String.join("<br/>", times)) + "</html>"
        );
    }

    /**
     * Applies shared styling to action buttons.
     *
     * @param button the button to style
     */
    private void styleActionButton(JButton button) {
        button.setForeground(Theme.BRAND_OFFWHITE);
        button.setFont(Theme.FONT_BODY.deriveFont(18f));
    }

    /**
     * Returns a fallback string when the provided value is null or blank.
     *
     * @param value the value to check
     * @param fallback the fallback text
     * @return the original value or the fallback
     */
    private String safeValue(String value, String fallback) {
        return (value == null || value.isBlank()) ? fallback : value;
    }

    /**
     * Returns the section type name as text, or null if no type is set.
     *
     * @param section the section to inspect
     * @return the enum name, or null if unavailable
     */
    private String enumName(Section section) {
        return section.getSectionType() == null ? null : section.getSectionType().name();
    }
}