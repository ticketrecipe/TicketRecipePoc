package com.ticketrecipe.api.sales;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PrivateSaleRepository extends JpaRepository<PrivateSale, String> {

    List<PrivateSale> findByPrivateBuyerEmail(String emailAddress);
}
