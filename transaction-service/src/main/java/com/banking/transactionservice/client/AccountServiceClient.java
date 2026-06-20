package com.banking.transactionservice.client;

import com.banking.transactionservice.dto.AccountResponse;
import com.banking.transactionservice.dto.BalanceUpdateRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "account-service", url = "${account-service.url}")
public interface AccountServiceClient {

    // Get account details by account number
    @GetMapping("/api/accounts/number/{accountNumber}")
    AccountResponse getAccountByNumber(@PathVariable String accountNumber);

    // Deduct from account balance
    @PutMapping("/api/accounts/{accountNumber}/debit")
    AccountResponse debit(@PathVariable String accountNumber,
                          @RequestBody BalanceUpdateRequest request);

    // Add to account balance
    @PutMapping("/api/accounts/{accountNumber}/credit")
    AccountResponse credit(@PathVariable String accountNumber,
                           @RequestBody BalanceUpdateRequest request);
}