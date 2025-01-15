package com.ticketrecipe.api.event;

import com.ticketrecipe.common.auth.CustomUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@RequestMapping("/v1/events")
public class EventController {

    @Autowired
    private EventService eventService;

    @GetMapping("/purchases")
    public List<PurchasedEvent> getPurchasedEvents(@AuthenticationPrincipal CustomUserDetails userDetails) {
        return eventService.getPurchasedEvents(userDetails.getUsername());
    }

    @GetMapping("/purchases/{eventId}/tickets")
    public PurchasedEventTickets getTicketsForEvent(
            @PathVariable String eventId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return eventService.getTicketsForUserAndEvent(userDetails.getUsername(), eventId);
    }
}