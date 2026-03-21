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

    public BlockView(
            Section section,
            TimeBlock timeBlock,
            String displayType,
            boolean selected,
            Consumer<String> onSelectSection,
            Consumer<String> onEditSection,
            Consumer<String> onDeleteSection
    ) {
        this.section = section;
        this.timeBlock = timeBlock;

        setLayout(new BorderLayout());
        setOpaque(true);
        setBackground(section.getColor() == null ? Theme.BLOCK_BLUE : section.getColor());

        JPanel textPanel = new JPanel(new GridLayout(0, 1, 0, 2));
        textPanel.setOpaque(false);
        textPanel.setBorder(new EmptyBorder(4, 6, 4, 6));

        JLabel titleLabel = new JLabel(section.getSectionCode());
        titleLabel.setFont(Theme.FONT_BODY.deriveFont(20f));
        titleLabel.setForeground(Color.BLACK);

        JLabel timeLabel = new JLabel(TimetablePanel.formatRange(
                timeBlock.getStartTime(),
                timeBlock.getEndTime()
        ));
        timeLabel.setFont(Theme.FONT_BODY.deriveFont(15f));
        timeLabel.setForeground(Color.BLACK);

        textPanel.add(titleLabel);
        textPanel.add(timeLabel);

        if (displayType != null && !displayType.isBlank()) {
            JLabel typeLabel = new JLabel(displayType);
            typeLabel.setFont(Theme.FONT_BODY.deriveFont(10f));
            typeLabel.setForeground(Color.BLACK);
            textPanel.add(typeLabel);
        }

        add(textPanel, BorderLayout.NORTH);
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
        titleLabel.addMouseListener(clickHandler);
        timeLabel.addMouseListener(clickHandler);
    }

    public TimeBlock getTimeBlock() {
        return timeBlock;
    }

    public void setSelected(boolean selected) {
        if (selected) {
            setBorder(BorderFactory.createLineBorder(Theme.BRAND_BLUE, 1, true));
        } else {
            setBorder(BorderFactory.createLineBorder(new Color(80, 110, 150), 1, true));
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
}