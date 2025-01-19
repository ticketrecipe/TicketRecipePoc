package com.ticketrecipe.api.listing;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ListingRepository extends JpaRepository<Listing, String> {

    Optional<List<Listing>> findBySellerId(String sellerId);

    List<Listing> findByEventId(String eventId);
}
