package com.github.starowo.mirai.command.cipher;

import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Queue;

public class MoonlightWindow {

    static void floodFill(BufferedImage image, int startX, int startY, Color targetColor, Color replacementColor) {
        Queue<Point> queue = new ArrayDeque<>();
        queue.add(new Point(startX, startY));

        while (!queue.isEmpty()) {
            Point point = queue.poll();
            int x = point.x;
            int y = point.y;

            if (x < 0 || y < 0 || x >= image.getWidth() || y >= image.getHeight()) {
                continue;
            }

            int currentColor = image.getRGB(x, y);

            if (currentColor != targetColor.getRGB()) {
                continue;
            }

            image.setRGB(x, y, replacementColor.getRGB());

            queue.add(new Point(x + 1, y));
            queue.add(new Point(x - 1, y));
            queue.add(new Point(x, y + 1));
            queue.add(new Point(x, y - 1));
        }
    }

    public static BufferedImage[] generateImage(String chars, int pixPerChar) {
        int charNum = chars.length();
        int width = charNum * pixPerChar;
        int height = pixPerChar;

        Color bgColor = new Color(255, 255, 255);
        Color ansColor = new Color(255, 255, 255);
        Color charColor = new Color(36, 36, 36);
        Color splitColor = new Color(127, 127, 127);

        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();

        g.setColor(bgColor);
        g.fillRect(0, 0, width, height);

        Font font = new Font("SimHei", Font.PLAIN, pixPerChar);
        g.setFont(font);

        for (int idx = 0; idx < charNum; idx++) {
            int x = idx * pixPerChar;
            g.setColor(charColor);
            g.drawString(String.valueOf(chars.charAt(idx)), x, pixPerChar - 45);
        }

        // Convert to BufferedImage
        BufferedImage ansImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        ansImage.createGraphics().drawImage(image, 0, 0, width, height, null);

        g.dispose();
        floodFill(ansImage, 0, 0, ansColor, new Color(180, 180, 180));

        // Prepare question image
        BufferedImage questionImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        g = questionImage.createGraphics();
        g.drawImage(ansImage, 0, 0, null);
        g.setColor(bgColor);
        g.dispose();
        floodFill(questionImage, 0, 0, new Color(180, 180, 180), charColor);
        questionImage = applyGlowEffect(questionImage);
        g = questionImage.createGraphics();
        for (int idx = 0; idx < charNum; idx++) {
            int x = idx * pixPerChar;
            g.setColor(splitColor);
            g.drawLine(x, 0, x, height);
        }

        for (int y : new int[]{0, height - 1}) {
            g.setColor(splitColor);
            g.drawLine(0, y, width - 1, y);
        }

        for (int x : new int[]{0, width - 1}) {
            g.setColor(splitColor);
            g.drawLine(x, 0, x, height - 1);
        }

        g.dispose();

        return new BufferedImage[]{questionImage, ansImage};
    }

    public static BufferedImage applyGlowEffect(BufferedImage inputImage) {
        int width = inputImage.getWidth();
        int height = inputImage.getHeight();
        BufferedImage outputImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        boolean[] processed = new boolean[width * height];
        // Iterate through each pixel
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                Color pixelColor = new Color(inputImage.getRGB(x, y));

                // Check if the pixel is white
                if (pixelColor.getRed() == 255 && pixelColor.getGreen() == 255 && pixelColor.getBlue() == 255) {
                    // Apply glow effect to the pixel's surroundings
                    for (int dy = -20; dy <= 20; dy++) {
                        for (int dx = -20; dx <= 20; dx++) {
                            int newX = x + dx;
                            int newY = y + dy;

                            // Ensure new coordinates are within bounds
                            if (newX >= 0 && newX < width && newY >= 0 && newY < height) {
                                processed[newY*width+newX] = true;
                                double p = 1 - Math.sqrt(dx * dx + dy * dy) / 28.28;
                                int alpha = (int) (36+219 * (p*p*p));// Adjust the gradient strength here
                                if ((outputImage.getRGB(newX, newY) >> 8 & 0xFF) < alpha) {
                                    Color glowColor = new Color(alpha, alpha, alpha);
                                    outputImage.setRGB(newX, newY, glowColor.getRGB());
                                }
                            }
                        }
                    }
                } else {
                    // Copy the original pixel to the output image
                    if (!processed[y*width+x])
                        outputImage.setRGB(x, y, pixelColor.getRGB());
                }
            }
        }

        return outputImage;
    }

    public static void main(String[] args) {
        System.out.println((int)(255*Math.pow(1 - Math.sqrt(10*10+10*10) / 14.14, 2)));
        String chars = "白云千载空xx";
        int pixPerChar = 300;

        BufferedImage[] questionImage = generateImage(chars, pixPerChar);

        try {
            File output = new File("output/" + chars + "_Q.png");
            output.mkdirs();
            ImageIO.write(questionImage[0], "png", output);
            File output1 = new File("output/" + chars + "_A.png");
            output1.mkdirs();
            ImageIO.write(questionImage[1], "png", output1);
            System.out.println("Success! Please refer to " + output.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
