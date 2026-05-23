package com.banking.accountservice.util;

import com.banking.accountservice.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component                      // utility bean — not a service, repository, or controller
@RequiredArgsConstructor
public class AccountNumberGenerator {

    private final AccountRepository accountRepository;

    public String generate() {
        // count existing accounts + 1 → format as 6-digit padded number
        long count = accountRepository.count() + 1;
        return String.format("PB-%06d", count);
        // PB-000001, PB-000002, PB-000003 ...
    }
}