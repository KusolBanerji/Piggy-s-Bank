package com.banking.services.service;

import com.banking.services.dto.*;
import com.banking.services.entity.Role;
import com.banking.services.entity.User;
import com.banking.services.exception.DuplicateResourceException;
import com.banking.services.repository.UserRepository;
import com.banking.services.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;      // BCrypt
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;

    // ─── REGISTER ─────────────────────────────────────────────
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        log.info("Registering new user: {}", request.getEmail());

        // Check duplicates
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException(
                    "Email already exists: " + request.getEmail());
        }
        if (userRepository.existsByPhone(request.getPhone())) {
            throw new DuplicateResourceException(
                    "Phone already exists: " + request.getPhone());
        }

        // Build user — hash password with BCrypt
        User user = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .password(passwordEncoder.encode(request.getPassword()))
                // "secret123" → "$2a$10$N9qo8uLOickgx2ZMRZoMye..."
                .role(Role.CUSTOMER)    // all new registrations = CUSTOMER
                .build();

        User saved = userRepository.save(user);

        // Generate JWT token immediately after registration
        String token = jwtTokenProvider.generateToken(saved);
        log.info("User registered successfully: {}", saved.getEmail());

        return buildAuthResponse(saved, token);
    }

    // ─── LOGIN ────────────────────────────────────────────────
    public AuthResponse login(LoginRequest request) {
        log.info("Login attempt for: {}", request.getEmail());

        // Spring Security handles verification:
        //   1. Calls UserDetailsService.loadUserByUsername(email)
        //   2. Compares BCrypt hash of provided password with stored hash
        //   3. Throws exception if mismatch
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        // Authentication succeeded — get user from DB
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow();

        // Generate JWT token
        String token = jwtTokenProvider.generateToken(user);
        log.info("Login successful for: {}", request.getEmail());

        return buildAuthResponse(user, token);
    }

    // ─── Private Helper ───────────────────────────────────────
    private AuthResponse buildAuthResponse(User user, String token) {
        return AuthResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .userId(user.getId())
                .email(user.getEmail())
                .role(user.getRole().name())
                .expiresIn(jwtTokenProvider.getExpirationMs())
                .build();
    }
}