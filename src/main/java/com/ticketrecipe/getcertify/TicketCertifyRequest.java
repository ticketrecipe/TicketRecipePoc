package com.ticketrecipe.getcertify;

import com.ticketrecipe.common.Price;
import lombok.Data;

import java.util.List;

@Data
public class TicketCertifyRequest {

    private String requestId;
    private String eventId;
    private String eventName;
    private String startDateTime;
    private Purchaser purchaser;
    private Venue venue;
    private List<Ticket> tickets;

    @Data
    public static class Purchaser {
        private String name;
        private String emailAddress;
    }

    @Data
    public static class Venue {
        private String name;
        private String address;
    }

    @Data
    public static class Ticket {
        private Price price;
        private String barcodeId;
        private String entrance;
        private String category;
        private String row;
        private String seat;
        private String section;
    }
}
