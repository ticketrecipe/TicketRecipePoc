package com.ticketrecipe.api.listing;

import com.ticketrecipe.common.Price;
import com.ticketrecipe.common.ListingInventory;
import com.ticketrecipe.common.Ticket;
import com.ticketrecipe.common.TicketType;
import com.ticketrecipe.common.listing.ConfirmListingDto;
import com.ticketrecipe.getcertify.registry.GetCertifyRegistryService;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ListingService {
    @Autowired
    private GetCertifyRegistryService gCertifyRegistryService;
    @Autowired
    private ListingRepository listingRepository;

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

        List<ListingInventory> seats = new ArrayList<ListingInventory>();
        for (Ticket ticket : tickets) {
            if (TicketType.RESERVED_SEATING.equals(ticket.getTicketType())){
                seats.add(ListingInventory.builder()
                        .seat(ticket.getSeat())
                        .row(ticket.getRow())
                        .section(ticket.getSection())
                        .listing(listing)
                        .ticket(ticket)
                        .build());
            }
        }
        listing.setInventories(seats);
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

    @Transactional
    public List<Listing> confirmListings(List<ConfirmListingDto> listings) {
        // Step 1: Retrieve all listings in a single query
        List<String> listingIds = listings.stream()
                .map(ConfirmListingDto::listingId)
                .toList();

        List<Listing> fetchedListings = listingRepository.findAllById(listingIds);

        // Step 2: Validate if any listings are missing
        if (fetchedListings.size() != listingIds.size()) {
            throw new IllegalArgumentException("Some listings were not found.");
        }

        // Step 3: Prepare certified tickets in registry to lock in
        List<String> ticketIds = fetchedListings.stream()
                .flatMap(listing -> listing.getInventories().stream())
                .map(inventory -> inventory.getTicket().getCertifiedId())
                .toList();

        // Lock all tickets in a batch
        boolean locked = gCertifyRegistryService.lockTickets(ticketIds);
        if (!locked) {
            throw new IllegalStateException("Failed to lock one or more tickets.");
        }

        // Step 4: Update all listings and their statuses
        Map<String, ConfirmListingDto> dtoMap = listings.stream()
                .collect(Collectors.toMap(ConfirmListingDto::listingId, dto -> dto));

        fetchedListings.forEach(listing -> {
            ConfirmListingDto dto = dtoMap.get(listing.getId().toString());

            // Update price and status
            listing.setSellingPrice(dto.sellingPrice());
            listing.setStatus(ListingStatus.ACTIVE);
        });

        // Step 5: Save all listings in batch
        return listingRepository.saveAll(fetchedListings);
    }
}