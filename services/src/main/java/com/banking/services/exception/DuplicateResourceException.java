package com.banking.services.exception;

public class DuplicateResourceException extends RuntimeException {
    // Used when email or phone already exists
    // Maps to HTTP 409 Conflict

    public DuplicateResourceException(String message) {
        super(message);
    }
}