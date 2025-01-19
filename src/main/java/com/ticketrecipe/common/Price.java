package com.ticketrecipe.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Price {

    private double amount;
    private String currency;

    public static Optional<Price> from(String priceString) {
        if (priceString == null || priceString.isEmpty()) {
            return Optional.empty();
        }

        priceString = priceString.trim();
        double amount = 0.0;
        String currency = null;

        String regex = "(SGD|MYR|USD|\\$)?\\s*([\\d,]+(?:\\.\\d{1,2})?)\\s*(SGD|MYR|USD)?";
        Matcher matcher = Pattern.compile(regex).matcher(priceString);

        if (matcher.find()) {
            String symbol = matcher.group(1);
            currency = symbol != null && symbol.equals("$") ? "SGD" : symbol;
            if (matcher.group(3) != null) {
                currency = matcher.group(3);
            }

            String amountStr = matcher.group(2);
            if (amountStr != null) {
                amount = Double.parseDouble(amountStr.replace(",", ""));
            }
        }

        return (amount > 0.0 && currency != null) ?
                Optional.of(new Price(amount, currency)) :
                Optional.empty();
    }
}
