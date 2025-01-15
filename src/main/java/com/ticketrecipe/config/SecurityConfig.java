package com.ticketrecipe.config;

import com.ticketrecipe.common.auth.JwtAuthenticationConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtDecoders;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final String issuerUri = "https://cognito-idp.us-west-2.amazonaws.com/us-west-2_EKYaoBCKQ";

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        httpSecurity
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(AntPathRequestMatcher.antMatcher("/h2-console/**")).permitAll()
                        .requestMatchers(
                                "/v1/certify/tickets",
                                "/v1/getcertify/ticket/verify",
                                "/v1/getcertify/ticket/validate"
                        ).permitAll()
                        .anyRequest().authenticated()
                )
                .headers(headers -> headers
                        .frameOptions(frame -> frame.sameOrigin())  // Allow H2 console to load in iframe from the same origin
                        .httpStrictTransportSecurity(hsts -> hsts.disable())  // Disable HTTP Strict Transport Security (HSTS) for the console
                        .contentSecurityPolicy(csp -> csp.policyDirectives("frame-ancestors 'self'"))  // Allow iframe embedding from same origin
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwtConfigurer -> jwtConfigurer
                                .decoder(jwtDecoder())
                                .jwtAuthenticationConverter(new JwtAuthenticationConverter(
                                ))
                        )
                );
        return httpSecurity.build();
    }

    @Bean
    public JwtDecoder jwtDecoder() {
        // Create a JwtDecoder using the Cognito issuer URI
        return JwtDecoders.fromOidcIssuerLocation(issuerUri);
    }
}