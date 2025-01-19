//package com.ticketrecipe.marketplace.sell;
//
//import com.ticketrecipe.api.listing.ListingService;
//import com.ticketrecipe.api.ticket.TicketImportService;
//import com.ticketrecipe.common.Ticket;
//import com.ticketrecipe.common.auth.CustomUserDetails;
//import jakarta.servlet.http.HttpSession;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.security.core.annotation.AuthenticationPrincipal;
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.RequestBody;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RestController;
//import java.util.List;
//import java.util.Map;
//
//@RestController
//@RequestMapping("/api/v1/sell/tickets")
//@RequiredArgsConstructor
//@Slf4j
//public class SellerController {
//
//    private final TicketImportService ticketUploadService;
//    private final ListingService listingService;
//    private static final String SESSION_TICKETS_KEY = "processedTickets";
//
//    // Endpoint to upload and process PDF
//    @PostMapping("/upload")
//    public ResponseEntity<?> processUploadedPdf(@RequestBody Map<String, String> payload, HttpSession session, @AuthenticationPrincipal CustomUserDetails userDetails) {
//        String objectKey = payload.get("objectKey");
//        if (objectKey == null || objectKey.isEmpty()) {
//            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("objectKey is required.");
//        }
//        try {
//            log.info("Processing PDF with objectKey: {}", objectKey);
//            // Fetch and process the PDF
//            List<Ticket> tickets = ticketUploadService.processUploadedPDF(objectKey,userDetails);
//            // Store the imported tickets in the session
//            session.setAttribute(SESSION_TICKETS_KEY, tickets);
//            log.info("Stored {} tickets in session.", tickets.size());
//            return ResponseEntity.ok(tickets);
//        } catch (Exception e) {
//            log.error("Error processing PDF: {}", e.getMessage(), e);
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error processing PDF from S3.");
//        }
//    }
//}