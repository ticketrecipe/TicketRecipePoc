package com.ticketrecipe.common;

public enum TicketType {
    GENERAL_ADMISSION("GA"),
    RESERVED_SEATING("RS");

    private final String code;

    TicketType(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
