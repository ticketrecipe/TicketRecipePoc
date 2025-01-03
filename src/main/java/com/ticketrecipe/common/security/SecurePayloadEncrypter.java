package com.ticketrecipe.common.security;

import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

@Component
public class SecurePayloadEncrypter
{
    public SecretKey secretKey() throws Exception {
        KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
        keyGenerator.init(256);  // AES-256
        return keyGenerator.generateKey();
    }

    public String encrypt(String barcodeId, String emailAddress, String purchaserName, SecretKey secretKey) throws Exception {
        // Generate AES GCM parameters
        byte[] iv = new byte[12];  // 12-byte IV for GCM
        SecureRandom random = new SecureRandom();
        random.nextBytes(iv);
        GCMParameterSpec gcmSpec = new GCMParameterSpec(128, iv); // 128-bit authentication tag

        // Prepare data payload including purchaser name, barcodeId, and emailAddress
        String payload = String.format("barcodeId:%s,emailAddress:%s,purchaserName:%s", barcodeId, emailAddress, purchaserName);

        // Encrypt payload
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, gcmSpec);
        byte[] cipherText = cipher.doFinal(payload.getBytes(StandardCharsets.UTF_8));

        // Combine IV, Ciphertext, and Authentication Tag into a single message
        byte[] combinedData = new byte[iv.length + cipherText.length];
        System.arraycopy(iv, 0, combinedData, 0, iv.length);
        System.arraycopy(cipherText, 0, combinedData, iv.length, cipherText.length);

        // Base64 encode the result
        return Base64.getEncoder().encodeToString(combinedData);
    }

    public String decrypt(String encryptedPayload, String encodedKey) throws Exception {

        byte[] decodedKey = Base64.getDecoder().decode(encodedKey);
        SecretKey secretKey = new SecretKeySpec(decodedKey, 0, decodedKey.length, "AES");

        // Decode combined payload
        byte[] decodedData = Base64.getDecoder().decode(encryptedPayload);

        // Extract IV and ciphertext
        byte[] iv = new byte[12];
        System.arraycopy(decodedData, 0, iv, 0, 12);
        byte[] cipherText = new byte[decodedData.length - 12];
        System.arraycopy(decodedData, 12, cipherText, 0, cipherText.length);

        // Decrypt
        GCMParameterSpec gcmSpec = new GCMParameterSpec(128, iv);
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.DECRYPT_MODE, secretKey, gcmSpec);
        byte[] decryptedBytes = cipher.doFinal(cipherText);

        return new String(decryptedBytes, StandardCharsets.UTF_8);
    }
}
