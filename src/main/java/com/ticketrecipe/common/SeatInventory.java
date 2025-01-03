package com.ticketrecipe.common;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.ticketrecipe.api.listing.Listing;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SeatInventory {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    private String seat;

    @Column(name = "seatRow")
    private String row;
    private String section;

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnore
    @JoinColumn(name = "listing_id", nullable = false)
    private Listing listing;
}