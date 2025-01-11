package com.ticketrecipe.getcertify.validate;

import com.ticketrecipe.getcertify.CertifiedTicket;
import com.ticketrecipe.getcertify.registry.TicketRegistryRepository;
import com.ticketrecipe.common.security.SecurePayloadEncrypter;
import com.ticketrecipe.getcertify.GetCertifyException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Service
@RequiredArgsConstructor
@Slf4j
public class GetCertifyAccessService {

    private final SecurePayloadEncrypter securePayloadEncrypter;
    private final TicketRegistryRepository ticketRegistryRepository;

    @Value("${getcertify.qr.code.base.url}")
    private String qrCodeBaseUrl;

    public SecuredAccessCode getSecuredAccessCode(String qrCodeData) {
        try {
            if (!qrCodeData.startsWith(qrCodeBaseUrl)) {
                throw new GetCertifyException("Invalid GetCertify! QR Code URL", "INVALID_GC_QR_CODE");
            }
            String qrCodePath = qrCodeData.substring(qrCodeBaseUrl.length());
            String[] qrCodeParts = qrCodePath.split("\\.");
            if (qrCodeParts.length != 2) {
                throw new GetCertifyException("Invalid GetCertify! QR Code", "INVALID_GC_QR_CODE");
            }

            String refId = qrCodeParts[0];
            String encryptedPayloadBase64 = qrCodeParts[1];

            // Step 3: Decode the Base64-encoded payload
            byte[] encryptedPayloadBytes = Base64.getDecoder().decode(encryptedPayloadBase64);
            String encryptedPayload = new String(encryptedPayloadBytes, StandardCharsets.UTF_8);

            // Retrieve the ticket by uniqueId
            CertifiedTicket ticket = ticketRegistryRepository.findByReferenceId(refId)
                    .orElseThrow(() -> new GetCertifyException("Invalid Grab Certify QR Code", "INVALID_GC_QR_CODE"));

            String decryptedPayload = securePayloadEncrypter.decrypt(encryptedPayload, ticket.getAesKey());

            // Parse decrypted payload to extract the barcode and purchaser email
            String[] payloadParts = decryptedPayload.split(",");
            String storedBarcode = payloadParts[0].split(":")[1];  // Extract barcodeId
            String purchaserEmail = payloadParts[1].split(":")[1]; // Extract emailAddress

            return new SecuredAccessCode(storedBarcode);
        }
        catch (Exception ex) {
            log.error("Failed to validate ticket", ex);
            throw new RuntimeException("Ticket validation failed", ex);
        }
    }
}
