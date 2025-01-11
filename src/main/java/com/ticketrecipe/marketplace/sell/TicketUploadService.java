package com.ticketrecipe.marketplace.sell;

import com.google.zxing.*;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import com.ticketrecipe.api.ticket.TicketService;
import com.ticketrecipe.api.user.UserService;
import com.ticketrecipe.common.*;
import com.ticketrecipe.common.util.S3Util;
import com.ticketrecipe.getcertify.GetCertifyException;
import com.ticketrecipe.getcertify.verify.TicketVerificationResult;
import com.ticketrecipe.getcertify.verify.TicketVerificationService;
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
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
public class TicketUploadService {

    private final TicketVerificationService ticketVerificationService;
    private final TicketService ticketService;
    private final UserService userService;
    private final S3Util s3Util;

    private static final int DPI = 300;

    public List<Ticket> processUploadedTickets(String objectKey, String userId) throws IOException {

        log.info("Importing uploaded PDF Tickets from S3 with S3 objectKey: {}", objectKey);

        // Retrieve the user using the service
        User seller = userService.getUserById(userId);
        log.info("Seller: {}", seller);

        try (ResponseInputStream<GetObjectResponse> s3ObjectStream = s3Util.getObjectStream(objectKey);
             PDDocument document = PDDocument.load(s3ObjectStream);)
        {
            log.info("Importing PDF with {} pages.", document.getNumberOfPages());
            return extractTicketsFromDocument(document, seller);
        }
    }

    private List<Ticket> extractTicketsFromDocument(PDDocument document, User seller) throws IOException {
        String currentYearMonth = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMM"));
        String sessionUuid = UUID.randomUUID().toString();
        String objectKeyPath = String.format("%s/%s/", currentYearMonth, sessionUuid);

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
                        importedTickets.add(processSingleTicket(seller, objectKeyPath, currentDecodedText, currentTicketDoc, ticketIndex++));
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
                importedTickets.add(processSingleTicket(seller, objectKeyPath, currentDecodedText, currentTicketDoc, ticketIndex));
            } finally {
                currentTicketDoc.close(); // Ensure the document is closed
            }
        }
        log.info("Completed processing {} tickets.", importedTickets.size());
        return importedTickets;
    }

    private Ticket processSingleTicket(
            User seller,
            String objectKeyPath,
            String qrCodeData,
            PDDocument singlePageDoc,
            int ticketIndex
    ) throws IOException {

        // Import ticket details from PDF
        Ticket ticket = importTicket(singlePageDoc);
        log.info("Imported ticket #{} with details: {}", ticketIndex, ticket);

        try {
            TicketVerificationResult verifiedResult = ticketVerificationService.verify(qrCodeData);
            log.info("GetCertify! Certified ticket details for ticket #{} : {}", ticketIndex, verifiedResult);
            ticket.setEventId(verifiedResult.getEventId());
            ticket.setCertifiedId(verifiedResult.getId());

            // Check if the ticket already exists in the system
            Optional <Ticket> existingTicket = ticketService.findByCertifiedId(verifiedResult.getId());
            if (existingTicket.isPresent()) {
                log.info("Ticket #{} already existing in the system. .", ticketIndex);
                String thumbnailUrl = s3Util.generateSignedUrl(existingTicket.get().getThumbnailObjectKey());
                existingTicket.get().setThumbnailUrl(thumbnailUrl);
                return existingTicket.get(); // Early return; no need to save again
            }

            // Validate ticket details against certification result
            boolean isValid = Objects.equals(verifiedResult.getPurchaserName(), ticket.getPurchaser().getFullName()) &&
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

            // Save thumbnail and Generate thumbnail URL
            String thumbnailKey = saveThumbnail(singlePageDoc, objectKeyPath);
            String thumbnailUrl = s3Util.generateSignedUrl(thumbnailKey);

            //TO:DO
            ticket.setThumbnailObjectKey(thumbnailKey);
            ticket.setThumbnailUrl(thumbnailUrl);

            // Save only verified tickets
            String pdfObjectKey = s3Util.savePdfToS3(singlePageDoc, objectKeyPath + UUID.randomUUID().toString() + ".pdf");
            ticket.setPdfObjectKey(pdfObjectKey);
            ticket.setPurchaser(seller);
            ticketService.saveTicket(ticket);

        } catch (GetCertifyException gce) {
            log.error("GetCertify failed for ticket #{} with error #{}. Skipping validation.", ticketIndex, gce.getErrorCode());

            // Generate and set thumbnail URL
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
                .size(image.getWidth() / 2, image.getHeight() / 2)
                .asBufferedImage();
        return s3Util.saveImageToS3(thumbnail, objectKeyPath + UUID.randomUUID().toString() + "_thumbnail.jpg");
    }

    private Ticket importTicket(PDDocument document) {
        try {
            PDFTextStripper pdfStripper = new PDFTextStripper();
            String text = pdfStripper.getText(document);

            //log.info(text);

            String purchaserName = extractFieldValue(text, "Name", "Patron Name", "Patron Full Name");
            String category = extractFieldValue(text, "Category");
            String seatNumber =  extractFieldValue(text, "Seat Number", "Seat No.", "Seat");
            String row = extractFieldValue(text, "Row");
            String section = extractFieldValue(text, "Section");
            String entrance = extractFieldValue(text, "Entrance", "Gate");
            Price originalPrice = parsePrice(extractFieldValue(text, "Price", "Ticket Price"));

            return Ticket.builder()
                    .purchaser(User.builder().fullName(purchaserName).build())
                    .category(category)
                    .seat(seatNumber)
                    .row(row)
                    .section(section)
                    .entrance(entrance)
                    .price(originalPrice)
                    .ticketType(StringUtils.isAllBlank(seatNumber, row) ? TicketType.GENERAL_ADMISSION : TicketType.RESERVED_SEATING)
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

    private Price parsePrice(String priceString) {
        // Null or empty check
        if (priceString == null || priceString.isEmpty()) {
            return null;
        }

        priceString = priceString.trim();
        double amount = 0.0;
        String currency = null;

        // Regex to match supported currencies and amount, including symbols like "$"
        String regex = "(SGD|MYR|USD|\\$)?\\s*([\\d,]+(?:\\.\\d{1,2})?)\\s*(SGD|MYR|USD)?";
        Matcher matcher = Pattern.compile(regex).matcher(priceString);

        if (matcher.find()) {
            // Determine the currency: symbol at the start (group 1) or explicit currency code (group 3)
            if (matcher.group(1) != null) {
                if (matcher.group(1).equals("$")) {
                    currency = "SGD"; // Map "$" to "SGD"
                } else {
                    currency = matcher.group(1);
                }
            } else if (matcher.group(3) != null) {
                currency = matcher.group(3);
            }

            // Parse the numeric value
            String amountStr = matcher.group(2);
            if (amountStr != null) {
                amount = Double.parseDouble(amountStr.replace(",", ""));
            }
        }
        // If no amount or currency is found, return null
        if (amount == 0.0 || currency == null) {
            return null;
        }
        return new Price(amount, currency);
    }
}