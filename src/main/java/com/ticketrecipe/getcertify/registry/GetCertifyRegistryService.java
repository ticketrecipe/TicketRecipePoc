package com.ticketrecipe.getcertify.registry;

import com.ticketrecipe.api.event.EventRepository;
import com.ticketrecipe.api.event.EventService;
import com.ticketrecipe.common.Event;
import com.ticketrecipe.common.Price;
import com.ticketrecipe.common.getcertify.SecureQrCodeHelper;
import com.ticketrecipe.common.getcertify.SecurePayloadEncrypter;
import com.ticketrecipe.getcertify.CertifiedTicket;
import com.ticketrecipe.getcertify.GetCertifyException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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
public class GetCertifyRegistryService {

    @Autowired
    private TicketRegistryRepository ticketRegistryRepository;
    @Autowired
    private SecurePayloadEncrypter qrCodeEncrypter;
    @Autowired
    private SecureQrCodeHelper qrCodeGenerator;
    @Autowired
    private EventService eventService;

    @Transactional
    public TicketRegistryResponse certifyTickets(TicketCertifyRequest request) {
        log.info("Certifying tickets for event: {}", request.getEventName());
        try {
            Event event = eventService.getEvent(request.getEventId())
                    .orElseGet(() -> {
                        Event newEvent = Event.builder()
                                .id(request.getEventId())
                                .name(request.getEventName())
                                .startDateTime(request.getStartDateTime())
                                .issuer("TM")
                                .venueName(request.getVenue().getName())
                                .address(request.getVenue().getAddress())
                                .build();
                        return eventService.createEvent(newEvent);
                    });

            List<TicketRegistryResponse.TicketQRCode> qrCodeList = request.getTickets().stream().map(ticketDto -> {
                try {
                    // Check if ticket already exists by barcode ID
                    String referenceId = hash(ticketDto.getBarcodeId());

                    CertifiedTicket ticket = ticketRegistryRepository.findByReferenceId(referenceId)
                            .orElse(new CertifiedTicket()); // Create new or overwrite existing

                    ticket.setEvent(event);
                    ticket.setPrice(new Price(ticketDto.getPrice().getAmount(), ticketDto.getPrice().getCurrency()));
                    ticket.setCategory(ticketDto.getCategory());
                    ticket.setType(ticketDto.getType());
                    ticket.setEntrance(ticketDto.getEntrance());
                    ticket.setRow(ticketDto.getRow());
                    ticket.setSeat(ticketDto.getSeat());
                    ticket.setSection(ticketDto.getSection());
                    ticket.setReferenceId(referenceId);

                    // Generate secret key and encrypt sensitive data
                    byte[] decodedKey = Base64.getDecoder().decode("T2KwjJha5e0OIlHsh+CR/ADCPpZtc20c21hxxi8Ni5A=");
                    SecretKey secretKey = new SecretKeySpec(decodedKey, 0, decodedKey.length, "AES");
                    ticket.setAesKey(Base64.getEncoder().encodeToString(secretKey.getEncoded()));

                    // Encrypt payload and generate QR code
                    String encryptedPayload = qrCodeEncrypter.encrypt(
                            ticketDto.getBarcodeId(),
                            request.getPurchaser().getEmailAddress(),
                            request.getPurchaser().getName(),
                            secretKey
                    );
                    String qrCodeImage = qrCodeGenerator.generate(referenceId, encryptedPayload);

                    // Save or update ticket in registry
                    ticketRegistryRepository.save(ticket);

                    return new TicketRegistryResponse.TicketQRCode(
                            ticketDto.getBarcodeId(),
                            referenceId,
                            qrCodeImage
                    );

                } catch (Exception e) {
                    log.error("Error processing ticket: {}", ticketDto, e);
                    throw new GetCertifyException("Error processing ticket.", "PROCESSING_ERROR");
                }
            }).collect(Collectors.toList());

            return new TicketRegistryResponse(qrCodeList);

        } catch (Exception e) {
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

    @Transactional
    public boolean lockTickets(List<String> ticketIds) {
        try {
            // Step 1: Fetch all tickets by their ticket IDs
            List<CertifiedTicket> tickets = ticketRegistryRepository.findAllByIdIn(ticketIds);

            // Step 2: Validate if any tickets are already locked
            List<String> alreadyLocked = tickets.stream()
                    .filter(CertifiedTicket::isLocked)
                    .map(CertifiedTicket::getId)
                    .collect(Collectors.toList());

            if (!alreadyLocked.isEmpty()) {
                log.warn("Some tickets are already locked: {}", alreadyLocked);
                throw new GetCertifyException("Some tickets are already locked: " + alreadyLocked, "ALREADY_LOCKED");
            }

            // Step 3: Lock all tickets
            tickets.forEach(ticket -> ticket.setLocked(true));

            // Step 4: Bulk update in database
            ticketRegistryRepository.saveAll(tickets);

            log.info("Successfully locked {} tickets.", tickets.size());
            return true;
        } catch (GetCertifyException e) {
            log.error("Error locking tickets: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error while locking tickets: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to lock tickets due to an internal error.");
        }
    }
}