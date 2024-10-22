package com.example.SeniorProject.Configuration;

import org.springframework.boot.test.context.*;
import org.springframework.context.annotation.*;
import org.springframework.security.config.annotation.web.builders.*;
import org.springframework.security.config.annotation.web.configuration.*;
import org.springframework.security.config.annotation.web.configurers.*;
import org.springframework.security.web.*;

@TestConfiguration
@EnableWebSecurity
public class TestSecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception
    {
        // Disable security for testing
        http.csrf(AbstractHttpConfigurer::disable)  // New way to disable CSRF
                .authorizeHttpRequests(authorization -> authorization.anyRequest().permitAll());  // Allow all requests without authentication
        return http.build();
    }
}
