package com.banking.transactionservice.entity;

public enum TransactionType {
    DEPOSIT,
    WITHDRAWAL,
    TRANSFER,
    TRANSFER_DEBIT,         // debit leg of a transfer
    TRANSFER_CREDIT,        // credit leg of a transfer
    TRANSFER_REVERSAL       // compensation — debit reversed
}