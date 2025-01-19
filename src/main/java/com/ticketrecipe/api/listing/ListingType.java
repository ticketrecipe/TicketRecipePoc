package com.ticketrecipe.api.listing;

public enum ListingType {
    PRIVATE("private"),
    PUBLIC("public");

    private final String code;

    ListingType(String code) {
        this.code = code;
    }

    @Override
    public String toString() {
        return code; // Returns "private" or "public" when toString() is called
    }

    public static ListingType fromString(String code) {
        for (ListingType type : ListingType.values()) {
            if (type.code.equalsIgnoreCase(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown code: " + code);
    }
}
