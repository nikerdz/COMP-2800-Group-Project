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

/**
 * Visual component representing a single timetable block.
 * Displays course and section information and supports selection,
 * editing, and deletion through mouse interaction.
 */
public class BlockView extends JPanel {

    // Section associated with this block.
    private final Section section;

    // Specific time block represented by this visual component.
    private final TimeBlock timeBlock;

    /**
     * Creates a timetable block view for a specific section and time block.
     *
     * @param section the section associated with the block
     * @param timeBlock the specific time block represented visually
     * @param courseCode the course code to display
     * @param selected whether the block should initially appear selected
     * @param onSelectSection callback triggered when the block is selected
     * @param onEditSection callback triggered when the block should be edited
     * @param onDeleteSection callback triggered when the block should be deleted
     */
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

        // Inner panel used to stack text labels vertically inside the block.
        JPanel textPanel = new JPanel(new GridLayout(0, 1, 0, 0));
        textPanel.setOpaque(false);
        textPanel.setBorder(new EmptyBorder(4, 4, 4, 4));

        // Main course code label.
        JLabel courseCodeLabel = new JLabel(
                (courseCode == null || courseCode.isBlank()) ? "Course" : courseCode
        );
        courseCodeLabel.setFont(Theme.FONT_BODY.deriveFont(16f));
        courseCodeLabel.setForeground(Theme.BRAND_BROWN);
        courseCodeLabel.setHorizontalAlignment(SwingConstants.CENTER);

        // Section code or fallback label.
        String sectionCodeText = safeValue(section.getSectionCode(), "Section");
        JLabel sectionCodeLabel = new JLabel(sectionCodeText);
        sectionCodeLabel.setFont(Theme.FONT_BODY.deriveFont(14f));
        sectionCodeLabel.setForeground(Theme.BRAND_BROWN);
        sectionCodeLabel.setHorizontalAlignment(SwingConstants.CENTER);

        // Combined instructor/location label for compact display.
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

        // Center the text panel within the block view.
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        add(textPanel, gbc);

        setSelected(selected);

        // Shared mouse handler for selection and context-menu behavior.
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

        // Attach the same handler to the block and its child components
        // so clicks anywhere on the block behave consistently.
        addMouseListener(clickHandler);
        textPanel.addMouseListener(clickHandler);
        courseCodeLabel.addMouseListener(clickHandler);
        sectionCodeLabel.addMouseListener(clickHandler);
        instructorLocationLabel.addMouseListener(clickHandler);
    }

    /**
     * Returns the time block represented by this view.
     *
     * @return the time block
     */
    public TimeBlock getTimeBlock() {
        return timeBlock;
    }

    /**
     * Updates the visual selection state of the block.
     *
     * @param selected true to show the selected border, otherwise false
     */
    public void setSelected(boolean selected) {
        if (selected) {
            setBorder(BorderFactory.createLineBorder(Theme.BRAND_BLUE, 2, true));
        } else {
            setBorder(BorderFactory.createLineBorder(new Color(160, 180, 205), 1, true));
        }
        repaint();
    }

    /**
     * Shows the context menu for editing or deleting the section.
     *
     * @param e the mouse event that triggered the popup
     * @param onEditSection callback for editing the section
     * @param onDeleteSection callback for deleting the section
     */
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

    /**
     * Checks whether a mouse event should be treated as a right-click
     * or popup-trigger action.
     *
     * @param e the mouse event
     * @return true if the event is a right-click or popup trigger
     */
    private boolean isRightClick(MouseEvent e) {
        return e.getButton() == MouseEvent.BUTTON3 || e.isPopupTrigger();
    }

    /**
     * Builds a compact display string for instructor and location.
     *
     * @param instructor the instructor name
     * @param location the location name
     * @return a combined display string, or a blank placeholder if neither exists
     */
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
}