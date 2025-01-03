package com.ticketrecipe.api.listing;

import com.ticketrecipe.common.Price;
import com.ticketrecipe.common.SeatInventory;
import com.ticketrecipe.common.TicketType;
import com.ticketrecipe.common.User;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Builder
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Listing {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String listingId;

    private String eventId;

    @Enumerated(EnumType.STRING)
    private ListingStatus status;

    private TicketType ticketType;

    private String category;

    private int quantity;

    private String seatRow;

    private String section;

    // Fixed: Added @Builder.Default to use default initialization
    @Builder.Default
    @OneToMany(mappedBy = "listing", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SeatInventory> seats = new ArrayList<>();

    @OneToOne
    @JoinColumn(name = "seller_id", referencedColumnName = "id")
    private User seller;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "amount", column = @Column(name = "original_price_amount")),
            @AttributeOverride(name = "currency", column = @Column(name = "original_price_currency"))
    })
    private Price originalTicketPrice;

    @Transient
    private Price maximumSellingPrice;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "amount", column = @Column(name = "selling_price_amount")),
            @AttributeOverride(name = "currency", column = @Column(name = "selling_price_currency"))
    })
    private Price sellingPrice;

    @Transient
    public List<String> getConsecutiveSeats() {
        List<String> consecutiveSeatsList = new ArrayList<>();

        // Iterate through the list of seats and add their seat identifiers
        for (SeatInventory seatInventory : seats) {
            consecutiveSeatsList.add(seatInventory.getSeat());
        }
        return consecutiveSeatsList;
    }
}