package com.banking.services.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    // OncePerRequestFilter → guaranteed to run exactly ONCE per request

    private final JwtTokenProvider jwtTokenProvider;
    private final UserDetailsServiceImpl userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        // Step 1: Extract token from Authorization header
        String token = extractTokenFromRequest(request);

        // Step 2: Validate token
        if (StringUtils.hasText(token) && jwtTokenProvider.validateToken(token)) {

            // Step 3: Get user email from token
            String email = jwtTokenProvider.getEmailFromToken(token);

            // Step 4: Load user details from DB
            UserDetails userDetails = userDetailsService.loadUserByUsername(email);

            // Step 5: Create authentication object
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,                           // credentials (not needed after auth)
                            userDetails.getAuthorities()    // roles
                    );
            authentication.setDetails(
                    new WebAuthenticationDetailsSource().buildDetails(request));

            // Step 6: Set authentication in Security Context
            // This tells Spring Security: "this request is authenticated"
            SecurityContextHolder.getContext().setAuthentication(authentication);
            log.debug("Authentication set for user: {}", email);
        }

        // Step 7: Continue to next filter / controller
        filterChain.doFilter(request, response);
    }

    private String extractTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        // Authorization: Bearer eyJhbGc...

        if (StringUtils.hasText(bearerToken)
                && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);    // remove "Bearer " prefix
        }
        return null;
    }
}