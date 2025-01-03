package com.ticketrecipe.common;

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
    @Column(name = "ticket_id")
    private String id;

    @Column(name = "event_id", nullable = false)
    private String eventId;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "owner_id", referencedColumnName = "id")
    private User purchaser;       // The name of the ticket holder

    @Column(name = "category", nullable = false)
    private String category;   // Ticket category (e.g., CAT2, VIP)

    @Column(name = "seat")
    private String seat;       // Seat number

    @Column(name = "seatRow")
    private String row;        // Row number

    @Column(name = "section")
    private String section;    // Section number

    @Column(name = "entrance")
    private String entrance;   // Entrance or gate information

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "amount", column = @Column(name = "original_price_amount")),
            @AttributeOverride(name = "currency", column = @Column(name = "original_price_currency"))
    })
    private Price price;       // Price information (amount and currency)

    @Enumerated(EnumType.STRING)
    @Column(name = "ticket_type")
    private TicketType ticketType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private TicketStatus status;

    // The S3 object key to access the ticket's full PDF
    @Column(name = "pdf_s3_object_key")
    private String pdfS3ObjectKey;

    @Transient
    private String thumbnailUrl;
}