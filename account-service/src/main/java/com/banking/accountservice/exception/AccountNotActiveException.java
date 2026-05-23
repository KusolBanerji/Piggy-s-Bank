package com.banking.accountservice.exception;

public class AccountNotActiveException extends RuntimeException {
    public AccountNotActiveException(String accountNumber) {
        super("Account is not active: " + accountNumber);
    }
}