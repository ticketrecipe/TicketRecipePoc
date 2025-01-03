package com.ticketrecipe.getcertify;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TicketRegistryRepository extends JpaRepository<CertifiedTicket, Long> {
    Optional<CertifiedTicket> findByReferenceId(String referenceId);
}