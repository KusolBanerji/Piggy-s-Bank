package com.banking.services.controller;

//import com.banking.services.entity.User;
import com.banking.services.dto.*;
import com.banking.services.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController                 // REST API controller — returns JSON
@RequestMapping("/api/users")   // All endpoints in this class start with /api/users
@RequiredArgsConstructor        // Constructor injection for UserService
@Slf4j
public class UserController {

    private final UserService userService;

    // Code without DTOs and custom exceptions (for simplicity):
    /*

    // ─── POST /api/users ──────────────────────────────────────
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)     // Returns HTTP 201 instead of 200
    public ResponseEntity<User> createUser(@Valid @RequestBody User user) {
        // @Valid        → triggers validation annotations on User fields
        // @RequestBody  → converts incoming JSON to User object
        log.info("POST /api/users");
        User created = userService.createUser(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    // ─── GET /api/users ───────────────────────────────────────
    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        log.info("GET /api/users");
        return ResponseEntity.ok(userService.getAllUsers());
        // ResponseEntity.ok() → wraps response with HTTP 200
    }

    // ─── GET /api/users/{id} ──────────────────────────────────
    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        // @PathVariable → extracts {id} from the URL
        log.info("GET /api/users/{}", id);
        return ResponseEntity.ok(userService.getUserById(id));
    }

    // ─── PUT /api/users/{id} ──────────────────────────────────
    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody User user) {
        log.info("PUT /api/users/{}", id);
        return ResponseEntity.ok(userService.updateUser(id, user));
    }

    // ─── DELETE /api/users/{id} ───────────────────────────────
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        log.info("DELETE /api/users/{}", id);
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
        // HTTP 204 No Content — success but nothing to return
    }
    */
    
    // Code with DTOs and custom exceptions will be added here later for better separation of concerns and security (e.g. not exposing password field).
    @PostMapping
    public ResponseEntity<UserResponse> createUser(
            @Valid @RequestBody CreateUserRequest request) {
        log.info("POST /api/users");
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(userService.createUser(request));
    }

    @GetMapping
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        log.info("GET /api/users");
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long id) {
        log.info("GET /api/users/{}", id);
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserRequest request) {
        log.info("PUT /api/users/{}", id);
        return ResponseEntity.ok(userService.updateUser(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        log.info("DELETE /api/users/{}", id);
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}