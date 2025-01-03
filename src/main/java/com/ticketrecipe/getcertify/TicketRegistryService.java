package com.ticketrecipe.getcertify;

import com.ticketrecipe.common.SecureQrCodeHelper;
import com.ticketrecipe.common.security.SecurePayloadEncrypter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TicketRegistryService {

    private final TicketRegistryRepository ticketRegistryRepository;
    private final SecurePayloadEncrypter qrCodeEncrypter;
    private final SecureQrCodeHelper qrCodeGenerator;

    @Transactional
    public TicketRegistryResponse certifyTickets(TicketCertifyRequest request) {
        log.info("Certifying tickets for event: {}", request.getEventName());
        try {
            // Process each ticket and generate QR codes
            List<TicketRegistryResponse.TicketQRCode> qrCodeList = request.getTickets().stream().map(ticketDto -> {
                try {
                    // Create and save ticket with non-sensitive data directly here
                    CertifiedTicket ticket = new CertifiedTicket();
                    ticket.setEventId(request.getEventId());
                    ticket.setEventName(request.getEventName());
                    ticket.setStartDateTime(request.getStartDateTime());
                    ticket.setIssuer("TM"); //hard code for now.
                    ticket.setVenue(new CertifiedTicket.Venue (request.getVenue().getName(), request.getVenue().getAddress()));
                    ticket.setPrice(new CertifiedTicket.Price(ticketDto.getPrice().getAmount(), ticketDto.getPrice().getCurrency()));
                    ticket.setCategory(ticketDto.getCategory());
                    ticket.setEntrance(ticketDto.getEntrance());
                    ticket.setRow(ticketDto.getRow());
                    ticket.setSeat(ticketDto.getSeat());
                    ticket.setSection(ticketDto.getSection());

                    // Generate a unique Grab Certify reference ID by hashing the ticket's original barcode ID
                    String referenceId = hash(ticketDto.getBarcodeId());
                    ticket.setReferenceId(referenceId);

                    // Generate secret key for this ticket and encrypt sensitive data
                    //SecretKey secretKey = qrCodeEncrypter.secretKey();

                    //TODO Dev purpose - to use the same key.....
                    byte[] decodedKey = Base64.getDecoder().decode("T2KwjJha5e0OIlHsh+CR/ADCPpZtc20c21hxxi8Ni5A=");
                    SecretKey secretKey = new SecretKeySpec(decodedKey, 0, decodedKey.length, "AES");

                    ticket.setAesKey(Base64.getEncoder().encodeToString(secretKey.getEncoded()));

                    CertifiedTicket savedTicket = ticketRegistryRepository.save(ticket);

                    String encryptedPayload = qrCodeEncrypter.encrypt(ticketDto.getBarcodeId(), request.getPurchaser().getEmailAddress(),
                            request.getPurchaser().getName(), secretKey);

                    String qrCodeImage = qrCodeGenerator.generate(referenceId, encryptedPayload);

                    return new TicketRegistryResponse.TicketQRCode(
                            ticketDto.getBarcodeId(),
                            referenceId,
                            qrCodeImage
                    );
                }
                catch (DataIntegrityViolationException e) {
                    log.error("Duplicate ticket detected: {}", e.getMessage());
                    throw new TicketRegistryException("Duplicate ticket detected. The barcode has already been registered.", "DUPLICATE_TICKET");
                }
                catch (Exception e) {
                    log.error("Error processing ticket: {}", ticketDto, e);
                    throw new RuntimeException(e);
                }
            }).collect(Collectors.toList());

            return new TicketRegistryResponse(qrCodeList);
        }
        catch (Exception e) {
            log.error("Error certifying tickets for event: {}", request.getEventName(), e);
            throw e;
        }
    }
    public String hash(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(input.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error hashing input", e);
        }
    }
}