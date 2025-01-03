package com.ticketrecipe.api.ticket;

import com.ticketrecipe.common.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TicketRepository extends JpaRepository<Ticket, String> {
    List<Ticket> findByEventId(String eventId);
}
