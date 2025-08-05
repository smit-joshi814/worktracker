package com.worktracker.utils;

import java.awt.*;
import java.awt.image.BufferedImage;

public class IconUtils {

    public static Image createTrayIcon() {
        int size = 16;
        BufferedImage icon = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = icon.createGraphics();
        enableQualityRendering(g2d);

        // Gradient clock face
        GradientPaint gp = new GradientPaint(0, 0, new Color(100, 149, 237), size, size, new Color(65, 105, 225));
        g2d.setPaint(gp);
        g2d.fillOval(0, 0, size, size);

        // Border
        g2d.setColor(new Color(25, 25, 112));
        g2d.setStroke(new BasicStroke(1.2f));
        g2d.drawOval(1, 1, size - 2, size - 2);

        // Clock hands
        g2d.setColor(Color.WHITE);
        g2d.setStroke(new BasicStroke(1.6f));
        g2d.drawLine(8, 8, 8, 4); // hour
        g2d.drawLine(8, 8, 11, 8); // minute

        // Center
        g2d.fillOval(7, 7, 2, 2);

        g2d.dispose();
        return icon;
    }

    public static Image createAppIcon() {
        int size = 32;
        BufferedImage icon = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = icon.createGraphics();
        enableQualityRendering(g2d);

        // Clock face with radial gradient
        g2d.setPaint(new GradientPaint(0, 0, new Color(70, 130, 180), size, size, new Color(30, 90, 150)));
        g2d.fillOval(2, 2, size - 4, size - 4);

        // Border
        g2d.setColor(new Color(25, 25, 112));
        g2d.setStroke(new BasicStroke(2.0f));
        g2d.drawOval(2, 2, size - 4, size - 4);

        // Hour markers
        g2d.setColor(Color.WHITE);
        for (int i = 0; i < 12; i++) {
            double angle = Math.toRadians(i * 30 - 90);
            int x1 = (int) (size / 2 + 12 * Math.cos(angle));
            int y1 = (int) (size / 2 + 12 * Math.sin(angle));
            int x2 = (int) (size / 2 + 9 * Math.cos(angle));
            int y2 = (int) (size / 2 + 9 * Math.sin(angle));
            g2d.drawLine(x1, y1, x2, y2);
        }

        // Clock hands
        g2d.setStroke(new BasicStroke(3.0f));
        g2d.drawLine(16, 16, 16, 8);  // Hour hand
        g2d.drawLine(16, 16, 24, 16); // Minute hand

        // Center dot
        g2d.fillOval(14, 14, 4, 4);

        g2d.dispose();
        return icon;
    }

    private static void enableQualityRendering(Graphics2D g2d) {
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
    }
}
