package com.coursely.ui;

import java.awt.Component;
import java.awt.Dimension;

import javax.swing.JLayeredPane;

/**
 * Layered pane used to stack the timetable grid, block layer,
 * and details panel in a single composite view.
 */
public class TimetableLayeredPane extends JLayeredPane {

    // Base grid showing the timetable structure.
    private final Component gridPanel;

    // Transparent overlay containing timetable block components.
    private final Component blockLayer;

    // Floating panel showing details for the selected section.
    private final Component detailsPanel;

    /**
     * Creates a layered pane with the grid at the back,
     * blocks in the middle, and details on top.
     *
     * @param gridPanel the base timetable grid
     * @param blockLayer the overlay containing timetable blocks
     * @param detailsPanel the floating details panel
     */
    public TimetableLayeredPane(Component gridPanel, Component blockLayer, Component detailsPanel) {
        this.gridPanel = gridPanel;
        this.blockLayer = blockLayer;
        this.detailsPanel = detailsPanel;

        // Lower layer values are painted first.
        add(gridPanel, Integer.valueOf(0));
        add(blockLayer, Integer.valueOf(1));
        add(detailsPanel, Integer.valueOf(2));
    }

    /**
     * Positions the grid and block layer to fill the entire pane,
     * then anchors the details panel near the bottom-right corner.
     */
    @Override
    public void doLayout() {
        int width = getWidth();
        int height = getHeight();

        // Grid and block layer always occupy the full available area.
        gridPanel.setBounds(0, 0, width, height);
        blockLayer.setBounds(0, 0, width, height);

        int margin = 14;
        Dimension preferred = detailsPanel.getPreferredSize();

        // Enforce a minimum width while keeping the panel within the pane height.
        int panelWidth = Math.max(preferred.width, 280);
        int panelHeight = Math.min(preferred.height, height - margin * 2);

        detailsPanel.setBounds(
                width - panelWidth - margin,
                height - panelHeight - margin,
                panelWidth,
                panelHeight
        );
    }

    /**
     * Uses the grid panel's preferred size as the preferred size
     * for the entire layered pane.
     *
     * @return the preferred size of the grid panel
     */
    @Override
    public Dimension getPreferredSize() {
        return gridPanel.getPreferredSize();
    }
}