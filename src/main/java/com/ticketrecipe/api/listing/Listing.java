package com.ticketrecipe.api.listing;

import com.ticketrecipe.common.*;
import com.ticketrecipe.common.listing.ListingType;
import jakarta.persistence.*;
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
    private String id;

    @ManyToOne
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @Enumerated(EnumType.STRING)
    private ListingStatus status;

    @Enumerated(EnumType.STRING)
    private ListingType type;

    private TicketType ticketType;

    private String category;

    private int quantity;

    private String seatRow;

    private String section;

    @Builder.Default
    @OneToMany(mappedBy = "listing", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ListingInventory> inventories = new ArrayList<>();

    @ManyToOne
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
        for (ListingInventory seatInventory : inventories) {
            consecutiveSeatsList.add(seatInventory.getSeat());
        }
        return consecutiveSeatsList;
    }
}