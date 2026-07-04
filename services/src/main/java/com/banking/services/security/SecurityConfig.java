package com.banking.services.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.*;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity          // enables Spring Security for this app
@EnableMethodSecurity       // enables @PreAuthorize on methods
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final UserDetailsServiceImpl userDetailsService;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http)
            throws Exception {

        http
            // Disable CSRF — not needed for stateless REST APIs
            // CSRF protects against browser-based attacks
            // JWT APIs are not vulnerable to CSRF ✅
            .csrf(AbstractHttpConfigurer::disable)

            // Stateless session — no server-side session storage
            // Every request must carry its own JWT token
            .sessionManagement(session -> session
                    .sessionCreationPolicy(SessionCreationPolicy.STATELESS))

            // Define which endpoints are public vs protected
            .authorizeHttpRequests(auth -> auth

                // Public endpoints — no token needed
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/h2-console/**").permitAll()   // dev only!
                .requestMatchers("/actuator/**").permitAll()

                // Admin only endpoints
                .requestMatchers(HttpMethod.GET,
                        "/api/users").hasRole("ADMIN")

                // Authenticated users (any role)
                .anyRequest().authenticated()
            )

            // Add our JWT filter BEFORE Spring's default auth filter
            .addFilterBefore(jwtAuthenticationFilter,
                    UsernamePasswordAuthenticationFilter.class)

            // H2 console needs this (uses iframes)
            .headers(headers -> headers
                    .frameOptions(frame -> frame.sameOrigin()));

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
        // BCrypt with default strength 10
        // strength = number of hashing rounds (2^10 = 1024)
        // higher = slower to hash = harder to brute force
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
        // Spring's authentication manager — handles login verification
        // Uses UserDetailsService + PasswordEncoder automatically
    }
}