package com.ticketrecipe.api.ticket;

import com.ticketrecipe.common.Ticket;
import com.ticketrecipe.common.TicketStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class TicketService {

    private final TicketRepository ticketRepository;

    public Ticket saveTicket(Ticket ticket) {
        return ticketRepository.save(ticket);
    }

    public Optional<Ticket> findById(String id) {
        return ticketRepository.findById(id);
    }

    public void deleteById(String id) {
        ticketRepository.deleteById(id);
    }

    public Optional<Ticket> findByCertifiedId(String id) {
        return ticketRepository.findByCertifiedId(id);
    }
}
