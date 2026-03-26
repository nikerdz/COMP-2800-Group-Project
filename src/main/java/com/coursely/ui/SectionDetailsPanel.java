package com.coursely.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import javax.swing.BorderFactory;
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
        setLayout(new BorderLayout(0, 12));
        setOpaque(true);
        setBackground(new Color(255, 255, 255, 245));
        setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(Theme.BRAND_BLUE, 1, true),
                BorderFactory.createEmptyBorder(14, 14, 14, 14)
        ));

        JPanel infoPanel = new JPanel(new GridLayout(0, 1, 0, 8));
        infoPanel.setOpaque(false);

        courseCodeLabel.setFont(Theme.FONT_BODY.deriveFont(18f));
        courseCodeLabel.setForeground(Theme.BRAND_BROWN);

        sectionCodeLabel.setFont(Theme.FONT_BODY.deriveFont(15f));
        sectionCodeLabel.setForeground(Theme.BRAND_BROWN);

        typeLabel.setFont(Theme.FONT_BODY.deriveFont(14f));
        typeLabel.setForeground(Theme.BRAND_BROWN);

        instructorLabel.setFont(Theme.FONT_BODY.deriveFont(14f));
        instructorLabel.setForeground(Theme.BRAND_BROWN);

        locationLabel.setFont(Theme.FONT_BODY.deriveFont(14f));
        locationLabel.setForeground(Theme.BRAND_BROWN);

        daysLabel.setFont(Theme.FONT_BODY.deriveFont(14f));
        daysLabel.setForeground(Theme.BRAND_BROWN);

        timesLabel.setFont(Theme.FONT_BODY.deriveFont(14f));
        timesLabel.setForeground(Theme.BRAND_BROWN);

        infoPanel.add(courseCodeLabel);
        infoPanel.add(sectionCodeLabel);
        infoPanel.add(typeLabel);
        infoPanel.add(instructorLabel);
        infoPanel.add(locationLabel);
        infoPanel.add(daysLabel);
        infoPanel.add(timesLabel);

        JButton editButton = new JButton("Edit");
        styleActionButton(editButton);
        editButton.addActionListener(e -> {
            if (currentUiId != null) {
                onEditSection.accept(currentUiId);
            }
        });

        JButton deleteButton = new JButton("Delete");
        styleActionButton(deleteButton);
        deleteButton.addActionListener(e -> {
            if (currentUiId != null) {
                onDeleteSection.accept(currentUiId);
            }
        });

        JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 8, 0));
        buttonPanel.setOpaque(false);
        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);

        add(infoPanel, BorderLayout.CENTER);
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
        button.setBackground(Theme.BRAND_OFFWHITE);
        button.setForeground(Theme.BRAND_BROWN);
        button.setFocusPainted(false);
        button.setOpaque(true);
        button.setContentAreaFilled(true);
        button.setFont(Theme.FONT_BODY.deriveFont(16f));
        button.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(Theme.BRAND_BLUE, 2, true),
                BorderFactory.createEmptyBorder(6, 10, 6, 10)
        ));
    }

    private String safeValue(String value, String fallback) {
        return (value == null || value.isBlank()) ? fallback : value;
    }

    private String enumName(Section section) {
        return section.getSectionType() == null ? null : section.getSectionType().name();
    }
}