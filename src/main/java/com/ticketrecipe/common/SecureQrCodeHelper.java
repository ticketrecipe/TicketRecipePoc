package com.ticketrecipe.common;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
public class SecureQrCodeHelper {

    @Value("${getcertify.qr.code.base.url}")
    private String qrCodeBaseUrl;

    public String generate(String referenceId, String encryptedPayload) {
        try {
            // Encode the encrypted payload into Base64
            String base64EncryptedPayload = Base64.getEncoder().encodeToString(encryptedPayload.getBytes(StandardCharsets.UTF_8));

            // Construct the data to be encoded in the QR code
            String data = qrCodeBaseUrl + referenceId + "." + base64EncryptedPayload;
            log.info("Generated QR code for {} is {}", referenceId, data);

            // Generate the QR code matrix with no padding
            Map<EncodeHintType, Object> hints = new HashMap<>();
            hints.put(EncodeHintType.MARGIN, 0); // Remove default padding
            BitMatrix bitMatrix = new QRCodeWriter().encode(data, BarcodeFormat.QR_CODE, 190, 190, hints);

            // Create a BufferedImage for the QR code
            BufferedImage qrCodeImage = new BufferedImage(190, 190, BufferedImage.TYPE_INT_ARGB);
            Graphics2D qrGraphics = qrCodeImage.createGraphics();

            // Set QR code colors (white background, black pixels)
            qrGraphics.setColor(Color.WHITE);
            qrGraphics.fillRect(0, 0, 190, 190); // Fill the background
            qrGraphics.setColor(Color.BLACK);

            // Draw the QR code matrix
            for (int x = 0; x < bitMatrix.getWidth(); x++) {
                for (int y = 0; y < bitMatrix.getHeight(); y++) {
                    if (bitMatrix.get(x, y)) {
                        qrGraphics.fillRect(x, y, 1, 1); // Black pixel for each "true" value
                    }
                }
            }
            qrGraphics.dispose();

            // Load the logo from resources
            InputStream logoStream = getClass().getResourceAsStream("/LogoSample.png");
            if (logoStream == null) {
                throw new FileNotFoundException("Logo file not found in resources");
            }
            BufferedImage logoImage = ImageIO.read(logoStream);

            // Calculate logo size (20% of QR code)
            int logoWidth = qrCodeImage.getWidth() / 5;
            int logoHeight = qrCodeImage.getHeight() / 5;

            // Resize the logo
            Image scaledLogo = logoImage.getScaledInstance(logoWidth, logoHeight, Image.SCALE_SMOOTH);

            // Draw the logo on the center of the QR code
            Graphics2D g = qrCodeImage.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

            int x = (qrCodeImage.getWidth() - logoWidth) / 2;
            int y = (qrCodeImage.getHeight() - logoHeight) / 2;
            g.drawImage(scaledLogo, x, y, null);
            g.dispose();

            // Extend the QR code image to add text below
            int textHeight = 30; // Space for the text background
            String text = "GetCertify!";
            Font font = new Font("Arial", Font.BOLD, 16);

            // Measure text dimensions
            BufferedImage tempImage = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
            Graphics2D tempGraphics = tempImage.createGraphics();
            tempGraphics.setFont(font);
            FontMetrics fontMetrics = tempGraphics.getFontMetrics();
            int textWidth = fontMetrics.stringWidth(text);
            tempGraphics.dispose();

            int totalHeight = qrCodeImage.getHeight() + textHeight;

            BufferedImage finalImage = new BufferedImage(qrCodeImage.getWidth(), totalHeight, BufferedImage.TYPE_INT_ARGB);
            Graphics2D finalGraphics = finalImage.createGraphics();

            // Draw the QR code
            finalGraphics.drawImage(qrCodeImage, 0, 0, null);

            // Draw the black background for the text spanning the full width
            finalGraphics.setColor(Color.BLACK);
            finalGraphics.fillRect(0, qrCodeImage.getHeight(), qrCodeImage.getWidth(), textHeight);

            // Draw the white text
            finalGraphics.setFont(font);
            finalGraphics.setColor(Color.WHITE);
            int textX = (qrCodeImage.getWidth() - textWidth) / 2;
            int textY = qrCodeImage.getHeight() + ((textHeight - fontMetrics.getHeight()) / 2) + fontMetrics.getAscent();
            finalGraphics.drawString(text, textX, textY);
            finalGraphics.dispose();

            // Convert the final image to Base64
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ImageIO.write(finalImage, "PNG", outputStream);
            String base64Image = Base64.getEncoder().encodeToString(outputStream.toByteArray());
            return "data:image/png;base64," + base64Image;

        } catch (WriterException | IOException e) {
            throw new RuntimeException("Failed to generate QR code", e);
        }
    }

}
