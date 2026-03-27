package com.coursely.ui;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.Timer;

public class RoundedButton extends JButton {

    private final int arc;

    private static final Color NORMAL_COLOR = Theme.BRAND_BLUE;
    private static final Color HOVER_COLOR  = lighten(Theme.BRAND_BLUE, 0.25f);
    private static final int TRANSITION_MS  = 150; // total fade duration
    private static final int TICK_MS        = 10;  // repaint interval

    private float progress = 0f; // 0 = normal, 1 = hovered
    private Timer timer;

    public RoundedButton(String text, int arc) {
        super(text);
        this.arc = arc;
        setOpaque(false);
        setContentAreaFilled(false);
        setBorderPainted(false);
        setFocusPainted(false);
        setCursor(new Cursor(Cursor.HAND_CURSOR));
        setBorder(BorderFactory.createEmptyBorder(8, 18, 8, 18));

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                startFade(true);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                startFade(false);
            }
        });
    }

    private void startFade(boolean fadeIn) {
        if (timer != null && timer.isRunning()) {
            timer.stop();
        }

        float step = (float) TICK_MS / TRANSITION_MS;

        timer = new Timer(TICK_MS, e -> {
            progress = fadeIn
                    ? Math.min(1f, progress + step)
                    : Math.max(0f, progress - step);

            repaint();

            if ((fadeIn && progress >= 1f) || (!fadeIn && progress <= 0f)) {
                timer.stop();
            }
        });

        timer.start();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(interpolate(NORMAL_COLOR, HOVER_COLOR, progress));
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), arc, arc);
        g2.dispose();
        super.paintComponent(g);
    }

    @Override
    public Dimension getPreferredSize() {
        Dimension d = super.getPreferredSize();
        return new Dimension(d.width, Math.max(d.height, 36));
    }

    private static Color interpolate(Color a, Color b, float t) {
        int r = (int)(a.getRed()   + (b.getRed()   - a.getRed())   * t);
        int g = (int)(a.getGreen() + (b.getGreen() - a.getGreen()) * t);
        int bv = (int)(a.getBlue() + (b.getBlue()  - a.getBlue())  * t);
        return new Color(r, g, bv);
    }

    private static Color lighten(Color c, float factor) {
        int r  = Math.min(255, (int)(c.getRed()   + (255 - c.getRed())   * factor));
        int g  = Math.min(255, (int)(c.getGreen() + (255 - c.getGreen()) * factor));
        int b  = Math.min(255, (int)(c.getBlue()  + (255 - c.getBlue())  * factor));
        return new Color(r, g, b);
    }
}