package com.ticketrecipe.api.sales;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.ticketrecipe.api.listing.Listing;
import com.ticketrecipe.common.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "private_sales")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PrivateSale {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(name = "private_buyer_email", nullable = false, length = 255)
    @Email
    private String privateBuyerEmail;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "private_buyer_id", referencedColumnName = "id")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private User privateBuyer;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @OneToMany(mappedBy = "privateListing", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<Listing> listings;
}
