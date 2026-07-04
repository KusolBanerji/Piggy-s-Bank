package com.banking.services.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthResponse {

    private String token;           // JWT token
    private String tokenType;       // always "Bearer"
    private Long userId;
    private String email;
    private String role;
    private Long expiresIn;         // milliseconds until expiry
}