package com.ticketrecipe.api.listing;

import com.ticketrecipe.common.Price;
import jakarta.validation.constraints.Email;
import lombok.Data;
import java.util.List;

@Data
public class ListingConfirmation {
    private ListingType listingType;
    @Email
    private String privateBuyerEmail;
    private List<ListingDetail> listings;

    record ListingDetail(
        String listingId,
        Price sellingPrice
    ){}
}
