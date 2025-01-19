package com.ticketrecipe.common;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "events")
public class Event {
    @Id
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

    @CreationTimestamp
    @Column(nullable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime lastUpdatedDate;
}
