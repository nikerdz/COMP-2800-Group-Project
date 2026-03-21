package com.coursely.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.function.Consumer;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.border.EmptyBorder;

import com.coursely.model.Section;
import com.coursely.model.TimeBlock;

public class BlockView extends JPanel {

    private final Section section;
    private final TimeBlock timeBlock;
    private final Color baseColor;

    public BlockView(
            Section section,
            TimeBlock timeBlock,
            String courseCode,
            boolean selected,
            boolean conflicted,
            Consumer<String> onSelectSection,
            Consumer<String> onEditSection,
            Consumer<String> onDeleteSection
    ) {
        this.section = section;
        this.timeBlock = timeBlock;
        this.baseColor = section.getColor() == null ? Theme.BLOCK_BLUE : section.getColor();

        setLayout(new BorderLayout());
        setOpaque(true);
        setBackground(baseColor);

        JPanel textPanel = new JPanel(new GridLayout(0, 1, 0, 2));
        textPanel.setOpaque(false);
        textPanel.setBorder(new EmptyBorder(4, 6, 4, 6));

        JLabel courseCodeLabel = new JLabel(
                (courseCode == null || courseCode.isBlank()) ? "Course" : courseCode
        );
        courseCodeLabel.setFont(Theme.FONT_BODY.deriveFont(13f));
        courseCodeLabel.setForeground(Color.BLACK);

        String sectionCodeText = safeValue(section.getSectionCode(), "Section");
        JLabel sectionCodeLabel = new JLabel(sectionCodeText);
        sectionCodeLabel.setFont(Theme.FONT_BODY.deriveFont(11f));
        sectionCodeLabel.setForeground(Color.BLACK);

        String instructorLocationText = buildInstructorLocationText(
                section.getInstructor(),
                section.getLocation()
        );
        JLabel instructorLocationLabel = new JLabel(instructorLocationText);
        instructorLocationLabel.setFont(Theme.FONT_BODY.deriveFont(10f));
        instructorLocationLabel.setForeground(Color.BLACK);

        textPanel.add(courseCodeLabel);
        textPanel.add(sectionCodeLabel);
        textPanel.add(instructorLocationLabel);

        add(textPanel, BorderLayout.NORTH);
        applyVisualState(selected, conflicted);

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
        applyVisualState(selected, false);
    }

    private void applyVisualState(boolean selected, boolean conflicted) {
        if (conflicted) {
            setBorder(BorderFactory.createLineBorder(new Color(214, 76, 76), 2, true));
            setBackground(mix(baseColor, new Color(255, 225, 225), 0.55));
        } else if (selected) {
            setBorder(BorderFactory.createLineBorder(Theme.BRAND_BLUE, 1, true));
            setBackground(baseColor);
        } else {
            setBorder(BorderFactory.createLineBorder(new Color(160, 180, 205), 1, true));
            setBackground(baseColor);
        }
        repaint();
    }

    private Color mix(Color a, Color b, double ratio) {
        double r = Math.max(0, Math.min(1, ratio));
        int red = (int) Math.round(a.getRed() * (1 - r) + b.getRed() * r);
        int green = (int) Math.round(a.getGreen() * (1 - r) + b.getGreen() * r);
        int blue = (int) Math.round(a.getBlue() * (1 - r) + b.getBlue() * r);
        return new Color(red, green, blue);
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
