package com.banking.services.service;

import com.banking.services.dto.*;
import com.banking.services.entity.User;
import com.banking.services.exception.*;
import com.banking.services.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
//import java.util.Optional;

@Service                    // Marks this as the business logic layer
@RequiredArgsConstructor    // Lombok: generates constructor for all final fields (clean injection)
@Slf4j                      // Lombok: injects a logger → use log.info(), log.error() etc.
public class UserService {

    private final UserRepository userRepository;
    // @RequiredArgsConstructor automatically injects UserRepository here
    // This is called Constructor Injection — the preferred way in Spring

    //Code without DTOs and custom exceptions (for simplicity):
    /*

    // ─── CREATE ───────────────────────────────────────────────
    @Transactional  // If anything fails, DB changes are rolled back automatically
    public User createUser(User user) {
        log.info("Creating user with email: {}", user.getEmail());

        // Business rule: no duplicate emails
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new RuntimeException("Email already exists: " + user.getEmail());
        }

        // Business rule: no duplicate phone numbers
        if (userRepository.existsByPhone(user.getPhone())) {
            throw new RuntimeException("Phone already exists: " + user.getPhone());
        }

        User savedUser = userRepository.save(user);
        log.info("User created successfully with id: {}", savedUser.getId());
        return savedUser;
    }

    // ─── READ ALL ─────────────────────────────────────────────
    public List<User> getAllUsers() {
        log.info("Fetching all users");
        return userRepository.findAll();
    }

    // ─── READ BY ID ───────────────────────────────────────────
    public User getUserById(Long id) {
        log.info("Fetching user with id: {}", id);
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
        // Optional.orElseThrow() → return user if found, throw exception if not
    }

    // ─── UPDATE ───────────────────────────────────────────────
    @Transactional
    public User updateUser(Long id, User updatedUser) {
        log.info("Updating user with id: {}", id);

        User existingUser = getUserById(id);  // reuse above method

        // Only update allowed fields (never update email/password here)
        existingUser.setFirstName(updatedUser.getFirstName());
        existingUser.setLastName(updatedUser.getLastName());
        existingUser.setPhone(updatedUser.getPhone());

        User saved = userRepository.save(existingUser);
        log.info("User updated successfully: {}", id);
        return saved;
    }

    // ─── DELETE ───────────────────────────────────────────────
    @Transactional
    public void deleteUser(Long id) {
        log.info("Deleting user with id: {}", id);

        if (!userRepository.existsById(id)) {
            throw new RuntimeException("User not found with id: " + id);
        }

        userRepository.deleteById(id);
        log.info("User deleted successfully: {}", id);
    }
    */

    // Code with DTOs and custom exceptions (for better API design and error handling):
    @Transactional
    public UserResponse createUser(CreateUserRequest request) {
        log.info("Creating user with email: {}", request.getEmail());

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException(
                "Email already exists: " + request.getEmail());
        }
        if (userRepository.existsByPhone(request.getPhone())) {
            throw new DuplicateResourceException(
                "Phone already exists: " + request.getPhone());
        }

        User user = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .password(request.getPassword())
                .build();

        User saved = userRepository.save(user);
        log.info("User created with id: {}", saved.getId());
        return mapToResponse(saved);
    }

    public List<UserResponse> getAllUsers() {
        log.info("Fetching all users");
        return userRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public UserResponse getUserById(Long id) {
        log.info("Fetching user with id: {}", id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
        return mapToResponse(user);
    }

    @Transactional
    public UserResponse updateUser(Long id, UpdateUserRequest request) {
        log.info("Updating user with id: {}", id);

        User existing = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));

        existing.setFirstName(request.getFirstName());
        existing.setLastName(request.getLastName());
        existing.setPhone(request.getPhone());

        return mapToResponse(userRepository.save(existing));
    }

    @Transactional
    public void deleteUser(Long id) {
        log.info("Deleting user with id: {}", id);
        if (!userRepository.existsById(id)) {
            throw new UserNotFoundException(id);
        }
        userRepository.deleteById(id);
        log.info("User deleted: {}", id);
    }

    // ─── Private Mapper ───────────────────────────────────────
    private UserResponse mapToResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}