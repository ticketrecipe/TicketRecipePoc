package com.ticketrecipe.common.auth;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.oauth2.jwt.Jwt;

public class JwtAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        // Extract custom details from JWT
        String username = jwt.getClaimAsString("sub"); // Replace with your username claim
        String email = jwt.getClaimAsString("email");  // Replace with your email claim

        // Create custom principal
        CustomUserDetails principal = new CustomUserDetails(username, email, null);

        // Return a CustomAuthenticationToken with the custom principal
        return new CustomAuthenticationToken(principal, null);
    }
}