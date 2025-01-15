package com.ticketrecipe.api.ticket;

import com.ticketrecipe.common.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface TicketRepository extends JpaRepository<Ticket, String> {

    Optional<Ticket> findByCertifiedId(String id);

    List<Ticket> findByPurchaserId(String purchaserId);

    List<Ticket> findByPurchaserIdAndEventId(String userId, String eventId);
}
