package com.ticketrecipe.common;

import lombok.Data;

@Data
public class Venue {
    private String name;
    private String address;

    public Venue(String venueName, String venueAddress) {
        this.name = venueName;
        this.address = venueAddress;
    }
}