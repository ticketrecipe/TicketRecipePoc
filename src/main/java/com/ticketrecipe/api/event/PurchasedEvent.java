package com.ticketrecipe.api.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PurchasedEvent {
    private String eventId;
    private String name;
    private String venue;
    private String date;
    private String location;
    private String eventBannerImageUrl;
    private String eventSmallImageUrl;
    private long ticketQuantity;
}
