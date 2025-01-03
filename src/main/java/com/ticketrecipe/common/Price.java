package com.ticketrecipe.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Price {
    private double amount;    // The price amount
    private String currency;  // The currency code (e.g., SGD, USD)
}