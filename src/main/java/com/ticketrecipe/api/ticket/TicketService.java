package com.ticketrecipe.api.ticket;

import com.ticketrecipe.common.Ticket;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class TicketService {

    private final TicketRepository ticketRepository;

    public TicketService(TicketRepository ticketRepository) {
        this.ticketRepository = ticketRepository;
    }

    /**
     * Save a single ticket.
     *
     * @param ticket The ticket to be saved.
     * @return The saved ticket.
     */
    @Transactional
    public Ticket saveTicket(Ticket ticket) {
        return ticketRepository.save(ticket);
    }

    /**
     * Save a list of tickets in batch.
     *
     * @param tickets List of tickets to be saved.
     * @return List of saved tickets.
     */
    @Transactional
    public List<Ticket> saveTickets(List<Ticket> tickets) {
        return ticketRepository.saveAll(tickets);
    }

    /**
     * Find tickets by event ID.
     *
     * @param eventId The event ID to filter tickets.
     * @return List of tickets for the given event.
     */
    public List<Ticket> getTicketsByEventId(String eventId) {
        return ticketRepository.findByEventId(eventId);
    }

    /**
     * Find a ticket by its ID.
     *
     * @param ticketId The ID of the ticket.
     * @return The ticket with the given ID.
     */
    public Ticket getTicketById(String ticketId) {
        return ticketRepository.findById(ticketId).orElse(null);
    }

    /**
     * Delete a ticket by ID.
     *
     * @param ticketId The ID of the ticket to delete.
     */
    @Transactional
    public void deleteTicket(String ticketId) {
        ticketRepository.deleteById(ticketId);
    }
}
