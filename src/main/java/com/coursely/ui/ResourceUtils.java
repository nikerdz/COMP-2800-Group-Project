package com.coursely.ui;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

public final class ResourceUtils {

    private ResourceUtils() {}

    public static ImageIcon loadIcon(String resourcePath, int width, int height) {
        Image img = loadImage(resourcePath);
        Image scaled = img.getScaledInstance(width, height, Image.SCALE_SMOOTH);
        return new ImageIcon(scaled);
    }

    public static ImageIcon loadIconKeepAspect(String resourcePath, int targetHeight) {
        Image img = loadImage(resourcePath);

        int w = img.getWidth(null);
        int h = img.getHeight(null);
        if (w <= 0 || h <= 0) {
            return new ImageIcon(img);
        }

        int targetWidth = (int) Math.round((double) w * targetHeight / h);
        Image scaled = img.getScaledInstance(targetWidth, targetHeight, Image.SCALE_SMOOTH);
        return new ImageIcon(scaled);
    }

    public static Image loadImage(String resourcePath) {
        URL url = ResourceUtils.class.getResource(resourcePath);
        if (url == null) {
            throw new IllegalStateException("Resource not found: " + resourcePath);
        }
        try {
            BufferedImage img = ImageIO.read(url);
            if (img == null) {
                throw new IllegalStateException("Failed to decode image: " + resourcePath);
            }
            return img;
        } catch (IOException e) {
            throw new RuntimeException("Failed to load image: " + resourcePath, e);
        }
    }
}