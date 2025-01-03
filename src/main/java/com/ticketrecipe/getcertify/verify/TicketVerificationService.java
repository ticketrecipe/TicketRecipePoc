package com.ticketrecipe.getcertify.verify;

import com.ticketrecipe.getcertify.CertifiedTicket;
import com.ticketrecipe.getcertify.TicketRegistryRepository;
import com.ticketrecipe.common.security.SecurePayloadEncrypter;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class TicketVerificationService {

    private final TicketRegistryRepository ticketRegistryRepository;
    private final SecurePayloadEncrypter qrCodeDecrypter;

    @Value("${getcertify.qr.code.base.url}")
    private String qrCodeBaseUrl;

    @Transactional
    public TicketVerificationResult validateTicket(String qrCodeData) {
        try {
            if (!qrCodeData.startsWith(qrCodeBaseUrl)) {
                throw new TicketVerificationException("Invalid GetCertify! QR Code URL", "INVALID_GC_QR_CODE");
            }

            String qrCodePath = qrCodeData.substring(qrCodeBaseUrl.length());
            String[] qrCodeParts = qrCodePath.split("\\.");
            if (qrCodeParts.length != 2) {
                throw new TicketVerificationException("Invalid GetCertify! QR Code", "INVALID_GC_QR_CODE");
            }

            String refId = qrCodeParts[0];
            String encryptedPayload = qrCodeParts[1];

            // Retrieve the ticket by uniqueId
            CertifiedTicket ticket = ticketRegistryRepository.findByReferenceId(refId)
                    .orElseThrow(() -> new TicketVerificationException("Invalid GetCertify! QR Code", "INVALID_GC_QR_CODE"));

            String decryptedPayload = qrCodeDecrypter.decrypt(encryptedPayload, ticket.getAesKey());

            // Parse decrypted payload to extract the barcode and purchaser email
            String[] payloadParts = decryptedPayload.split(",");
            String barcodeId = payloadParts[0].split(":")[1];  // Extract barcodeId
            String purchaserEmailAddress = payloadParts[1].split(":")[1]; // Extract emailAddress
            String purchaserName = payloadParts[2].split(":")[1]; // Extract emailAddress

            // Build and return the response with verifiable ticket details
            return new TicketVerificationResult(
                    refId,
                    purchaserEmailAddress,
                    ticket.getEventId(),
                    ticket.getEventName(),
                    ticket.getStartDateTime(),
                    ticket.getIssuer(),
                    ticket.getVenue().getName(),
                    ticket.getVenue().getAddress(),
                    purchaserName,
                    ticket.getRow(),
                    ticket.getSeat(),
                    ticket.getSection(),
                    ticket.getPrice().getAmount(),
                    ticket.getPrice().getCurrency()
            );
        } catch (Exception e) {
            log.error("GetCertify! QR code failed validation", e.getMessage());
            throw new TicketVerificationException("Invalid GetCertify! QR Code", "INVALID_GC_QR_CODE");
        }
    }
}