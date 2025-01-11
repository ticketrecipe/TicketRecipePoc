package com.ticketrecipe.common;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
@Table(name = "tickets")
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private String id;

    @Column(name = "event_id", nullable = false)
    private String eventId;

    @JsonIgnore
    @Column(name = "certified_id", nullable = true, unique = true)
    private String certifiedId;

    @ManyToOne(cascade = CascadeType.PERSIST)
    @JoinColumn(name = "owner_id", referencedColumnName = "id")
    private User purchaser;

    @Column(name = "category", nullable = false)
    private String category;

    @Column(name = "seat")
    private String seat;

    @Column(name = "seatRow")
    private String row;

    @Column(name = "section")
    private String section;

    @Column(name = "entrance")
    private String entrance;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "amount", column = @Column(name = "original_price_amount")),
            @AttributeOverride(name = "currency", column = @Column(name = "original_price_currency"))
    })
    private Price price;

    @Enumerated(EnumType.STRING)
    @Column(name = "ticket_type")
    private TicketType ticketType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private TicketStatus status;

    @JsonIgnore
    @Column(name = "pdf_ok")
    private String pdfObjectKey;

    @JsonIgnore
    @Column(name = "thumbnail_ok")
    private String thumbnailObjectKey;

    @Transient
    private String thumbnailUrl;
}
