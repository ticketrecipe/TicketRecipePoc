package com.ticketrecipe.common.listing;

import com.ticketrecipe.common.Price;

public record ConfirmListingDto(
        String listingId,
        Price sellingPrice
) {}
