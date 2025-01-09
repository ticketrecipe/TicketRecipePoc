package com.ticketrecipe.getcertify.verify;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.ticketrecipe.common.Price;
import com.ticketrecipe.common.Venue;
import lombok.Data;

@Data
public class TicketVerificationResult {
    private String id;
    @JsonIgnore
    private String purchaserEmailAddress;
    private String eventId;
    private String eventName;
    private String startDateTime;
    private String issuer;
    private Venue venue;
    private String purchaserName;
    private String category;
    private String type;
    private String section;
    private String row;
    private String seat;
    private Price price;

    public TicketVerificationResult(String id, String purchaserEmailAddress, String eventId, String eventName, String startDateTime, String issuer, String venueName, String venueAddress,
                                    String purchaserName, String category, String type, String row, String seat, String section, double priceAmount, String priceCurrency) {
        this.id = id;
        this.purchaserEmailAddress = purchaserEmailAddress;
        this.eventId = eventId;
        this.eventName = eventName;
        this.price = new Price (priceAmount, priceCurrency);
        this.purchaserName = purchaserName;
        this.category = category;
        this.type = type;
        this.row = row;
        this.seat = seat;
        this.section = section;
        this.startDateTime = startDateTime;
        this.venue = new Venue(venueName, venueAddress);
        this.issuer = issuer;
    }
}