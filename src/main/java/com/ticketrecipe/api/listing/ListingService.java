package com.ticketrecipe.api.listing;

import com.ticketrecipe.api.sales.PrivateSale;
import com.ticketrecipe.api.ticket.TicketRepository;
import com.ticketrecipe.api.user.UserRepository;
import com.ticketrecipe.common.Price;
import com.ticketrecipe.common.User;
import com.ticketrecipe.common.listing.InventoryStatus;
import com.ticketrecipe.common.listing.ListingInventory;
import com.ticketrecipe.common.Ticket;
import com.ticketrecipe.common.TicketType;
import com.ticketrecipe.getcertify.registry.GetCertifyRegistryService;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import static com.ticketrecipe.api.listing.ListingConfirmation.ListingDetail;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class ListingService {

    @Autowired
    private GetCertifyRegistryService gCertifyRegistryService;
    @Autowired
    private ListingRepository listingRepository;
    @Autowired
    private TicketRepository ticketRepository;
    @Autowired
    private UserRepository userRepository;

    public Listing createListing(List<Ticket> tickets) {
        Ticket firstTicket = tickets.get(0);
        Listing listing = Listing.builder()
                .event(firstTicket.getEvent())
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
            if (TicketType.RS.equals(ticket.getTicketType())){
                seats.add(ListingInventory.builder()
                        .seat(ticket.getSeat())
                        .row(ticket.getRow())
                        .section(ticket.getSection())
                        .listing(listing)
                        .ticket(ticket)
                        .status(InventoryStatus.AVAILABLE)
                        .build());
            }
        }
        listing.setInventories(seats);
        Price originalPrice = firstTicket.getPrice();
        listing.setMaximumSellingPrice(new Price(originalPrice.getAmount() * 3, originalPrice.getCurrency()));
        return listingRepository.save(listing);
    }

    @Transactional
    public List<Listing> create(String userId, String eventId, List<String> ticketIds) {
        List<Listing> listings = new ArrayList<>();

        // Fetch tickets owned by the user for the given event and ticket IDs
        List<Ticket> tickets = ticketRepository.findByPurchaserIdAndEventIdAndIdIn(userId, eventId, ticketIds);
        if (tickets.isEmpty()) {
            throw new IllegalArgumentException("No valid tickets found for the provided userId, eventId, and ticketIds.");
        }

        // Sort the tickets by section, row, and seat number
        List<Ticket> sortedTickets = tickets.stream()
                .sorted((t1, t2) -> {
                    int sectionCompare = t1.getSection().compareTo(t2.getSection());
                    if (sectionCompare != 0) return sectionCompare;

                    int rowCompare = t1.getRow().compareTo(t2.getRow());
                    if (rowCompare != 0) return rowCompare;

                    // Comparing seat numbers as integers if possible
                    return Integer.compare(
                            Integer.parseInt(t1.getSeat()),
                            Integer.parseInt(t2.getSeat())
                    );
                })
                .toList();

        List<Ticket> currentListing = new ArrayList<>();
        Ticket previousTicket = null;

        // Loop through sorted tickets and group them into listings
        for (Ticket ticket : sortedTickets) {
            if (previousTicket == null || isConsecutive(previousTicket, ticket)) {
                currentListing.add(ticket); // Add to the current group if consecutive
            } else {
                // Save the previous group as a listing and start a new one
                Listing savedListing = createListing(currentListing);
                listings.add(savedListing);

                currentListing = new ArrayList<>();
                currentListing.add(ticket); // Start a new listing with the current ticket
            }
            previousTicket = ticket; // Update previous ticket
        }

        // Add the last group if it exists
        if (!currentListing.isEmpty()) {
            Listing savedListing = createListing(currentListing);
            listings.add(savedListing);
        }
        return listings;
    }

    @Transactional
    public List<Listing> publish(ListingConfirmation confirmation) {
        List<String> listingIds = confirmation.getListings().stream()
                .map(ListingDetail::listingId)
                .toList();

        List<Listing> listings = listingRepository.findAllById(listingIds);

        // Validate if any listings are missing
        if (listings.size() != listingIds.size()) {
            throw new IllegalArgumentException("Some listings were not found.");
        }

        ListingStatus status;
        PrivateSale privateSale = null;

        if (ListingType.PRIVATE.equals(confirmation.getListingType())) {
            // Check if private buyer is an existing user
            Optional<User> privateBuyer = userRepository.findByEmailAddress(confirmation.getPrivateBuyerEmail());

            // Set listing status based on private buyer's registration status
            status = privateBuyer.isPresent() ? ListingStatus.RESERVED : ListingStatus.PENDING;

            // Create private listing object
            privateSale = PrivateSale.builder()
                    .privateBuyerEmail(confirmation.getPrivateBuyerEmail())
                    .privateBuyer(privateBuyer.orElse(null)) // Pass null if buyer isn't registered
                    .expiresAt(LocalDateTime.now().plusDays(7)) // Example expiration
                    .build();
        } else {
            status = ListingStatus.AVAILABLE;
        }

        ListingStatus finalStatus = status;
        PrivateSale finalPrivateSale = privateSale;

        listings.forEach(listing -> {
            ListingDetail dto = confirmation.getListings().stream()
                    .filter(l -> l.listingId().equals(listing.getId()))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Listing detail not found for listing ID: " + listing.getId()));

            // Update price and status
            listing.setSellingPrice(dto.sellingPrice());
            listing.setStatus(finalStatus);

            // Link private listing if applicable
            if (finalPrivateSale != null) {
                listing.setType(ListingType.PRIVATE);
                listing.setPrivateListing(finalPrivateSale);
            }
        });

        listings = listingRepository.saveAll(listings);

        // Prepare certified tickets in registry to lock in
        List<String> ticketIds = listings.stream()
                .flatMap(listing -> listing.getInventories().stream())
                .map(inventory -> inventory.getTicket().getCertifyId())
                .toList();

        // Lock all tickets in a batch
        boolean locked = gCertifyRegistryService.lockTickets(ticketIds);
        if (!locked) {
            throw new IllegalStateException("Failed to lock one or more tickets.");
        }
        return listings;
    }

    private boolean isConsecutive(Ticket previousTicket, Ticket currentTicket) {
        int previousSeat = Integer.parseInt(previousTicket.getSeat());
        int currentSeat = Integer.parseInt(currentTicket.getSeat());

        // Check if the current seat is the next one after the previous seat
        return currentSeat == previousSeat + 1;
    }
}