package com.coursely.ui;

import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.io.InputStream;

public final class FontLoader {

    private FontLoader() {}

    public static Font loadOrFallback(String resourcePath, int fontType, float size, Font fallback) {
        try (InputStream is = FontLoader.class.getResourceAsStream(resourcePath)) {
            if (is == null) {
                return fallback;
            }

            Font font = Font.createFont(fontType, is).deriveFont(size);

            // Optional: register it for general use
            GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(font);

            return font;
        } catch (Exception e) {
            return fallback;
        }
    }
}