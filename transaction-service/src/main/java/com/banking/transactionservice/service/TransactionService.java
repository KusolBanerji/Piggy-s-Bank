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
    private final IdempotencyService idempotencyService;    // ← ADD THIS

    // ─── DEPOSIT ──────────────────────────────────────────────
    @Transactional
    public TransactionResponse deposit(DepositRequest request,
                                       String idempotencyKey) {   // ← ADD KEY PARAM
        log.info("Deposit {} to account: {}",
                request.getAmount(), request.getToAccountNumber());

        // Check idempotency — already processed?
        if (idempotencyKey != null) {
            Optional<IdempotencyRecord> existing =
                    idempotencyService.findValidRecord(idempotencyKey);
            if (existing.isPresent()) {
                log.info("Duplicate deposit request detected, returning stored response");
                return idempotencyService.deserializeResponse(
                        existing.get().getResponseBody(),
                        TransactionResponse.class);
            }
        }

        // Not seen before — process normally
        getAccountOrThrow(request.getToAccountNumber());

        accountServiceClient.credit(
                request.getToAccountNumber(),
                BalanceUpdateRequest.builder()
                        .amount(request.getAmount())
                        .description(request.getDescription())
                        .build()
        );

        Transaction transaction = Transaction.builder()
                .transactionNumber(transactionNumberGenerator.generate())
                .type(TransactionType.DEPOSIT)
                .status(TransactionStatus.SUCCESS)
                .toAccountNumber(request.getToAccountNumber())
                .amount(request.getAmount())
                .description(request.getDescription())
                .build();

        Transaction saved = transactionRepository.save(transaction);
        TransactionResponse response = mapToResponse(saved);

        // Store idempotency record
        if (idempotencyKey != null) {
            idempotencyService.saveRecord(
                    idempotencyKey,
                    saved.getTransactionNumber(),
                    response,
                    201
            );
        }

        log.info("Deposit successful: {}", saved.getTransactionNumber());
        return response;
    }

    // ─── WITHDRAWAL ───────────────────────────────────────────
    @Transactional
    public TransactionResponse withdraw(WithdrawRequest request,
                                        String idempotencyKey) {
        log.info("Withdraw {} from account: {}",
                request.getAmount(), request.getFromAccountNumber());

        // Check idempotency
        if (idempotencyKey != null) {
            Optional<IdempotencyRecord> existing =
                    idempotencyService.findValidRecord(idempotencyKey);
            if (existing.isPresent()) {
                log.info("Duplicate withdrawal request detected, returning stored response");
                return idempotencyService.deserializeResponse(
                        existing.get().getResponseBody(),
                        TransactionResponse.class);
            }
        }

        getAccountOrThrow(request.getFromAccountNumber());

        accountServiceClient.debit(
                request.getFromAccountNumber(),
                BalanceUpdateRequest.builder()
                        .amount(request.getAmount())
                        .description(request.getDescription())
                        .build()
        );

        Transaction transaction = Transaction.builder()
                .transactionNumber(transactionNumberGenerator.generate())
                .type(TransactionType.WITHDRAWAL)
                .status(TransactionStatus.SUCCESS)
                .fromAccountNumber(request.getFromAccountNumber())
                .amount(request.getAmount())
                .description(request.getDescription())
                .build();

        Transaction saved = transactionRepository.save(transaction);
        TransactionResponse response = mapToResponse(saved);

        if (idempotencyKey != null) {
            idempotencyService.saveRecord(
                    idempotencyKey,
                    saved.getTransactionNumber(),
                    response,
                    201
            );
        }

        log.info("Withdrawal successful: {}", saved.getTransactionNumber());
        return response;
    }

    // ─── TRANSFER ─────────────────────────────────────────────
    @Transactional
    public TransactionResponse transfer(TransferRequest request,
                                        String idempotencyKey) {
        log.info("Transfer {} from {} to {}",
                request.getAmount(),
                request.getFromAccountNumber(),
                request.getToAccountNumber());

        // Check idempotency
        if (idempotencyKey != null) {
            Optional<IdempotencyRecord> existing =
                    idempotencyService.findValidRecord(idempotencyKey);
            if (existing.isPresent()) {
                log.info("Duplicate transfer request detected, returning stored response");
                return idempotencyService.deserializeResponse(
                        existing.get().getResponseBody(),
                        TransactionResponse.class);
            }
        }

        if (request.getFromAccountNumber().equals(request.getToAccountNumber())) {
            throw new SameAccountTransferException();
        }

        getAccountOrThrow(request.getFromAccountNumber());
        getAccountOrThrow(request.getToAccountNumber());

        accountServiceClient.debit(
                request.getFromAccountNumber(),
                BalanceUpdateRequest.builder()
                        .amount(request.getAmount())
                        .description("Transfer to " + request.getToAccountNumber())
                        .build()
        );

        accountServiceClient.credit(
                request.getToAccountNumber(),
                BalanceUpdateRequest.builder()
                        .amount(request.getAmount())
                        .description("Transfer from " + request.getFromAccountNumber())
                        .build()
        );

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
        TransactionResponse response = mapToResponse(saved);

        if (idempotencyKey != null) {
            idempotencyService.saveRecord(
                    idempotencyKey,
                    saved.getTransactionNumber(),
                    response,
                    201
            );
        }

        log.info("Transfer successful: {}", saved.getTransactionNumber());
        return response;
    }

    // ─── existing methods unchanged below ─────────────────────
    public List<TransactionResponse> getTransactionHistory(String accountNumber) {
        getAccountOrThrow(accountNumber);
        return transactionRepository
                .findByFromAccountNumberOrToAccountNumber(accountNumber, accountNumber)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public TransactionResponse getByTransactionNumber(String transactionNumber) {
        Transaction transaction = transactionRepository
                .findByTransactionNumber(transactionNumber)
                .orElseThrow(() -> new RuntimeException(
                        "Transaction not found: " + transactionNumber));
        return mapToResponse(transaction);
    }

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