package com.ticketrecipe.common.auth;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

public class CustomAuthenticationToken extends AbstractAuthenticationToken {

    private final CustomUserDetails principal;

    public CustomAuthenticationToken(CustomUserDetails principal, Collection<? extends GrantedAuthority> authorities) {
        super(authorities);
        this.principal = principal;
        setAuthenticated(true); // Indicate that this token is authenticated
    }

    @Override
    public Object getCredentials() {
        return null; // You can provide credentials if needed, but typically JWT-based tokens don't require this
    }

    @Override
    public Object getPrincipal() {
        return principal;
    }
}

