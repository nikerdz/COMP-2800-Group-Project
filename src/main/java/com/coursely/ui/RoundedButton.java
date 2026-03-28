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

/**
 * Custom rounded button with a hover fade animation.
 * The button paints its own rounded background and transitions
 * between normal and hover colors.
 */
public class RoundedButton extends JButton {

    // Corner roundness used when painting the button background.
    private final int arc;

    // Default background color.
    private static final Color NORMAL_COLOR = Theme.BRAND_BLUE;

    // Hover background color derived from the normal color.
    private static final Color HOVER_COLOR  = lighten(Theme.BRAND_BLUE, 0.25f);

    // Total animation duration in milliseconds.
    private static final int TRANSITION_MS  = 150;

    // Timer tick interval for updating the fade animation.
    private static final int TICK_MS        = 10;

    // Animation progress value:
    // 0 = fully normal color, 1 = fully hover color.
    private float progress = 0f;

    // Timer used to animate the hover transition.
    private Timer timer;

    /**
     * Creates a rounded button with the provided text and corner arc radius.
     *
     * @param text the button label
     * @param arc the corner arc size
     */
    public RoundedButton(String text, int arc) {
        super(text);
        this.arc = arc;

        // Disable default button painting so the custom rounded background is visible.
        setOpaque(false);
        setContentAreaFilled(false);
        setBorderPainted(false);
        setFocusPainted(false);
        setCursor(new Cursor(Cursor.HAND_CURSOR));
        setBorder(BorderFactory.createEmptyBorder(8, 18, 8, 18));

        // Animate the button when the mouse enters or exits.
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

    /**
     * Starts or reverses the hover fade animation.
     *
     * @param fadeIn true to animate toward the hover color,
     *               false to animate back to the normal color
     */
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

            // Stop the timer once the animation reaches its endpoint.
            if ((fadeIn && progress >= 1f) || (!fadeIn && progress <= 0f)) {
                timer.stop();
            }
        });

        timer.start();
    }

    /**
     * Paints the rounded animated background before drawing the button text.
     *
     * @param g the graphics context
     */
    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Blend between the normal and hover colors based on animation progress.
        g2.setColor(interpolate(NORMAL_COLOR, HOVER_COLOR, progress));
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), arc, arc);

        g2.dispose();
        super.paintComponent(g);
    }

    /**
     * Ensures the button keeps a minimum height for visual consistency.
     *
     * @return the preferred button size
     */
    @Override
    public Dimension getPreferredSize() {
        Dimension d = super.getPreferredSize();
        return new Dimension(d.width, Math.max(d.height, 36));
    }

    /**
     * Linearly interpolates between two colors.
     *
     * @param a the starting color
     * @param b the ending color
     * @param t the interpolation factor from 0 to 1
     * @return the blended color
     */
    private static Color interpolate(Color a, Color b, float t) {
        int r = (int) (a.getRed()   + (b.getRed()   - a.getRed())   * t);
        int g = (int) (a.getGreen() + (b.getGreen() - a.getGreen()) * t);
        int bv = (int) (a.getBlue() + (b.getBlue()  - a.getBlue())  * t);
        return new Color(r, g, bv);
    }

    /**
     * Produces a lighter version of a color by blending it toward white.
     *
     * @param c the original color
     * @param factor the amount of lightening, from 0 to 1
     * @return the lightened color
     */
    private static Color lighten(Color c, float factor) {
        int r = Math.min(255, (int) (c.getRed()   + (255 - c.getRed())   * factor));
        int g = Math.min(255, (int) (c.getGreen() + (255 - c.getGreen()) * factor));
        int b = Math.min(255, (int) (c.getBlue()  + (255 - c.getBlue())  * factor));
        return new Color(r, g, b);
    }
}