package com.banking.accountservice.exception;

public class OptimisticLockException extends RuntimeException {
    public OptimisticLockException(String accountNumber) {
        super("Account was modified by another request. Please retry: "
                + accountNumber);
    }
}