package com.banking.accountservice.controller;

import com.banking.accountservice.dto.*;
import com.banking.accountservice.service.AccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
@Slf4j
public class AccountController {

    private final AccountService accountService;

    // POST /api/accounts
    @PostMapping
    public ResponseEntity<AccountResponse> createAccount(
            @Valid @RequestBody CreateAccountRequest request) {
        log.info("POST /api/accounts");
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(accountService.createAccount(request));
    }

    // GET /api/accounts/user/{userId}
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<AccountResponse>> getAccountsByUser(
            @PathVariable Long userId) {
        log.info("GET /api/accounts/user/{}", userId);
        return ResponseEntity.ok(accountService.getAccountsByUserId(userId));
    }

    // GET /api/accounts/{id}
    @GetMapping("/{id}")
    public ResponseEntity<AccountResponse> getAccountById(
            @PathVariable Long id) {
        log.info("GET /api/accounts/{}", id);
        return ResponseEntity.ok(accountService.getAccountById(id));
    }

    // GET /api/accounts/number/{accountNumber}
    @GetMapping("/number/{accountNumber}")
    public ResponseEntity<AccountResponse> getAccountByNumber(
            @PathVariable String accountNumber) {
        log.info("GET /api/accounts/number/{}", accountNumber);
        return ResponseEntity.ok(accountService.getAccountByNumber(accountNumber));
    }

    // PATCH /api/accounts/{id}/close
    @PatchMapping("/{id}/close")
    public ResponseEntity<AccountResponse> closeAccount(
            @PathVariable Long id) {
        log.info("PATCH /api/accounts/{}/close", id);
        return ResponseEntity.ok(accountService.closeAccount(id));
    }

    // GET /api/accounts/user/{userId}/has-active
    // Used by other services to check if user has active accounts
    @GetMapping("/user/{userId}/has-active")
    public ResponseEntity<Boolean> hasActiveAccounts(
            @PathVariable Long userId) {
        log.info("GET /api/accounts/user/{}/has-active", userId);
        return ResponseEntity.ok(accountService.hasActiveAccounts(userId));
    }

    // PUT /api/accounts/{accountNumber}/debit
    @PutMapping("/{accountNumber}/debit")
    public ResponseEntity<AccountResponse> debit(
            @PathVariable String accountNumber,
            @Valid @RequestBody BalanceUpdateRequest request) {
        log.info("PUT /api/accounts/{}/debit", accountNumber);
        return ResponseEntity.ok(accountService.debit(accountNumber, request));
    }

    // PUT /api/accounts/{accountNumber}/credit
    @PutMapping("/{accountNumber}/credit")
    public ResponseEntity<AccountResponse> credit(
            @PathVariable String accountNumber,
            @Valid @RequestBody BalanceUpdateRequest request) {
        log.info("PUT /api/accounts/{}/credit", accountNumber);
        return ResponseEntity.ok(accountService.credit(accountNumber, request));
    }
}