package com.coursely.ui;

import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.io.InputStream;

/**
 * Utility class for loading custom fonts from application resources.
 * Falls back to a supplied default font if loading fails.
 */
public final class FontLoader {

    /**
     * Prevents instantiation of this utility class.
     */
    private FontLoader() {}

    /**
     * Loads a font from the classpath, derives it at the requested size,
     * and falls back to a provided font if loading fails.
     *
     * @param resourcePath the classpath location of the font resource
     * @param fontType the font type constant used by Font.createFont
     * @param size the target font size
     * @param fallback the fallback font to return if loading fails
     * @return the loaded font, or the fallback if unavailable
     */
    public static Font loadOrFallback(String resourcePath, int fontType, float size, Font fallback) {
        try (InputStream is = FontLoader.class.getResourceAsStream(resourcePath)) {
            if (is == null) {
                return fallback;
            }

            Font font = Font.createFont(fontType, is).deriveFont(size);

            // Registers the font with the local graphics environment
            // so it can be reused elsewhere in the application.
            GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(font);

            return font;
        } catch (Exception e) {
            return fallback;
        }
    }
}