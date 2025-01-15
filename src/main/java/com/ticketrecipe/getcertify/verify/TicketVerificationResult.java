package com.ticketrecipe.getcertify.verify;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.ticketrecipe.common.Event;
import com.ticketrecipe.common.Price;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TicketVerificationResult {
    private String id;

    @JsonIgnore
    private String purchaserEmailAddress;

    @JsonIgnore
    private String refId;

    private Event event;
    private String purchaserName;
    private String category;
    private String type;
    private String section;
    private String row;
    private String seat;
    private Price price;

}