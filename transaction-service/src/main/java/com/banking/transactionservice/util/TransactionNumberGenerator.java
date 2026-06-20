package com.banking.transactionservice.util;

import com.banking.transactionservice.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TransactionNumberGenerator {

    private final TransactionRepository transactionRepository;

    public String generate() {
        long count = transactionRepository.count() + 1;
        return String.format("TXN-%06d", count);
        // TXN-000001, TXN-000002 ...
    }
}