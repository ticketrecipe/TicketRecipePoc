package com.ticketrecipe.common;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "events")
public class Event {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "start_date_time", nullable = false)
    private String startDateTime;

    @Column(name = "issuer", nullable = false)
    private String issuer;

    @Column(name = "venue_name", nullable = false)
    private String venueName;

    @Column(name = "address", nullable = false)
    private String address;

    @Column(name = "banner_img_url", nullable = false)
    private String bannerImageUrl;

    @Column(name = "small_img_url", nullable = false)
    private String smallImageUrl;
}
