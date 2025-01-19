package com.ticketrecipe.api.event;

import com.ticketrecipe.common.Event;
import com.ticketrecipe.common.Ticket;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Builder
public record PurchasedEventTickets (
   PurchasedEvent event,
   String purchaserId,
   List<Ticket> tickets
) {}
