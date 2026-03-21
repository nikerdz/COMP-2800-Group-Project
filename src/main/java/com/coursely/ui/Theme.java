package com.coursely.ui;

import java.awt.Color;
import java.awt.Font;

public final class Theme {

    private Theme() {}

    // --------------
    // Brand Colors 
    // --------------
    public static final Color BRAND_BLUE = color("#2fa4d7");
    public static final Color BRAND_BROWN = color("#3e2c23");
    public static final Color BRAND_OFFWHITE = color("#fffefb");

    // Timetable block color options
    public static final Color BLOCK_BLUE  = color("#c2dcff");
    public static final Color BLOCK_AQUA  = color("#c7fae9");
    public static final Color BLOCK_LEMON = color("#fff3c2");
    public static final Color BLOCK_PEACH = color("#ffd9c2");
    public static final Color BLOCK_PINK  = color("#ffc2e8");
    public static final Color BLOCK_LILAC = color("#d8c7fa");

    public static final Color[] BLOCK_PALETTE = {
            BLOCK_BLUE, BLOCK_AQUA, BLOCK_LEMON, BLOCK_PEACH, BLOCK_PINK, BLOCK_LILAC
    };

    // -----------
    // Font sizes 
    // -----------
    public static final float SIZE_TITLE = 36f;
    public static final float SIZE_HEADING = 30f;
    public static final float SIZE_BODY = 16f;
    public static final float SIZE_SMALL = 13f;

    // -------------------------
    // Fonts (loaded from resources)
    // Paths are relative to src/main/resources
    // -------------------------
    private static final String FONT_COOPER_BOLD = "/fonts/Cooper-Bold.ttf";
    private static final String FONT_DONGLE_REGULAR = "/fonts/Dongle-Regular.ttf";

    // Title font: Cooper Bold (fallback to Serif Bold)
    public static final Font FONT_TITLE = FontLoader.loadOrFallback(
            FONT_COOPER_BOLD,
            Font.TRUETYPE_FONT,
            SIZE_TITLE,
            new Font("Serif", Font.BOLD, (int) SIZE_TITLE)
    );

    // Headings: use Cooper Bold 
    public static final Font FONT_HEADING = FontLoader.loadOrFallback(
            FONT_COOPER_BOLD,
            Font.TRUETYPE_FONT,
            SIZE_HEADING,
            new Font("SansSerif", Font.BOLD, (int) SIZE_HEADING)
    );

    // Body text: Dongle Regular (fallback to SansSerif)
    public static final Font FONT_BODY = FontLoader.loadOrFallback(
            FONT_DONGLE_REGULAR,
            Font.TRUETYPE_FONT,
            SIZE_BODY,
            new Font("SansSerif", Font.PLAIN, (int) SIZE_BODY)
    );

    public static final Font FONT_SMALL = FontLoader.loadOrFallback(
            FONT_DONGLE_REGULAR,
            Font.TRUETYPE_FONT,
            SIZE_SMALL,
            new Font("SansSerif", Font.PLAIN, (int) SIZE_SMALL)
    );

    // -------------------------
    // Helpers
    // -------------------------
    private static Color color(String hex) {
        return Color.decode(hex);
    }
}