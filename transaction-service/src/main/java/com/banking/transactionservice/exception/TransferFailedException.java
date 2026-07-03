package com.banking.transactionservice.exception;

public class TransferFailedException extends RuntimeException {
    public TransferFailedException(String message) {
        super(message);
    }
}