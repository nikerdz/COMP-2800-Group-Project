package com.coursely.ui;

import java.awt.Component;
import java.awt.Dimension;

import javax.swing.JLayeredPane;

public class TimetableLayeredPane extends JLayeredPane {

    private final Component gridPanel;
    private final Component blockLayer;
    private final Component detailsPanel;

    public TimetableLayeredPane(Component gridPanel, Component blockLayer, Component detailsPanel) {
        this.gridPanel = gridPanel;
        this.blockLayer = blockLayer;
        this.detailsPanel = detailsPanel;

        add(gridPanel, Integer.valueOf(0));
        add(blockLayer, Integer.valueOf(1));
        add(detailsPanel, Integer.valueOf(2));
    }

    @Override
    public void doLayout() {
        int width = getWidth();
        int height = getHeight();

        gridPanel.setBounds(0, 0, width, height);
        blockLayer.setBounds(0, 0, width, height);

        int margin = 14;
        Dimension preferred = detailsPanel.getPreferredSize();
        int panelWidth = Math.max(preferred.width, 280);
        int panelHeight = Math.min(preferred.height, height - margin * 2);

        detailsPanel.setBounds(
                width - panelWidth - margin,
                height - panelHeight - margin,
                panelWidth,
                panelHeight
        );
    }

    @Override
    public Dimension getPreferredSize() {
        return gridPanel.getPreferredSize();
    }
}