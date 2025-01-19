package com.ticketrecipe.api.ticket;

import com.ticketrecipe.common.auth.CustomUserDetails;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/tickets")
@Slf4j
public class TicketController {

    @Autowired
    private TicketImportService ticketImportService;

    @PostMapping("/import")
    public ResponseEntity<?> importTickets(@RequestBody TicketImportRequest ticketImportRequest,
                                           @AuthenticationPrincipal CustomUserDetails userDetails) {
        try {
            String eventId = ticketImportRequest.eventId;
            String s3ObjectKey = ticketImportRequest.objectKey;

            ImportedTickets importedTickets = ticketImportService.processUploadedPdf(eventId, s3ObjectKey, userDetails);
            return ResponseEntity.status(HttpStatus.CREATED).body(importedTickets);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error during ticket import: " + e.getMessage());
        }
    }

    public record TicketImportRequest (
        String eventId,
        String objectKey
    ) {}
}
