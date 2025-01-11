package com.ticketrecipe.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtDecoders;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final String issuerUri = "https://cognito-idp.us-west-2.amazonaws.com/us-west-2_EKYaoBCKQ";

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        httpSecurity
                .csrf(csrf -> csrf.disable()) // Disable CSRF for stateless APIs
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/v1/certify/tickets",
                                "/v1/getcertify/ticket/verify",
                                "/v1/getcertify/ticket/validate"
                        ).permitAll() // Publicly accessible endpoints
                        .anyRequest().authenticated() // Secure other endpoints
                )
                .oauth2ResourceServer(
                        oauth2 -> oauth2.jwt(jwtConfigurer -> jwtConfigurer.decoder(jwtDecoder()))); // Configure JWT decoder
        return httpSecurity.build();
    }

    @Bean
    public JwtDecoder jwtDecoder() {
        // Create a JwtDecoder using the Cognito issuer URI
        return JwtDecoders.fromOidcIssuerLocation(issuerUri);
    }
}