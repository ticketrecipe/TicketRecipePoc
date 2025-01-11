package com.ticketrecipe.common.util;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.Duration;

@Component
@RequiredArgsConstructor
@Slf4j
public class S3Util {

    private final S3Client s3Client;

    @Value("${s3.bucket.name}")
    private String s3BucketName;

    public String generateSignedUrl(String objectKey) {
        try (S3Presigner preSigner = S3Presigner.create()) {
            return preSigner.presignGetObject(GetObjectPresignRequest.builder()
                    .getObjectRequest(req -> req.bucket(s3BucketName).key(objectKey))
                    .signatureDuration(Duration.ofHours(1))
                    .build()).url().toString();
        }
    }

    public ResponseInputStream<GetObjectResponse> getObjectStream(String objectKey) {
        return s3Client.getObject(GetObjectRequest.builder().bucket(s3BucketName).key(objectKey).build());
    }

    public String saveImageToS3(BufferedImage image, String objectKey) throws IOException {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            javax.imageio.ImageIO.write(image, "jpg", outputStream);
            byte[] bytes = outputStream.toByteArray();

            s3Client.putObject(PutObjectRequest.builder()
                    .bucket(s3BucketName)
                    .key(objectKey)
                    .contentType("image/jpeg")
                    .contentLength((long) bytes.length)
                    .build(), RequestBody.fromBytes(bytes));

            return objectKey;
        }
    }

    public String savePdfToS3(PDDocument document, String objectKey) throws IOException {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            // Save the document to a byte array output stream
            document.save(outputStream);
            byte[] bytes = outputStream.toByteArray();

            // Upload the file to S3
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(s3BucketName)
                    .key(objectKey)
                    .contentType("application/pdf") // Set the content type
                    .contentLength((long) bytes.length) // Set content length
                    .build();

            // Upload the bytes to S3
            s3Client.putObject(putObjectRequest, RequestBody.fromBytes(bytes));

            log.info("Document saved to S3 with key: {}", objectKey);
            return objectKey;
        } catch (IOException e) {
            log.error("Failed to save document to S3 with key: {}", objectKey, e);
            throw e; // Rethrow exception or handle appropriately
        }
    }
}
