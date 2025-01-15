package com.ticketrecipe.getcertify;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.ticketrecipe.common.Event;
import com.ticketrecipe.common.Price;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@Table(name = "certified_tickets")
public class CertifiedTicket {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @JsonIgnore
    @Column(name = "ref_id", nullable = false, unique = true)
    private String referenceId;

    private boolean locked; // Add a boolean field for lock status

    @Column(name = "aes_key", nullable = false, unique = false)
    private String aesKey;

    @ManyToOne(cascade = {CascadeType.PERSIST})
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @Embedded
    private Price price;

    @Column(name = "entrance")
    private String entrance;

    @Column(name = "category")
    private String category;

    @Column(name = "seat_row")
    private String row;

    @Column(name = "seat")
    private String seat;

    @Column(name = "section")
    private String section;

    @Column(name = "type")
    private String type;
}
