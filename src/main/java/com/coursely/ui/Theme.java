package com.coursely.ui;

import java.awt.Color;
import java.awt.Font;

/**
 * Centralized theme configuration for application colors and fonts.
 * Provides brand colors, timetable block colors, font sizing constants,
 * and helper methods for color conversion.
 */
public final class Theme {

    /**
     * Prevents instantiation of this utility class.
     */
    private Theme() {}

    // --------------
    // Brand colors
    // --------------

    // Primary accent color used across buttons and borders.
    public static final Color BRAND_BLUE = color("#2fa4d7");

    // Primary text color used for headings and labels.
    public static final Color BRAND_BROWN = color("#3e2c23");

    // Main off-white background color used throughout the UI.
    public static final Color BRAND_OFFWHITE = color("#fffefb");

    // Timetable block color options.
    public static final Color BLOCK_BLUE  = color("#c2dcff");
    public static final Color BLOCK_AQUA  = color("#c7fae9");
    public static final Color BLOCK_LEMON = color("#fff3c2");
    public static final Color BLOCK_PEACH = color("#ffd9c2");
    public static final Color BLOCK_PINK  = color("#ffc2e8");
    public static final Color BLOCK_LILAC = color("#d8c7fa");

    // Shared palette for choosing section block colors.
    public static final Color[] BLOCK_PALETTE = {
            BLOCK_BLUE, BLOCK_AQUA, BLOCK_LEMON, BLOCK_PEACH, BLOCK_PINK, BLOCK_LILAC
    };

    // -----------
    // Font sizes
    // -----------

    // Large title size used for major headings.
    public static final float SIZE_TITLE = 36f;

    // Standard heading size.
    public static final float SIZE_HEADING = 30f;

    // Standard body text size.
    public static final float SIZE_BODY = 16f;

    // Smaller text size for compact labels or secondary text.
    public static final float SIZE_SMALL = 13f;

    // -------------------------
    // Font resource paths
    // Paths are relative to src/main/resources
    // -------------------------

    // Resource path for the Cooper Bold font.
    private static final String FONT_COOPER_BOLD = "/fonts/Cooper-Bold.ttf";

    // Resource path for the Dongle Regular font.
    private static final String FONT_DONGLE_REGULAR = "/fonts/Dongle-Regular.ttf";

    // Title font loaded from resources, with Serif Bold as a fallback.
    public static final Font FONT_TITLE = FontLoader.loadOrFallback(
            FONT_COOPER_BOLD,
            Font.TRUETYPE_FONT,
            SIZE_TITLE,
            new Font("Serif", Font.BOLD, (int) SIZE_TITLE)
    );

    // Heading font loaded from resources, with SansSerif Bold as a fallback.
    public static final Font FONT_HEADING = FontLoader.loadOrFallback(
            FONT_COOPER_BOLD,
            Font.TRUETYPE_FONT,
            SIZE_HEADING,
            new Font("SansSerif", Font.BOLD, (int) SIZE_HEADING)
    );

    // Body font loaded from resources, with SansSerif Plain as a fallback.
    public static final Font FONT_BODY = FontLoader.loadOrFallback(
            FONT_DONGLE_REGULAR,
            Font.TRUETYPE_FONT,
            SIZE_BODY,
            new Font("SansSerif", Font.PLAIN, (int) SIZE_BODY)
    );

    // Smaller body font variant loaded from resources.
    public static final Font FONT_SMALL = FontLoader.loadOrFallback(
            FONT_DONGLE_REGULAR,
            Font.TRUETYPE_FONT,
            SIZE_SMALL,
            new Font("SansSerif", Font.PLAIN, (int) SIZE_SMALL)
    );

    // -------------------------
    // Helper methods
    // -------------------------

    /**
     * Converts a hex color string into a Color object.
     *
     * @param hex the hex color string
     * @return the decoded Color
     */
    private static Color color(String hex) {
        return Color.decode(hex);
    }

    /**
     * Converts a Color object into a hex string in the form #rrggbb.
     *
     * @param c the color to convert
     * @return the hex string, or null if the input color is null
     */
    public static String colorToHex(Color c) {
        if (c == null) return null;
        return String.format("#%02x%02x%02x", c.getRed(), c.getGreen(), c.getBlue());
    }

    /**
     * Converts a hex string into a Color object.
     * Returns null if the input is blank or invalid.
     *
     * @param hex the hex color string
     * @return the decoded Color, or null if conversion fails
     */
    public static Color hexToColor(String hex) {
        if (hex == null || hex.isBlank()) return null;
        try {
            return Color.decode(hex);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}