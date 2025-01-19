package com.ticketrecipe.api.listing;

import com.ticketrecipe.common.auth.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@Slf4j
@RequestMapping("/v1/listings")
public class ListingController {

    @Autowired
    private  ListingService listingService;

    @PostMapping
    public ResponseEntity<CreateListingResponse> create(@RequestBody CreateListingRequest createListingRequest,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        List<Listing> listings = listingService.create(userDetails.getUserId(), createListingRequest.eventId,
                createListingRequest.ticketIds);
        // Create response object
        CreateListingResponse response = new CreateListingResponse(
                createListingRequest.eventId(),
                userDetails.getUserId(),
                listings.size(),
                createListingRequest.ticketIds().size(),
                listings
        );
        log.info("Created {} listings with {} tickets.", response.totalListings(), response.totalTickets());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/publish")
    public ResponseEntity<?> list(@Valid @RequestBody ListingConfirmation confirmation) {
        try {
            List<Listing> updatedListings = listingService.publish(confirmation);
            log.info("Successfully published {} listings.", updatedListings.size());
            return ResponseEntity.ok(updatedListings);

        } catch (Exception e) {
            log.error("Error confirming and publishing listings: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error confirming and publishing listings.");
        }
    }

    public record CreateListingRequest (
            String eventId,
            List<String> ticketIds
    ) {}

    public record CreateListingResponse(
            String eventId,
            String purchaserId,
            int totalListings,
            int totalTickets,
            List<Listing> listings
    ) {}
}
