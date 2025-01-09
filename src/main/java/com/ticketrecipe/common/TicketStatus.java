package com.ticketrecipe.common;

// Enum to define ticket statuses
public enum TicketStatus {
    GC_VERIFIED,        // Verified by GrabCertify
    INVALID,            // Invalid ticket
    NOT_VERIFIABLE,     // Cannot be verified (but allowed to sell)
    LOCKED,
    EXPIRED,
    EXISTING
}