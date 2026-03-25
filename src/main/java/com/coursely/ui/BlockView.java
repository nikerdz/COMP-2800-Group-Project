package com.coursely.ui;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.function.Consumer;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

import com.coursely.model.Section;
import com.coursely.model.TimeBlock;

public class BlockView extends JPanel {

    private final Section section;
    private final TimeBlock timeBlock;

    public BlockView(
            Section section,
            TimeBlock timeBlock,
            String courseCode,
            boolean selected,
            Consumer<String> onSelectSection,
            Consumer<String> onEditSection,
            Consumer<String> onDeleteSection
    ) {
        this.section = section;
        this.timeBlock = timeBlock;

        setLayout(new GridBagLayout());
        setOpaque(true);
        setBackground(section.getColor() == null ? Theme.BLOCK_BLUE : section.getColor());

        JPanel textPanel = new JPanel(new GridLayout(0, 1, 0, 0));
        textPanel.setOpaque(false);
        textPanel.setBorder(new EmptyBorder(4, 4, 4, 4));

        JLabel courseCodeLabel = new JLabel(
                (courseCode == null || courseCode.isBlank()) ? "Course" : courseCode
        );
        courseCodeLabel.setFont(Theme.FONT_HEADING.deriveFont(16f));
        courseCodeLabel.setForeground(Theme.BRAND_BROWN);
        courseCodeLabel.setHorizontalAlignment(SwingConstants.CENTER);

        String sectionCodeText = safeValue(section.getSectionCode(), "Section");
        JLabel sectionCodeLabel = new JLabel(sectionCodeText);
        sectionCodeLabel.setFont(Theme.FONT_BODY.deriveFont(14f));
        sectionCodeLabel.setForeground(Theme.BRAND_BROWN);
        sectionCodeLabel.setHorizontalAlignment(SwingConstants.CENTER);

        String instructorLocationText = buildInstructorLocationText(
                section.getInstructor(),
                section.getLocation()
        );
        JLabel instructorLocationLabel = new JLabel(instructorLocationText);
        instructorLocationLabel.setFont(Theme.FONT_BODY.deriveFont(13f));
        instructorLocationLabel.setForeground(Theme.BRAND_BROWN);
        instructorLocationLabel.setHorizontalAlignment(SwingConstants.CENTER);

        textPanel.add(courseCodeLabel);
        textPanel.add(sectionCodeLabel);
        textPanel.add(instructorLocationLabel);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        add(textPanel, gbc);
        setSelected(selected);

        MouseAdapter clickHandler = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                onSelectSection.accept(section.getUiId());

                if (isRightClick(e)) {
                    showPopup(e, onEditSection, onDeleteSection);
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    onSelectSection.accept(section.getUiId());
                    showPopup(e, onEditSection, onDeleteSection);
                }
            }
        };

        addMouseListener(clickHandler);
        textPanel.addMouseListener(clickHandler);
        courseCodeLabel.addMouseListener(clickHandler);
        sectionCodeLabel.addMouseListener(clickHandler);
        instructorLocationLabel.addMouseListener(clickHandler);
    }

    public TimeBlock getTimeBlock() {
        return timeBlock;
    }

    public void setSelected(boolean selected) {
        if (selected) {
            setBorder(BorderFactory.createLineBorder(Theme.BRAND_BLUE, 2, true));
        } else {
            setBorder(BorderFactory.createLineBorder(new Color(160, 180, 205), 1, true));
        }
        repaint();
    }

    private void showPopup(MouseEvent e, Consumer<String> onEditSection, Consumer<String> onDeleteSection) {
        JPopupMenu menu = new JPopupMenu();

        JMenuItem edit = new JMenuItem("Edit");
        edit.addActionListener(ev -> onEditSection.accept(section.getUiId()));

        JMenuItem delete = new JMenuItem("Delete");
        delete.addActionListener(ev -> onDeleteSection.accept(section.getUiId()));

        menu.add(edit);
        menu.add(delete);
        menu.show(this, e.getX(), e.getY());
    }

    private boolean isRightClick(MouseEvent e) {
        return e.getButton() == MouseEvent.BUTTON3 || e.isPopupTrigger();
    }

    private String buildInstructorLocationText(String instructor, String location) {
        boolean hasInstructor = instructor != null && !instructor.isBlank();
        boolean hasLocation = location != null && !location.isBlank();

        if (hasInstructor && hasLocation) {
            return instructor + " • " + location;
        }
        if (hasInstructor) {
            return instructor;
        }
        if (hasLocation) {
            return location;
        }
        return " ";
    }

    private String safeValue(String value, String fallback) {
        return (value == null || value.isBlank()) ? fallback : value;
    }
}