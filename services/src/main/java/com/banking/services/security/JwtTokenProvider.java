package com.banking.services.security;

import com.banking.services.entity.User;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
@Slf4j
public class JwtTokenProvider {

    @Value("${jwt.secret}")
    private String jwtSecret;          // reads from config repo

    @Value("${jwt.expiration}")
    private Long jwtExpiration;        // reads from config repo

    // ─── GENERATE TOKEN ───────────────────────────────────────
    public String generateToken(User user) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpiration);

        return Jwts.builder()
                .subject(user.getEmail())           // who this token is for
                .claim("userId", user.getId())       // custom claim
                .claim("role", user.getRole().name()) // custom claim
                .claim("firstName", user.getFirstName())
                .issuedAt(now)                      // when token was created
                .expiration(expiryDate)             // when token expires
                .signWith(getSigningKey())           // sign with secret
                .compact();                         // build the token string
    }

    // ─── VALIDATE TOKEN ───────────────────────────────────────
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token);
            return true;    // parsing succeeded → token is valid
        } catch (ExpiredJwtException e) {
            log.error("JWT token expired: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            log.error("JWT token malformed: {}", e.getMessage());
        } catch (SecurityException e) {
            log.error("JWT signature invalid: {}", e.getMessage());
        } catch (Exception e) {
            log.error("JWT validation error: {}", e.getMessage());
        }
        return false;
    }

    // ─── EXTRACT CLAIMS ───────────────────────────────────────
    public String getEmailFromToken(String token) {
        return getClaims(token).getSubject();
    }

    public Long getUserIdFromToken(String token) {
        return getClaims(token).get("userId", Long.class);
    }

    public String getRoleFromToken(String token) {
        return getClaims(token).get("role", String.class);
    }

    // ─── Private Helpers ──────────────────────────────────────
    private Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtSecret);
        return Keys.hmacShaKeyFor(keyBytes);
        // converts hex secret → cryptographic signing key
    }

    public Long getExpirationMs() {
        return jwtExpiration;
    }
}