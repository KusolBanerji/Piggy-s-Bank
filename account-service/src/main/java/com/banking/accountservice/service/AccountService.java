package com.banking.accountservice.service;

import com.banking.accountservice.client.UserServiceClient;
import com.banking.accountservice.dto.*;
import com.banking.accountservice.entity.*;
import com.banking.accountservice.exception.*;
import com.banking.accountservice.repository.AccountRepository;
import com.banking.accountservice.util.AccountNumberGenerator;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AccountService {

    private final AccountRepository accountRepository;
    private final UserServiceClient userServiceClient;
    private final AccountNumberGenerator accountNumberGenerator;

    // ─── CREATE ───────────────────────────────────────────────
    @Transactional
    public AccountResponse createAccount(CreateAccountRequest request) {
        log.info("Creating account for userId: {}", request.getUserId());

        // Step 1: Verify user exists by calling User Service
        UserResponse user = getUserOrThrow(request.getUserId());

        // Step 2: Generate unique account number
        String accountNumber = accountNumberGenerator.generate();

        // Step 3: Build and save account
        Account account = Account.builder()
                .userId(request.getUserId())
                .accountNumber(accountNumber)
                .accountType(request.getAccountType())
                .balance(request.getInitialDeposit())
                .build();

        Account saved = accountRepository.save(account);
        log.info("Account created: {}", accountNumber);
        return mapToResponse(saved, user);
    }

    // ─── GET ALL BY USER ──────────────────────────────────────
    public List<AccountResponse> getAccountsByUserId(Long userId) {
        log.info("Fetching accounts for userId: {}", userId);

        // Verify user exists
        UserResponse user = getUserOrThrow(userId);

        return accountRepository.findByUserId(userId)
                .stream()
                .map(account -> mapToResponse(account, user))
                .toList();
    }

    // ─── GET BY ACCOUNT NUMBER ────────────────────────────────
    public AccountResponse getAccountByNumber(String accountNumber) {
        log.info("Fetching account: {}", accountNumber);

        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new AccountNotFoundException(accountNumber));

        UserResponse user = getUserOrThrow(account.getUserId());
        return mapToResponse(account, user);
    }

    // ─── GET BY ID ────────────────────────────────────────────
    public AccountResponse getAccountById(Long id) {
        log.info("Fetching account with id: {}", id);
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new AccountNotFoundException(id));
        UserResponse user = getUserOrThrow(account.getUserId());
        return mapToResponse(account, user);
    }

    // ─── CLOSE ACCOUNT ────────────────────────────────────────
    @Transactional
    public AccountResponse closeAccount(Long id) {
        log.info("Closing account with id: {}", id);

        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new AccountNotFoundException(id));

        // Business rule: can't close already closed account
        if (account.getStatus() == AccountStatus.CLOSED) {
            throw new AccountNotActiveException(account.getAccountNumber());
        }

        account.setStatus(AccountStatus.CLOSED);
        Account saved = accountRepository.save(account);

        UserResponse user = getUserOrThrow(account.getUserId());
        log.info("Account closed: {}", account.getAccountNumber());
        return mapToResponse(saved, user);
    }

    // ─── CHECK USER HAS ACTIVE ACCOUNTS ───────────────────────
    public boolean hasActiveAccounts(Long userId) {
        return accountRepository.existsByUserIdAndStatus(userId, AccountStatus.ACTIVE);
    }

    // ─── Private Helpers ──────────────────────────────────────
    private UserResponse getUserOrThrow(Long userId) {
        try {
            return userServiceClient.getUserById(userId);
        } catch (FeignException.NotFound e) {
            // User Service returned 404 → user doesn't exist
            throw new UserNotFoundException(userId);
        }
    }

    private AccountResponse mapToResponse(Account account, UserResponse user) {
        return AccountResponse.builder()
                .id(account.getId())
                .accountNumber(account.getAccountNumber())
                .userId(account.getUserId())
                .userFullName(user.getFirstName() + " " + user.getLastName())
                .accountType(account.getAccountType())
                .status(account.getStatus())
                .balance(account.getBalance())
                .createdAt(account.getCreatedAt())
                .updatedAt(account.getUpdatedAt())
                .build();
    }
}