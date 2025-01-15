package com.ticketrecipe.marketplace.sell;

import com.ticketrecipe.api.listing.Listing;
import com.ticketrecipe.api.listing.ListingService;
import com.ticketrecipe.common.Ticket;
import com.ticketrecipe.common.auth.CustomUserDetails;
import com.ticketrecipe.common.listing.ConfirmListingDto;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/sell/tickets")
@RequiredArgsConstructor
@Slf4j
public class SellerController {

    private final TicketUploadService ticketUploadService;
    private final ListingService listingService;
    private static final String SESSION_TICKETS_KEY = "processedTickets";

    // Endpoint to upload and process PDF
    @PostMapping("/upload")
    public ResponseEntity<?> processUploadedPdf(@RequestBody Map<String, String> payload, HttpSession session, @AuthenticationPrincipal CustomUserDetails userDetails) {
        String objectKey = payload.get("objectKey");
        if (objectKey == null || objectKey.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("objectKey is required.");
        }
        try {
            log.info("Processing PDF with objectKey: {}", objectKey);
            // Fetch and process the PDF
            List<Ticket> tickets = ticketUploadService.processUploadedTickets(objectKey,userDetails);
            // Store the imported tickets in the session
            session.setAttribute(SESSION_TICKETS_KEY, tickets);
            log.info("Stored {} tickets in session.", tickets.size());
            return ResponseEntity.ok(tickets);
        } catch (Exception e) {
            log.error("Error processing PDF: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error processing PDF from S3.");
        }
    }

    // New Endpoint to create listings
    @PostMapping("/create-listings")
    public ResponseEntity<?> createListings(@RequestBody Map<String, List<String>> payload, HttpSession session) {
        List<String> ticketIds = payload.get("ticket_ids");
        if (ticketIds == null || ticketIds.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("ticket_ids are required.");
        }

        // Retrieve processed tickets from session
        List<Ticket> importedTickets = (List<Ticket>) session.getAttribute(SESSION_TICKETS_KEY);
        if (importedTickets == null || importedTickets.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("No imported tickets found in session.");
        }

        // Filter the tickets by selected IDs
        List<Ticket> selectedTickets = importedTickets.stream()
                .filter(ticket -> ticketIds.contains(ticket.getId()))
                .toList();

        if (selectedTickets.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("No matching tickets found.");
        }

        try {
            // Generate listings based on selected tickets
            List<Listing> listings = listingService.createListings(selectedTickets);
            // Create response object
            CreateListingResponse response = new CreateListingResponse(
                    listings.size(),               // Total number of listings
                    selectedTickets.size(),        // Total number of tickets
                    listings                       // List of listings
            );
            log.info("Created {} listings with {} tickets.", response.totalListings(), response.totalTickets());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error creating listings: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error creating listings.");
        }
    }

    @PostMapping("/confirm-listing")
    public ResponseEntity<?> confirmAndPublishListings(@RequestBody ConfirmListingRequest request) {
        try {
            // Update listings and lock tickets
            List<Listing> updatedListings = listingService.confirmListings(request.listings());

            log.info("Successfully published {} listings.", updatedListings.size());
            return ResponseEntity.ok(updatedListings);

        } catch (Exception e) {
            log.error("Error confirming and publishing listings: {}", e.getMessage(), e);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error confirming and publishing listings.");
        }
    }

    // Request DTO for confirm listing(s)
    public record ConfirmListingRequest(
            List<ConfirmListingDto> listings
    ) {}

    // Response DTO for create-listing(s)
    public record CreateListingResponse(
            int totalListings,
            int totalTickets,
            List<Listing> listings
    ) {}
}