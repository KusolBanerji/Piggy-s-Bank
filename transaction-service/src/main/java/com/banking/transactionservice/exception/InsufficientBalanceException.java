package com.banking.transactionservice.exception;

import java.math.BigDecimal;

public class InsufficientBalanceException extends RuntimeException {
    public InsufficientBalanceException(String accountNumber, BigDecimal balance) {
        super("Insufficient balance in account: " + accountNumber
                + ". Current balance: " + balance);
    }
}