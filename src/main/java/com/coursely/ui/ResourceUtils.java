package com.coursely.ui;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

/**
 * Utility methods for loading images and icons from application resources.
 */
public final class ResourceUtils {

    /**
     * Prevents instantiation of this utility class.
     */
    private ResourceUtils() {}

    /**
     * Loads an image resource and returns it as a scaled ImageIcon.
     *
     * @param resourcePath the classpath location of the image resource
     * @param width the target icon width
     * @param height the target icon height
     * @return the scaled ImageIcon
     */
    public static ImageIcon loadIcon(String resourcePath, int width, int height) {
        Image img = loadImage(resourcePath);
        Image scaled = img.getScaledInstance(width, height, Image.SCALE_SMOOTH);
        return new ImageIcon(scaled);
    }

    /**
     * Loads an image resource and scales it to a target height
     * while preserving the original aspect ratio.
     *
     * @param resourcePath the classpath location of the image resource
     * @param targetHeight the desired output height
     * @return the scaled ImageIcon
     */
    public static ImageIcon loadIconKeepAspect(String resourcePath, int targetHeight) {
        Image img = loadImage(resourcePath);

        int w = img.getWidth(null);
        int h = img.getHeight(null);

        // Fall back to the original image if dimensions are unavailable.
        if (w <= 0 || h <= 0) {
            return new ImageIcon(img);
        }

        int targetWidth = (int) Math.round((double) w * targetHeight / h);
        Image scaled = img.getScaledInstance(targetWidth, targetHeight, Image.SCALE_SMOOTH);
        return new ImageIcon(scaled);
    }

    /**
     * Loads an image resource from the classpath.
     *
     * @param resourcePath the classpath location of the image resource
     * @return the loaded image
     * @throws IllegalStateException if the resource cannot be found or decoded
     * @throws RuntimeException if an I/O error occurs while loading the image
     */
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