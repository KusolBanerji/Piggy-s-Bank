package com.banking.accountservice.exception;

import java.math.BigDecimal;

public class InsufficientBalanceException extends RuntimeException {
    public InsufficientBalanceException(String accountNumber, BigDecimal currentBalance) {
        super("Insufficient balance in account: " + accountNumber
                + ". Current balance: " + currentBalance);
    }
}