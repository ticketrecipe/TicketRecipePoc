package com.ticketrecipe.getcertify.verify;

import com.ticketrecipe.common.Event;
import com.ticketrecipe.common.TicketStatus;
import com.ticketrecipe.getcertify.CertifiedTicket;
import com.ticketrecipe.getcertify.GetCertifyException;
import com.ticketrecipe.getcertify.registry.TicketRegistryRepository;
import com.ticketrecipe.common.getcertify.SecurePayloadEncrypter;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Service
@RequiredArgsConstructor
@Slf4j
public class TicketVerificationService {

    private final TicketRegistryRepository ticketRegistryRepository;
    private final SecurePayloadEncrypter qrCodeDecrypter;

    @Value("${getcertify.qr.code.base.url}")
    private String qrCodeBaseUrl;

    @Transactional
    public TicketVerificationResult verify(String qrCodeData) {
        try {
            // Step 1: Validate the QR code URL format
            if (!qrCodeData.startsWith(qrCodeBaseUrl)) {
                throw new GetCertifyException("Invalid GetCertify! QR Code URL", TicketStatus.INVALID.toString());
            }

            // Step 2: Extract the reference ID and encrypted payload from the QR code
            String qrCodePath = qrCodeData.substring(qrCodeBaseUrl.length());
            String[] qrCodeParts = qrCodePath.split("\\.");
            if (qrCodeParts.length != 2) {
                throw new GetCertifyException("Invalid GetCertify! QR Code", TicketStatus.INVALID.toString());
            }

            String refId = qrCodeParts[0];
            String encryptedPayloadBase64 = qrCodeParts[1];

            // Step 3: Decode the Base64-encoded payload
            byte[] encryptedPayloadBytes = Base64.getDecoder().decode(encryptedPayloadBase64);
            String encryptedPayload = new String(encryptedPayloadBytes, StandardCharsets.UTF_8);

            // Step 3: Retrieve the ticket by its reference ID
            CertifiedTicket ticket = ticketRegistryRepository.findByReferenceId(refId)
                    .orElseThrow(() -> new GetCertifyException("Invalid GetCertify! QR Code", TicketStatus.INVALID.toString()));

            log.info("CertifiedTicket : {}", ticket);
            // Check if the ticket is locked because being put on sale
            if (ticket.isLocked()) {
                throw new GetCertifyException("Ticket has been locked for resale.", TicketStatus.LOCKED.toString());
            }

            // Step 4: Decrypt the payload
            String decryptedPayload = qrCodeDecrypter.decrypt(encryptedPayload, ticket.getAesKey());

            // Step 5: Parse the decrypted payload to extract the barcode and purchaser details
            String[] payloadParts = decryptedPayload.split(",");
            String barcodeId = payloadParts[0].split(":")[1];  // Extract barcodeId
            String purchaserEmailAddress = payloadParts[1].split(":")[1]; // Extract emailAddress
            String purchaserName = payloadParts[2].split(":")[1]; // Extract purchaser name

            // Step 6: Build and return the response with verifiable ticket details
            TicketVerificationResult result =
                    TicketVerificationResult.builder()
                            .id(ticket.getId())
                            .refId(refId)
                            .purchaserName(purchaserName)
                            .purchaserEmailAddress(purchaserEmailAddress)
                            .event(ticket.getEvent())
                            .category(ticket.getCategory())
                            .type(ticket.getType())
                            .row(ticket.getRow())
                            .seat(ticket.getSeat())
                            .section(ticket.getSection())
                            .price(ticket.getPrice())
                            .build();
            return result;

        } catch (GetCertifyException e) {
            throw e; // Re-throw custom exceptions
        } catch (Exception e) {
            log.error("GetCertify! QR code failed validation: {}", e.getMessage());
            throw new GetCertifyException("Invalid GetCertify! QR Code", TicketStatus.INVALID.toString());
        }
    }

}