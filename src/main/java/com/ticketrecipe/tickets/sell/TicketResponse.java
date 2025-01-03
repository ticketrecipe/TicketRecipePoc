package com.ticketrecipe.tickets.sell;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.ticketrecipe.common.Price;
import com.ticketrecipe.common.TicketStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TicketResponse {

    // The object key to access the ticket's full PDF in S3 (excluded from JSON response)
    @JsonIgnore
    private String s3ObjectKey;

    // The pre-signed URL to access the ticket's thumbnail image in S3
    private String thumbnailUrl;

    // Detailed information about the ticket (e.g., name, seat info, price)
    private TicketInfo ticketInfo;

    // Status of the ticket verification and eligibility
    private TicketStatus status;

    // Derived method to determine if the ticket is sellable
    public boolean isSellable() {
        return status == TicketStatus.GC_VERIFIED;
    }

    private int index;

    // Nested class to represent ticket information
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TicketInfo {
        private String name;       // The name of the ticket holder
        private String category;   // Ticket category (e.g., VIP, GA)
        private String seat;       // Seat number
        private String row;        // Row number
        private String section;    // Section number
        private String entrance;   // Entrance or gate information
        private Price price;       // Price information (amount and currency)

        // Determine ticket type based on seat and row information
        public String getType() {
            return StringUtils.isAllBlank(seat, row) ? "GA" : "RS";
        }
    }
}
