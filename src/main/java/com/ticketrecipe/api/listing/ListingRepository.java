package com.ticketrecipe.api.listing;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ListingRepository extends JpaRepository<Listing, String> {

    List<Listing> findBySellerId(String sellerId);

    List<Listing> findByEventId(String eventId);
}
