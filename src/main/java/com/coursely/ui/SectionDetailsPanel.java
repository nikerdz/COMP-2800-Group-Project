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

public class SectionDetailsPanel extends JPanel {

    private final JLabel courseCodeLabel = new JLabel();
    private final JLabel sectionCodeLabel = new JLabel();
    private final JLabel typeLabel = new JLabel();
    private final JLabel instructorLabel = new JLabel();
    private final JLabel locationLabel = new JLabel();
    private final JLabel daysLabel = new JLabel();
    private final JLabel timesLabel = new JLabel();

    private String currentUiId;

    public SectionDetailsPanel(Consumer<String> onEditSection, Consumer<String> onDeleteSection) {
        setLayout(new BorderLayout(0, 14));
        setOpaque(true);
        setBackground(Theme.BRAND_OFFWHITE);
        setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(Theme.BRAND_BLUE, 2, true),
                BorderFactory.createEmptyBorder(16, 16, 16, 16)
        ));

        courseCodeLabel.setFont(Theme.FONT_BODY.deriveFont(28f));
        courseCodeLabel.setForeground(Theme.BRAND_BROWN);

        sectionCodeLabel.setFont(Theme.FONT_BODY.deriveFont(22f));
        sectionCodeLabel.setForeground(Theme.BRAND_BROWN);

        for (JLabel label : new JLabel[]{ typeLabel, instructorLabel, locationLabel, daysLabel }) {
            label.setFont(Theme.FONT_BODY.deriveFont(20f));
            label.setForeground(Theme.BRAND_BROWN);
        }
        timesLabel.setFont(Theme.FONT_BODY.deriveFont(20f));
        timesLabel.setForeground(Theme.BRAND_BROWN);

        // Thin divider under course code
        JPanel divider = new JPanel();
        divider.setBackground(new Color(
                Theme.BRAND_BLUE.getRed(),
                Theme.BRAND_BLUE.getGreen(),
                Theme.BRAND_BLUE.getBlue(), 80
        ));
        divider.setPreferredSize(new Dimension(0, 1));
        divider.setOpaque(true);

        JPanel headerPanel = new JPanel(new BorderLayout(0, 8));
        headerPanel.setOpaque(false);
        headerPanel.add(courseCodeLabel, BorderLayout.NORTH);
        headerPanel.add(divider, BorderLayout.SOUTH);

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

        JPanel centrePanel = new JPanel(new BorderLayout(0, 10));
        centrePanel.setOpaque(false);
        centrePanel.add(headerPanel, BorderLayout.NORTH);
        centrePanel.add(infoPanel, BorderLayout.CENTER);

        JButton editButton = new RoundedButton("Edit", 30);
        JButton deleteButton = new RoundedButton("Delete", 30);
        styleActionButton(editButton);
        styleActionButton(deleteButton);

        editButton.addActionListener(e -> {
            if (currentUiId != null) onEditSection.accept(currentUiId);
        });
        deleteButton.addActionListener(e -> {
            if (currentUiId != null) onDeleteSection.accept(currentUiId);
        });

        JPanel buttonPanel = new JPanel(new java.awt.GridLayout(1, 2, 8, 0));
        buttonPanel.setOpaque(false);
        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);

        add(centrePanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

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

    private void styleActionButton(JButton button) {
        button.setForeground(Theme.BRAND_OFFWHITE);
        button.setFont(Theme.FONT_BODY.deriveFont(18f));
    }

    private String safeValue(String value, String fallback) {
        return (value == null || value.isBlank()) ? fallback : value;
    }

    private String enumName(Section section) {
        return section.getSectionType() == null ? null : section.getSectionType().name();
    }
}