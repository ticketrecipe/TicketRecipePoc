package com.ticketrecipe.api.ticket;

import com.ticketrecipe.common.Ticket;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
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

    public Optional<Ticket> findByCertifyId(String id) {
        return ticketRepository.findByCertifyId(id);
    }
}
