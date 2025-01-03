package com.ticketrecipe.api.listing;

import com.ticketrecipe.common.Price;
import com.ticketrecipe.common.SeatInventory;
import com.ticketrecipe.common.Ticket;
import com.ticketrecipe.common.TicketType;
import com.ticketrecipe.common.listing.ConfirmedListing;
import com.ticketrecipe.tickets.sell.SellerController;
import com.ticketrecipe.tickets.sell.TicketResponse;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ListingService {

    private final ListingRepository listingRepository;

    @Autowired
    public ListingService(ListingRepository listingRepository) {
        this.listingRepository = listingRepository;
    }

    private Listing saveListing (List<Ticket> tickets) {
        Ticket firstTicket = tickets.get(0);
        Listing listing = Listing.builder()
                .eventId(firstTicket.getEventId())
                .seller(firstTicket.getPurchaser())
                .quantity(tickets.size())
                .category(firstTicket.getCategory())
                .seatRow(firstTicket.getRow())
                .section(firstTicket.getSection())
                .ticketType(firstTicket.getTicketType())
                .originalTicketPrice(firstTicket.getPrice())
                .status(ListingStatus.DRAFT)
                .build();

        List<SeatInventory> seats = new ArrayList<SeatInventory>();
        for (Ticket ticket : tickets) {
            if (TicketType.RESERVED_SEATING.equals(ticket.getTicketType())){
                seats.add(SeatInventory.builder()
                        .seat(ticket.getSeat())
                        .row(ticket.getRow())
                        .section(ticket.getSection())
                                .listing(listing)
                        .build());
            }
        }
        listing.setSeats(seats);
        Price originalPrice = firstTicket.getPrice();
        listing.setMaximumSellingPrice(new Price(originalPrice.getAmount() * 3, originalPrice.getCurrency()));
        return listingRepository.save(listing);
    }

    @Transactional
    public List<Listing> createListings (List<Ticket> tickets) {
        List<Listing> listings = new ArrayList<>();

        // Sort the tickets by section, row, and seat number
        List<Ticket> sortedTickets = tickets.stream()
                .sorted((t1, t2) -> {
                    int sectionCompare = t1.getSection().compareTo(t2.getSection());
                    if (sectionCompare != 0) return sectionCompare;

                    int rowCompare = t1.getRow().compareTo(t2.getRow());
                    if (rowCompare != 0) return rowCompare;

                    // Comparing seat numbers as integers if possible
                    return Integer.compare(Integer.parseInt(t1.getSeat()), Integer.parseInt(t2.getSeat()));
                })
                .toList();

        List<Ticket> currentListing = new ArrayList<>();
        Ticket previousTicket = null;

        // Loop through sorted tickets and group them into listings
        for (Ticket ticket : sortedTickets) {
            if (previousTicket == null || isConsecutive(previousTicket, ticket)) {
                currentListing.add(ticket); // Add to the current group if consecutive
            }
            else {
                Listing savedListing = saveListing (currentListing);
                listings.add(savedListing); // Save the previous group and start a new one


                currentListing = new ArrayList<>();
                currentListing.add(ticket); // Start a new listing with the current ticket
            }
            previousTicket = ticket; // Update previous ticket
        }

        // Add the last group if it exists
        if (!currentListing.isEmpty()) {
            Listing savedListing = saveListing (currentListing);
            listings.add(savedListing);
        }
        return listings;
    }

    // Retrieve a listing by ID
    public Optional<Listing> getListingById(String listingId) {
        return listingRepository.findById(listingId);
    }

    // Retrieve listings by seller ID
    public List<Listing> getListingsBySellerId(String sellerId) {
        return listingRepository.findBySellerId(sellerId);
    }

    // Retrieve listings by event ID
    public List<Listing> getListingsByEventId(String eventId) {
        return listingRepository.findByEventId(eventId);
    }

    // Delete a listing by ID
    public void deleteListing(String listingId) {
        listingRepository.deleteById(listingId);
    }

    // Helper method to check if two tickets are consecutive
    private boolean isConsecutive(Ticket previousTicket, Ticket currentTicket) {
        int previousSeat = Integer.parseInt(previousTicket.getSeat());
        int currentSeat = Integer.parseInt(currentTicket.getSeat());

        // Check if the current seat is the next one after the previous seat
        return currentSeat == previousSeat + 1;
    }
}
