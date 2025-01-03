package com.ticketrecipe.getcertify.validate;

import com.ticketrecipe.getcertify.CertifiedTicket;
import com.ticketrecipe.getcertify.TicketRegistryRepository;
import com.ticketrecipe.common.security.SecurePayloadEncrypter;
import com.ticketrecipe.getcertify.verify.TicketVerificationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class TicketAccessControlService {

    private final SecurePayloadEncrypter qrCodeDecryptor;
    private final TicketRegistryRepository ticketRegistryRepository;

    @Value("${getcertify.qr.code.base.url}")
    private String qrCodeBaseUrl;

    public EmbeddedAccessCode getEmbeddedBarCode(String qrCodeData) {
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
                    .orElseThrow(() -> new TicketVerificationException("Invalid Grab Certify QR Code", "INVALID_GC_QR_CODE"));

            String decryptedPayload = qrCodeDecryptor.decrypt(encryptedPayload, ticket.getAesKey());

            // Parse decrypted payload to extract the barcode and purchaser email
            String[] payloadParts = decryptedPayload.split(",");
            String storedBarcode = payloadParts[0].split(":")[1];  // Extract barcodeId
            String purchaserEmail = payloadParts[1].split(":")[1]; // Extract emailAddress

            return new EmbeddedAccessCode(storedBarcode);
        }
        catch (Exception ex) {
            log.error("Failed to validate ticket", ex);
            throw new RuntimeException("Ticket validation failed", ex);
        }
    }
}
