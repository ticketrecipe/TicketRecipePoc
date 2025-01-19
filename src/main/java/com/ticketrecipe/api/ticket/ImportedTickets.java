package com.ticketrecipe.api.ticket;

import com.ticketrecipe.common.Ticket;
import lombok.Builder;
import java.util.List;

@Builder
public record ImportedTickets (
        String eventId,
        String purchaserId,
        List<Ticket> tickets
) {}
