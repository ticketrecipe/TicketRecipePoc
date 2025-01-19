package com.ticketrecipe.common;

public enum TicketType {
    GA("GENERAL_ADMISSION"),
    RS("RESERVED_SEATING");

    private final String desc;

    TicketType(String desc) {
        this.desc = desc;
    }

    @Override
    public String toString() {
        return desc;
    }

    public static TicketType fromString(String desc) {
        for (TicketType type : TicketType.values()) {
            if (type.desc.equals(desc)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown code: " + desc);
    }
}

