package com.github.starowo.core.util;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;

public class ImageBuilder {

    public BufferedImage image;
    public Graphics2D graphics;

    public ImageBuilder(int width, int height, int type) {
        this.image = new BufferedImage(width, height, type);
        this.graphics = this.image.createGraphics();
    }

    public void setColor(int r, int g, int b) {
        this.graphics.setColor(new Color(r, g, b));
    }

    public void setColor(int r, int g, int b, int a) {
        this.graphics.setColor(new Color(r, g, b, a));
    }

    public void setColor(Color color) {
        this.graphics.setColor(color);
    }

    public void setColor(String color) throws NoSuchFieldException, IllegalAccessException {
        Field field = Color.class.getField(color);
        this.graphics.setColor((Color) field.get(null));
    }

    public void setFont(String fontName, int style, int size) {
        this.graphics.setFont(new Font(fontName, style, size));
    }

    public void setFont(Font font) {
        this.graphics.setFont(font);
    }

    public void drawString(String text, int x, int y) {
        this.graphics.drawString(text, x, y);
    }

    public void fillRect(int x, int y, int width, int height) {
        this.graphics.fillRect(x, y, width, height);
    }

    public void drawRect(int x, int y, int width, int height) {
        this.graphics.drawRect(x, y, width, height);
    }

    public void drawImage(Image img, int x, int y, int width, int height) {
        this.graphics.drawImage(img, x, y, width, height, null);
    }

    public void drawLine(int x1, int y1, int x2, int y2) {
        this.graphics.drawLine(x1, y1, x2, y2);
    }

    public void drawOval(int x, int y, int width, int height) {
        this.graphics.drawOval(x, y, width, height);
    }

    public void fillOval(int x, int y, int width, int height) {
        this.graphics.fillOval(x, y, width, height);
    }

    public void drawArc(int x, int y, int width, int height, int startAngle, int arcAngle) {
        this.graphics.drawArc(x, y, width, height, startAngle, arcAngle);
    }

    public void fillArc(int x, int y, int width, int height, int startAngle, int arcAngle) {
        this.graphics.fillArc(x, y, width, height, startAngle, arcAngle);
    }

    public int stringWidth(String text) {
        return this.graphics.getFontMetrics().stringWidth(text);
    }

    public int getHeight() {
        return this.graphics.getFontMetrics().getHeight();
    }

    public int getAscent() {
        return this.graphics.getFontMetrics().getAscent();
    }

    public byte[] build() {
        this.graphics.dispose();
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        byte[] baImage = null;
        try {
            ImageIO.write(image, "png", os);
            baImage = os.toByteArray();
            os.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return baImage;
    }
}
