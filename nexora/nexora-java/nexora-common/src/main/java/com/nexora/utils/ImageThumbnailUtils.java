package com.nexora.utils;

import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Component
public class ImageThumbnailUtils {

    private static final int MAX_WIDTH = 320;
    private static final int MAX_HEIGHT = 180;

    public void generateThumbnail(String sourcePath, String targetPath) throws IOException {
        BufferedImage sourceImage = ImageIO.read(Path.of(sourcePath).toFile());
        if (sourceImage == null) {
            throw new IOException("无法读取图片文件");
        }

        int sourceWidth = sourceImage.getWidth();
        int sourceHeight = sourceImage.getHeight();
        double scale = Math.min(
                Math.min((double) MAX_WIDTH / sourceWidth, (double) MAX_HEIGHT / sourceHeight),
                1D
        );
        int targetWidth = Math.max((int) Math.round(sourceWidth * scale), 1);
        int targetHeight = Math.max((int) Math.round(sourceHeight * scale), 1);

        BufferedImage thumbnail = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = thumbnail.createGraphics();
        try {
            graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            graphics.setColor(Color.WHITE);
            graphics.fillRect(0, 0, targetWidth, targetHeight);
            graphics.drawImage(sourceImage, 0, 0, targetWidth, targetHeight, null);
        } finally {
            graphics.dispose();
        }

        Path target = Path.of(targetPath);
        Files.createDirectories(target.getParent());
        ImageIO.write(thumbnail, "jpg", target.toFile());
    }
}
