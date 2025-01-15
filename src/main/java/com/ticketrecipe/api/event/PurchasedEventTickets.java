package com.ticketrecipe.api.event;

import com.ticketrecipe.common.Event;
import com.ticketrecipe.common.Ticket;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Builder
@Data
public class PurchasedEventTickets {
    private PurchasedEvent event;
    private String purchaserId;
    private List<Ticket> tickets;
}
