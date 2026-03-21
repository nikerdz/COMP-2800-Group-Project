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

    private final JLabel titleLabel = new JLabel();
    private final JLabel typeLabel = new JLabel();
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

        titleLabel.setFont(Theme.FONT_HEADING.deriveFont(18f));
        titleLabel.setForeground(Theme.BRAND_BROWN);

        typeLabel.setFont(Theme.FONT_BODY.deriveFont(25f));
        typeLabel.setForeground(Theme.BRAND_BROWN);

        daysLabel.setFont(Theme.FONT_BODY.deriveFont(25f));
        daysLabel.setForeground(Theme.BRAND_BROWN);

        timesLabel.setFont(Theme.FONT_BODY.deriveFont(25f));
        timesLabel.setForeground(Theme.BRAND_BROWN);

        infoPanel.add(titleLabel);
        infoPanel.add(typeLabel);
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

    public void bind(Section section, String displayType) {
        currentUiId = section.getUiId();

        titleLabel.setText(section.getSectionCode());

        String resolvedType = (displayType == null || displayType.isBlank())
                ? section.getSectionType().name()
                : displayType;
        typeLabel.setText("Type: " + resolvedType);

        List<String> days = new ArrayList<>();
        List<String> times = new ArrayList<>();

        for (TimeBlock tb : section.getTimeBlocks()) {
            days.add(tb.getDayOfWeek());
            times.add(tb.getDayOfWeek() + "  " +
                    TimetablePanel.formatRange(tb.getStartTime(), tb.getEndTime()));
        }

        daysLabel.setText("Days: " + String.join(", ", days));
        timesLabel.setText("<html>Times:<br/>" + String.join("<br/>", times) + "</html>");
    }

    private void styleActionButton(JButton button) {
        button.setBackground(Theme.BRAND_OFFWHITE);
        button.setForeground(Theme.BRAND_BROWN);
        button.setFocusPainted(false);
        button.setOpaque(true);
        button.setContentAreaFilled(true);
        button.setFont(Theme.FONT_BODY.deriveFont(25f));
        button.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(Theme.BRAND_BLUE, 2, true),
                BorderFactory.createEmptyBorder(6, 10, 6, 10)
        ));
    }
}