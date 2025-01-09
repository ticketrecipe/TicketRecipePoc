package com.ticketrecipe.getcertify;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
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

    @Column(name = "event_id", nullable = false)
    private String eventId;

    @Column(name = "event_name", nullable = false)
    private String eventName;

    @Column(name = "start_date_time", nullable = false)
    private String startDateTime;

    @Column(name = "issuer", nullable = false)
    private String issuer;

    @Embedded
    private Venue venue;

    @Data
    @Embeddable
    @NoArgsConstructor
    public static class Venue {
        private String name;
        private String address;

        public Venue(String name, String address) {
            this.name = name;
            this.address=address;
        }
    }

    @Embedded
    private Price price;

    @Data
    @Embeddable
    @NoArgsConstructor
    public static class Price {
        private Double amount;
        private String currency;

        public Price(double amount, String currency) {
            this.amount = amount;
            this.currency = currency;
        }
    }

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
