package com.ticketrecipe.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        // Allow CORS for all paths
        registry.addMapping("/**")
                .allowedOrigins("http://localhost:3000","https://main.d3shnslwbw8wup.amplifyapp.com/")  // Allow localhost:3000 for development
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "HEAD")  // Allow OPTIONS (for preflight)
                .allowedHeaders("*")  // Allow all headers
                .allowCredentials(true)  // Allow credentials (cookies, authorization headers)
                .maxAge(3600);  // Max age for the CORS preflight request (in seconds)
    }
}
