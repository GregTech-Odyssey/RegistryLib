package com.gto.registrylib.util;

import lombok.experimental.UtilityClass;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Path;

import javax.imageio.ImageIO;

@UtilityClass
public class ImageUtil {

    public final Shape CIRCLE = new Circle();
    public final Shape SQUARE = new Square();
    public final Shape STAR = new Star();

    public BufferedImage generateIcon(Color color, Shape shape) {
        return generateIcon(16, 16, color, shape);
    }

    public BufferedImage generateIcon(Color color, Shape shape, Color backgroundColor) {
        return generateIcon(16, 16, color, shape, false, backgroundColor);
    }

    public BufferedImage generateIcon(int width, int height, Color color, Shape shape) {
        return generateIcon(width, height, color, shape, false);
    }

    public BufferedImage generateIcon(
                                      int width, int height, Color color, Shape shape, boolean addShadow) {
        return generateIconInternal(width, height, color, shape, addShadow, null);
    }

    public BufferedImage generateIcon(
                                      int width, int height, Color color, Shape shape, boolean addShadow, Color backgroundColor) {
        return generateIconInternal(width, height, color, shape, addShadow, backgroundColor);
    }

    private BufferedImage generateIconInternal(
                                               int width, int height, Color color, Shape shape, boolean addShadow, Color backgroundColor) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();
        try {
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            if (backgroundColor != null) {
                g2d.setColor(backgroundColor);
                g2d.fillRect(0, 0, width, height);
            }
            int margin = Math.max(1, Math.min(width, height) / 8);
            int shapeSize = Math.min(width, height) - margin * 2;
            int x = (width - shapeSize) / 2;
            int y = (height - shapeSize) / 2;

            if (addShadow) {
                drawShadow(g2d, x, y, shapeSize, shapeSize, shape);
            }

            shape.draw(g2d, x, y, shapeSize, shapeSize, color);

            if (shapeSize >= 10) {
                addHighlight(g2d, x, y, shapeSize, shapeSize, shape);
            }
        } finally {
            g2d.dispose();
        }

        return image;
    }

    private void drawShadow(Graphics2D g2d, int x, int y, int width, int height, Shape shape) {
        Color shadowColor = new Color(0, 0, 0, 60);
        int shadowOffset = 2;
        shape.draw(g2d, x + shadowOffset, y + shadowOffset, width, height, shadowColor);
    }

    private void addHighlight(Graphics2D g2d, int x, int y, int width, int height, Shape shape) {
        Color highlightColor = new Color(255, 255, 255, 100);
        g2d.setColor(highlightColor);

        int highlightWidth = width / 3;
        int highlightHeight = height / 3;

        shape.addHighlight(g2d, x, y, highlightWidth, highlightHeight);
    }

    public Path writeToStream(String name, BufferedImage image, Path path, OutputStream stream)
                                                                                                throws IOException {
        ImageIO.write(image, "png", stream);
        return path.resolve(name + ".png");
    }

    public abstract static class Shape {

        public abstract void draw(Graphics2D g2d, int x, int y, int width, int height, Color color);

        public abstract void addHighlight(Graphics2D g2d, int x, int y, int width, int height);
    }

    private static final class Circle extends Shape {

        @Override
        public void draw(Graphics2D g2d, int x, int y, int width, int height, Color color) {
            g2d.setColor(color);
            g2d.fillOval(x, y, width, height);
        }

        @Override
        public void addHighlight(Graphics2D g2d, int x, int y, int width, int height) {
            g2d.setColor(new Color(255, 255, 255, 100));
            g2d.fillOval(x + width / 4, y + height / 4, width, height);
        }
    }

    private static final class Square extends Shape {

        @Override
        public void draw(Graphics2D g2d, int x, int y, int width, int height, Color color) {
            g2d.setColor(color);
            g2d.fillRect(x, y, width, height);
        }

        @Override
        public void addHighlight(Graphics2D g2d, int x, int y, int width, int height) {
            g2d.setColor(new Color(255, 255, 255, 100));
            g2d.fillRect(x + width / 4, y + height / 4, width, height);
        }
    }

    private static final class Star extends Shape {

        @Override
        public void draw(Graphics2D g2d, int x, int y, int width, int height, Color color) {
            g2d.setColor(color);
            int centerX = x + width / 2;
            int centerY = y + height / 2;
            int outerRadius = Math.min(width, height) / 2;
            int innerRadius = outerRadius / 2;

            int[] xPoints = new int[10];
            int[] yPoints = new int[10];

            for (int i = 0; i < 10; i++) {
                double angle = Math.PI / 2 - i * Math.PI / 5;
                int radius = (i % 2 == 0) ? outerRadius : innerRadius;
                xPoints[i] = (int) (centerX + radius * Math.cos(angle));
                yPoints[i] = (int) (centerY - radius * Math.sin(angle));
            }

            g2d.fillPolygon(xPoints, yPoints, 10);
        }

        @Override
        public void addHighlight(Graphics2D g2d, int x, int y, int width, int height) {
            g2d.setColor(new Color(255, 255, 255, 100));
            g2d.fillOval(x + width / 2 - 1, y + height / 2 - 1, 2, 2);
        }
    }
}
