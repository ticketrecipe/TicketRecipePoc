package com.ticketrecipe.api.ticket;

import com.google.zxing.*;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import com.ticketrecipe.api.event.EventRepository;
import com.ticketrecipe.api.user.UserService;
import com.ticketrecipe.common.*;
import com.ticketrecipe.common.auth.CustomUserDetails;
import com.ticketrecipe.common.util.S3Util;
import com.ticketrecipe.getcertify.GetCertifyException;
import com.ticketrecipe.getcertify.verify.GetCertifyVerificationService;
import com.ticketrecipe.getcertify.verify.TicketVerificationResult;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import org.apache.commons.lang3.StringUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class TicketImportService {

    private final GetCertifyVerificationService ticketVerificationService;
    private final TicketService ticketService;
    private final EventRepository eventRepository;
    private final UserService userService;
    private final S3Util s3Util;
    private static final int DPI = 300;

    public ImportedTickets processUploadedPdf(String eventId,
                                           String objectKey, CustomUserDetails userDetails) throws IOException {

        log.info("Process uploaded Pdf Tickets from S3 with S3 objectKey: {} for user details: {}", objectKey, userDetails);

        Event event = eventRepository.findById(eventId).orElseThrow(() ->
                new EntityNotFoundException("Event not found for ID: " + eventId));

        User user = userService.getUserById(userDetails.getUserId());
        log.info("User: {}", user);

        try (ResponseInputStream<GetObjectResponse> s3ObjectStream = s3Util.getObjectStream(objectKey);
             PDDocument document = PDDocument.load(s3ObjectStream);)
        {
            log.info("Importing PDF with {} pages.", document.getNumberOfPages());
            List<Ticket> tickets = extractTicketsFromPdf(event, document, user);
            ImportedTickets importedTickets = ImportedTickets.builder()
                    .eventId(event.getId())
                    .tickets(tickets)
                    .purchaserId(user.getId())
                    .build();
            return importedTickets;
        }
    }

    private List<Ticket> extractTicketsFromPdf(Event selectedEvent, PDDocument document, User user) throws IOException {
        PDFRenderer pdfRenderer = new PDFRenderer(document);
        PDDocument currentTicketDoc = null;
        int ticketIndex = 1;
        String currentDecodedText = null;

        List<Ticket> importedTickets = new ArrayList<>();
        for (int pageIndex = 0; pageIndex < document.getNumberOfPages(); pageIndex++) {
            try {
                BufferedImage pageImage = pdfRenderer.renderImageWithDPI(pageIndex, DPI);
                String decodedText = null;
                try {
                    BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(new BufferedImageLuminanceSource(pageImage)));
                    MultiFormatReader reader = new MultiFormatReader();

                    Map<DecodeHintType, Object> hints = new HashMap<>();
                    hints.put(DecodeHintType.POSSIBLE_FORMATS, Arrays.asList(BarcodeFormat.QR_CODE, BarcodeFormat.CODE_128));
                    hints.put(DecodeHintType.TRY_HARDER, Boolean.TRUE);

                    Result result = reader.decode(bitmap, hints);
                    decodedText = result.getText();
                }
                catch (Exception e) {
                    log.error("Error reading or finding QrBarcode for start of a new ticket, {}", e.getMessage());
                }

                if (decodedText != null) {
                    log.info("Detected start of a new ticket on page {}: {}", pageIndex + 1, decodedText);

                    if (currentTicketDoc != null) {
                        importedTickets.add(processSingleTicket(selectedEvent, user, currentDecodedText, currentTicketDoc, ticketIndex++));
                    }

                    currentDecodedText = decodedText;
                    currentTicketDoc = new PDDocument();
                }

                if (currentTicketDoc != null) {
                    currentTicketDoc.addPage(document.getPage(pageIndex));
                }
            } catch (Exception e) {
                log.error("Error processing page {}: {}", pageIndex + 1, e.getMessage());
            }
        }

        // Finalize the last ticket
        if (currentTicketDoc != null) {
            try {
                importedTickets.add(processSingleTicket(selectedEvent, user, currentDecodedText, currentTicketDoc, ticketIndex));
            } finally {
                currentTicketDoc.close(); // Ensure the document is closed
            }
        }
        log.info("Completed processing {} tickets.", importedTickets.size());
        return importedTickets;
    }

    private Ticket processSingleTicket(
            Event selectedEvent, User user, String qrCodeData,
            PDDocument singlePageDoc,
            int ticketIndex
    ) throws IOException {

        // Import ticket details from PDF
        Ticket ticket = importTicket(singlePageDoc);
        log.info("Imported ticket #{} with details: {}", ticketIndex, ticket);

        try {
            TicketVerificationResult verifiedResult = ticketVerificationService.verify(qrCodeData);
            log.info("GetCertify! Certified ticket details for ticket #{} : {}", ticketIndex, verifiedResult);
            ticket.setCertifyId(verifiedResult.getId());
            ticket.setEvent(verifiedResult.getEvent());

            // Check if the ticket already exists in the system
            Optional <Ticket> existingTicket = ticketService.findByCertifyId(verifiedResult.getId());
            if (existingTicket.isPresent()) {
                log.info("Ticket #{} already existing in the system. .", ticketIndex);
                String thumbnailUrl = s3Util.generateSignedUrl(existingTicket.get().getThumbnailObjectKey());
                existingTicket.get().setThumbnailUrl(thumbnailUrl);
                return existingTicket.get(); // Early return; no need to save again
            }

            // Validate imported ticket details against certified details
            boolean isValid =
                    Objects.equals(verifiedResult.getEvent().getId(), selectedEvent.getId()) &&
                    Objects.equals(verifiedResult.getPurchaserName(), ticket.getPrintedName()) &&
                    Objects.equals(verifiedResult.getSection(), ticket.getSection()) &&
                    Objects.equals(verifiedResult.getRow(), ticket.getRow()) &&
                    Objects.equals(verifiedResult.getSeat(), ticket.getSeat()) &&
                    Objects.equals(verifiedResult.getPrice(), ticket.getPrice());

            if (!isValid) {
                log.error("Data mismatch for ticket #{}. Marking as INVALID.", ticketIndex);
                ticket.setStatus(TicketStatus.INVALID);
                return ticket; // Early return; do not save or process further
            }

            // Mark as verified and set event details
            ticket.setStatus(TicketStatus.GC_VERIFIED);
            ticket.setGetCertifyQrCode(verifiedResult.getGetCertifyQrCode());

            String objectKeyPath = String.format("%s/%s/", verifiedResult.getEvent().getId(), verifiedResult.getRefId());

            // Save thumbnail and Generate thumbnail URL
            String thumbnailKey = saveThumbnail(singlePageDoc, objectKeyPath);
            String thumbnailUrl = s3Util.generateSignedUrl(thumbnailKey);

            //TO:DO
            ticket.setThumbnailObjectKey(thumbnailKey);
            ticket.setThumbnailUrl(thumbnailUrl);

            // Save only verified tickets
            String pdfObjectKey = s3Util.savePdfToS3(singlePageDoc, objectKeyPath + verifiedResult.getRefId() + ".pdf");
            ticket.setPdfObjectKey(pdfObjectKey);
            ticket.setPurchaser(user);
            ticketService.saveTicket(ticket);

        } catch (GetCertifyException gce) {
            log.error("GetCertify failed for ticket #{} with error #{}. Skipping validation.", ticketIndex, gce.getErrorCode());

            String objectKeyPath = String.format("%s/%s/", "temp", UUID.randomUUID().toString());

            // Generate and set thumbnail URL to temp
            String thumbnailKey = saveThumbnail(singlePageDoc, objectKeyPath);
            String thumbnailUrl = s3Util.generateSignedUrl(thumbnailKey);
            //TO:DO
            ticket.setThumbnailUrl(thumbnailUrl);
            ticket.setStatus(TicketStatus.valueOf(gce.getErrorCode()));
            return ticket;
        }
        return ticket;
    }

    private String saveThumbnail(PDDocument document, String objectKeyPath) throws IOException {
        PDFRenderer pdfRenderer = new PDFRenderer(document);
        BufferedImage image = pdfRenderer.renderImageWithDPI(0, DPI);
        BufferedImage thumbnail = Thumbnails.of(image)
                .size(image.getWidth() / 4, image.getHeight() / 4)
                .asBufferedImage();
        return s3Util.saveImageToS3(thumbnail, objectKeyPath  + "thumbnail.jpg");
    }

    private Ticket importTicket(PDDocument document) {
        try {
            PDFTextStripper pdfStripper = new PDFTextStripper();
            String text = pdfStripper.getText(document);

            System.out.println(text);

            String printedName = extractFieldValue(text, "Name", "Patron Name", "Patron Full Name");
            String category = extractFieldValue(text, "Category");
            String seatNumber =  extractFieldValue(text, "Seat Number", "Seat No.", "Seat");
            String row = extractFieldValue(text, "Row");
            String section = extractFieldValue(text, "Section");
            String entrance = extractFieldValue(text, "Entrance", "Gate");
            Optional<Price> originalPrice = Price.from(extractFieldValue(text, "Price", "Ticket Price"));

            return Ticket.builder()
                    .printedName(printedName)
                    .category(category)
                    .seat(seatNumber)
                    .row(row)
                    .section(section)
                    .entrance(entrance)
                    .price(originalPrice.get())
                    .ticketType(StringUtils.isAllBlank(seatNumber, row) ? TicketType.GA : TicketType.RS)
                    .build();

        } catch (IOException e) {
            log.error("Failed to extract ticket information: {}", e.getMessage());
            return null;
        }
    }

    private String extractFieldValue(String text, String... fieldNames) {
        String[] lines = text.split("\\n");

        for (String fieldName : fieldNames) {
            for (String line : lines) {
                line = line.trim();

                // Check for "key: value" format
                if (line.toLowerCase().startsWith(fieldName.toLowerCase() + ":")) {
                    return line.substring(line.indexOf(":") + 1).trim(); // Return value after colon
                }

                // Check for "key" followed by value in the next line
                if (line.equalsIgnoreCase(fieldName) && Arrays.asList(lines).indexOf(line) + 1 < lines.length) {
                    return lines[Arrays.asList(lines).indexOf(line) + 1].trim(); // Return the next line as the value
                }

                // Check for lines containing the field name followed by a value
                if (line.toLowerCase().contains(fieldName.toLowerCase())) {
                    // Split the line into parts and capture the value after the field name
                    String lowerLine = line.toLowerCase();
                    int startIndex = lowerLine.indexOf(fieldName.toLowerCase()) + fieldName.length();
                    String valuePart = line.substring(startIndex).trim(); // Get everything after the field name

                    // If there's a value after the field name, return it
                    if (!valuePart.isEmpty()) {
                        return valuePart.trim();
                    }
                }
            }
        }
        return null; // Return null if no value found
    }
}