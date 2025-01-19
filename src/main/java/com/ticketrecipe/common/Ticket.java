package com.ticketrecipe.common;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

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
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    @JsonIgnore
    private Event event;

    @JsonIgnore
    @Column(name = "certify_id", nullable = true, unique = true)
    private String certifyId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", referencedColumnName = "id")
    @JsonIgnore
    private User purchaser;

    @Column(name = "printed_name", nullable = false)
    private String printedName;

    @Column(name = "category", nullable = false)
    private String category;

    @Column(name = "seat", nullable = false)
    private String seat;

    @Column(name = "seatRow", nullable = false)
    private String row;

    @Column(name = "section", nullable = false)
    private String section;

    @Column(name = "entrance", nullable = false)
    private String entrance;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "amount", column = @Column(name = "original_price_amount")),
            @AttributeOverride(name = "currency", column = @Column(name = "original_price_currency"))
    })
    private Price price;

    @Enumerated(EnumType.STRING)
    @Column(name = "ticket_type",  nullable = false)
    private TicketType ticketType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private TicketStatus status;

    @JsonIgnore
    @Column(name = "pdf_ok", nullable = false)
    private String pdfObjectKey;

    @JsonIgnore
    @Column(name = "thumbnail_ok", nullable = false)
    private String thumbnailObjectKey;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Transient
    private String thumbnailUrl;

    @Lob
    @Column(name = "gc_qr_code", columnDefinition = "CLOB")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String getCertifyQrCode;

    @CreationTimestamp
    @Column(nullable = false)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private LocalDateTime lastUpdatedDate;
}
