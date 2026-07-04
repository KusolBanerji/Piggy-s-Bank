package com.banking.services.security;

import com.banking.services.entity.User;
import com.banking.services.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {
    // UserDetailsService is Spring Security's interface
    // We implement it to tell Spring "here's how to find a user"

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email)
            throws UsernameNotFoundException {
        // Spring Security calls this during authentication
        // "username" in our case = email address

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "User not found with email: " + email));

        return org.springframework.security.core.userdetails.User
                .builder()
                .username(user.getEmail())
                .password(user.getPassword())       // BCrypt hash
                .authorities(List.of(
                        new SimpleGrantedAuthority("ROLE_" + user.getRole().name())
                ))
                // ROLE_CUSTOMER or ROLE_ADMIN
                // Spring Security requires "ROLE_" prefix
                .build();
    }
}