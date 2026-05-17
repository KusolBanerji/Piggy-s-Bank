package com.banking.services.exception;

public class UserNotFoundException extends RuntimeException {
    // Custom exception specifically for "user not found" cases
    // Extends RuntimeException so we don't have to declare it everywhere

    public UserNotFoundException(Long id) {
        super("User not found with id: " + id);
    }

    public UserNotFoundException(String message) {
        super(message);
    }
}