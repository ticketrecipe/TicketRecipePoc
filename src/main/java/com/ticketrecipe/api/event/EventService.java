package com.ticketrecipe.api.event;

import com.ticketrecipe.api.ticket.TicketRepository;
import com.ticketrecipe.common.Event;
import com.ticketrecipe.common.Ticket;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class EventService {

    @Autowired
    private TicketRepository ticketRepository;
    @Autowired
    private EventRepository eventRepository;

    public Optional<Event> getEvent(String eventId) {
        return eventRepository.findById(eventId);
    }

    public Event createEvent(Event event) {
        event.setBannerImageUrl("https://s3.us-west-2.amazonaws.com/static.ticketrecipe.com/events/default/banner.png");
        event.setSmallImageUrl("https://s3.us-west-2.amazonaws.com/static.ticketrecipe.com/events/default/small_banner.png");
        return eventRepository.save(event);
    }

    // Get purchased events for the user, along with the ticket count
    public List<PurchasedEvent> getPurchasedEvents(String userId) {
        // Fetch all tickets for the user
        List<Ticket> tickets = ticketRepository.findByPurchaserId(userId);

        // Group tickets by eventId and count the number of tickets for each event
        Map<String, Long> eventTicketCount = tickets.stream()
                .collect(Collectors.groupingBy(ticket -> ticket.getEvent().getId(), Collectors.counting()));

        // Extract unique eventIds from tickets
        List<String> eventIds = new ArrayList<>(eventTicketCount.keySet());

        // Fetch event details by eventIds (if necessary, otherwise you could just use the eventId)
        List<Event> events = eventRepository.findByIdIn(eventIds);

        // Map the events to PurchasedEventDTO and set the ticket count
        return events.stream().map(event -> {
            long ticketQuantity = eventTicketCount.getOrDefault(event.getId(), 0L);
            return PurchasedEvent.builder()
                    .eventId(event.getId())
                    .name(event.getName())
                    .venue(event.getVenueName())
                    .date(event.getStartDateTime())
                    .location(event.getAddress())
                    .eventBannerImageUrl(event.getBannerImageUrl())
                    .eventSmallImageUrl(event.getSmallImageUrl())
                    .ticketQuantity(ticketQuantity)
                    .build();
        }).collect(Collectors.toList());
    }

    public PurchasedEventTickets getTicketsForUserAndEvent(String userId, String eventId) {
        List<Ticket> tickets = ticketRepository.findByPurchaserIdAndEventId(userId, eventId);
        if (tickets.isEmpty()) {
            throw new EntityNotFoundException("No tickets found for the user and event.");
        }

        Event event = eventRepository.findById(eventId).orElseThrow(() ->
                new EntityNotFoundException("Event not found for ID: " + eventId));

        PurchasedEventTickets purchasedEventTickets = PurchasedEventTickets.builder()
                .event(PurchasedEvent.builder()
                        .eventId(event.getId())
                        .name(event.getName())
                        .venue(event.getVenueName())
                        .date(event.getStartDateTime())
                        .location(event.getAddress())
                        .eventBannerImageUrl(event.getBannerImageUrl())
                        .eventSmallImageUrl(event.getSmallImageUrl())
                        .build())
                .tickets(tickets)
                .purchaserId(userId)
                .build();

        return purchasedEventTickets;
    }
}