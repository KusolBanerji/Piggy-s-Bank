package com.banking.transactionservice.exception;

public class DuplicateTransactionException extends RuntimeException {
    public DuplicateTransactionException(String idempotencyKey) {
        super("Duplicate request detected for idempotency key: " + idempotencyKey);
    }
}