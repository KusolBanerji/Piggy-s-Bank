package com.banking.transactionservice.service;

import com.banking.transactionservice.client.AccountServiceClient;
import com.banking.transactionservice.dto.*;
import com.banking.transactionservice.entity.*;
import com.banking.transactionservice.exception.*;
import com.banking.transactionservice.repository.TransactionRepository;
import com.banking.transactionservice.util.TransactionNumberGenerator;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountServiceClient accountServiceClient;
    private final TransactionNumberGenerator transactionNumberGenerator;

    // ─── DEPOSIT ──────────────────────────────────────────────
    @Transactional
    public TransactionResponse deposit(DepositRequest request) {
        log.info("Deposit {} to account: {}",
                request.getAmount(), request.getToAccountNumber());

        // Step 1: Verify account exists
        getAccountOrThrow(request.getToAccountNumber());

        // Step 2: Credit the account
        accountServiceClient.credit(
                request.getToAccountNumber(),
                BalanceUpdateRequest.builder()
                        .amount(request.getAmount())
                        .description(request.getDescription())
                        .build()
        );

        // Step 3: Record the transaction
        Transaction transaction = Transaction.builder()
                .transactionNumber(transactionNumberGenerator.generate())
                .type(TransactionType.DEPOSIT)
                .status(TransactionStatus.SUCCESS)
                .toAccountNumber(request.getToAccountNumber())
                .amount(request.getAmount())
                .description(request.getDescription())
                .build();

        Transaction saved = transactionRepository.save(transaction);
        log.info("Deposit successful: {}", saved.getTransactionNumber());
        return mapToResponse(saved);
    }

    // ─── WITHDRAWAL ───────────────────────────────────────────
    @Transactional
    public TransactionResponse withdraw(WithdrawRequest request) {
        log.info("Withdraw {} from account: {}",
                request.getAmount(), request.getFromAccountNumber());

        // Step 1: Verify account exists
        getAccountOrThrow(request.getFromAccountNumber());

        // Step 2: Debit the account
        // Account Service handles insufficient balance check
        try {
            accountServiceClient.debit(
                    request.getFromAccountNumber(),
                    BalanceUpdateRequest.builder()
                            .amount(request.getAmount())
                            .description(request.getDescription())
                            .build()
            );
        } catch (FeignException.BadRequest e) {
            throw new InsufficientBalanceException(request.getFromAccountNumber(), request.getAmount());
        }

        // Step 3: Record the transaction
        Transaction transaction = Transaction.builder()
                .transactionNumber(transactionNumberGenerator.generate())
                .type(TransactionType.WITHDRAWAL)
                .status(TransactionStatus.SUCCESS)
                .fromAccountNumber(request.getFromAccountNumber())
                .amount(request.getAmount())
                .description(request.getDescription())
                .build();

        Transaction saved = transactionRepository.save(transaction);
        log.info("Withdrawal successful: {}", saved.getTransactionNumber());
        return mapToResponse(saved);
    }

    // ─── TRANSFER ─────────────────────────────────────────────
    @Transactional      // Critical — both debit and credit must succeed or both rollback
    public TransactionResponse transfer(TransferRequest request) {
        log.info("Transfer {} from {} to {}",
                request.getAmount(),
                request.getFromAccountNumber(),
                request.getToAccountNumber());

        // Business rule: can't transfer to same account
        if (request.getFromAccountNumber().equals(request.getToAccountNumber())) {
            throw new SameAccountTransferException();
        }

        // Step 1: Verify both accounts exist
        getAccountOrThrow(request.getFromAccountNumber());
        getAccountOrThrow(request.getToAccountNumber());

        // Step 2: Debit source account
        try {
            accountServiceClient.debit(
                    request.getFromAccountNumber(),
                    BalanceUpdateRequest.builder()
                            .amount(request.getAmount())
                            .description("Transfer to " + request.getToAccountNumber())
                            .build()
            );
        } catch (FeignException.BadRequest e) {
            throw new InsufficientBalanceException(request.getFromAccountNumber(), request.getAmount());
        }

        // Step 3: Credit destination account
        // If this fails → @Transactional rolls back the debit ✅
        accountServiceClient.credit(
                request.getToAccountNumber(),
                BalanceUpdateRequest.builder()
                        .amount(request.getAmount())
                        .description("Transfer from " + request.getFromAccountNumber())
                        .build()
        );

        // Step 4: Record the transaction
        Transaction transaction = Transaction.builder()
                .transactionNumber(transactionNumberGenerator.generate())
                .type(TransactionType.TRANSFER)
                .status(TransactionStatus.SUCCESS)
                .fromAccountNumber(request.getFromAccountNumber())
                .toAccountNumber(request.getToAccountNumber())
                .amount(request.getAmount())
                .description(request.getDescription())
                .build();

        Transaction saved = transactionRepository.save(transaction);
        log.info("Transfer successful: {}", saved.getTransactionNumber());
        return mapToResponse(saved);
    }

    // ─── GET HISTORY BY ACCOUNT ───────────────────────────────
    public List<TransactionResponse> getTransactionHistory(String accountNumber) {
        log.info("Fetching transaction history for: {}", accountNumber);

        // Verify account exists
        getAccountOrThrow(accountNumber);

        return transactionRepository
                .findByFromAccountNumberOrToAccountNumber(accountNumber, accountNumber)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    // ─── GET BY TRANSACTION NUMBER ────────────────────────────
    public TransactionResponse getByTransactionNumber(String transactionNumber) {
        log.info("Fetching transaction: {}", transactionNumber);
        Transaction transaction = transactionRepository
                .findByTransactionNumber(transactionNumber)
                .orElseThrow(() -> new RuntimeException(
                        "Transaction not found: " + transactionNumber));
        return mapToResponse(transaction);
    }

    // ─── Private Helpers ──────────────────────────────────────
    private AccountResponse getAccountOrThrow(String accountNumber) {
        try {
            return accountServiceClient.getAccountByNumber(accountNumber);
        } catch (FeignException.NotFound e) {
            throw new AccountNotFoundException(accountNumber);
        }
    }

    private TransactionResponse mapToResponse(Transaction t) {
        return TransactionResponse.builder()
                .id(t.getId())
                .transactionNumber(t.getTransactionNumber())
                .type(t.getType())
                .status(t.getStatus())
                .fromAccountNumber(t.getFromAccountNumber())
                .toAccountNumber(t.getToAccountNumber())
                .amount(t.getAmount())
                .description(t.getDescription())
                .createdAt(t.getCreatedAt())
                .build();
    }
}