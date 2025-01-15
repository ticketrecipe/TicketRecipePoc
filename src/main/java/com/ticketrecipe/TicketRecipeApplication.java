package com.ticketrecipe;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
public class TicketRecipeApplication {
    public static void main(String[] args) {
        SpringApplication.run(TicketRecipeApplication.class, args);
    }
}