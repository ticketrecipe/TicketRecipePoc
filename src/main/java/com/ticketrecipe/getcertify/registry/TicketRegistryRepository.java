package com.ticketrecipe.getcertify.registry;

import com.ticketrecipe.getcertify.CertifiedTicket;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface TicketRegistryRepository extends JpaRepository<CertifiedTicket, Long> {
    Optional<CertifiedTicket> findByReferenceId(String referenceId);

    List<CertifiedTicket> findAllByIdIn(List<String> ticketIds); // Updated to use ticket ID
}