package com.ticketrecipe.api.sales;

import com.ticketrecipe.api.listing.Listing;
import com.ticketrecipe.api.listing.ListingStatus;
import com.ticketrecipe.api.user.UserRepository;
import com.ticketrecipe.common.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Slf4j
@Service
public class PrivateSaleService {

    @Autowired
    private PrivateSaleRepository privateSaleRepository;

    @Autowired
    private UserRepository userRepository;

    @Transactional
    public List<PrivateSale> linkPrivateBuyer(User user) {

        // Fetch private sales listings associated with the email address
        List<PrivateSale> privateSales = privateSaleRepository.findByPrivateBuyerEmail(user.getEmailAddress()
        );

        // If no private sales are found for the email, simply return an empty list (no exception)
        if (privateSales.isEmpty()) {
            log.info("No private listings found for user: {}", user.getEmailAddress());
            return privateSales; // Return empty list instead of throwing an exception
        }

        // Link the new user to each of the found private sales listings and update status
        for (PrivateSale sale : privateSales) {
            sale.setPrivateBuyer(user);

            // Update status of all listings related to this private sale
            for (Listing listing : sale.getListings()) {
                listing.setStatus(ListingStatus.RESERVED);
            }
        }
        return privateSaleRepository.saveAll(privateSales);
    }
}