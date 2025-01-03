package com.ticketrecipe.common.listing;

import com.ticketrecipe.common.Price;

public record ConfirmedListing(
        String listingId,
        Price sellingPrice
) {}
