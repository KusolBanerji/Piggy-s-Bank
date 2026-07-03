package com.banking.transactionservice.service;

import com.banking.transactionservice.client.AccountServiceClient;
import com.banking.transactionservice.dto.BalanceUpdateRequest;
import com.banking.transactionservice.dto.TransferRequest;
import com.banking.transactionservice.dto.TransactionResponse;
import com.banking.transactionservice.entity.*;
import com.banking.transactionservice.exception.*;
import com.banking.transactionservice.repository.TransactionRepository;
import com.banking.transactionservice.util.TransactionNumberGenerator;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransferSagaService {

    private final AccountServiceClient accountServiceClient;
    private final TransactionRepository transactionRepository;
    private final TransactionNumberGenerator transactionNumberGenerator;

    @Transactional
    public TransactionResponse executeTransfer(TransferRequest request) {
        log.info("Starting transfer saga: {} from {} to {}",
                request.getAmount(),
                request.getFromAccountNumber(),
                request.getToAccountNumber());

        // ── STEP 1: Debit source account ──────────────────────
        log.info("Saga Step 1: Debiting {}", request.getFromAccountNumber());
        try {
            accountServiceClient.debit(
                    request.getFromAccountNumber(),
                    BalanceUpdateRequest.builder()
                            .amount(request.getAmount())
                            .description("Transfer to "
                                    + request.getToAccountNumber())
                            .build()
            );
        } catch (FeignException e) {
            // Step 1 failed — nothing was done, just record and fail
            log.error("Saga Step 1 FAILED: Could not debit {}",
                    request.getFromAccountNumber());
            saveTransactionRecord(
                    TransactionType.TRANSFER_DEBIT,
                    TransactionStatus.FAILED,
                    request.getFromAccountNumber(),
                    null,
                    request.getAmount(),
                    "FAILED: " + e.getMessage()
            );
            throw new TransferFailedException(
                    "Transfer failed: could not debit source account");
        }

        // Record Step 1 success
        saveTransactionRecord(
                TransactionType.TRANSFER_DEBIT,
                TransactionStatus.SUCCESS,
                request.getFromAccountNumber(),
                null,
                request.getAmount(),
                "Transfer debit to " + request.getToAccountNumber()
        );
        log.info("Saga Step 1 SUCCESS: Debited {}", request.getFromAccountNumber());

        // ── STEP 2: Credit destination account ────────────────
        log.info("Saga Step 2: Crediting {}", request.getToAccountNumber());
        try {
            accountServiceClient.credit(
                    request.getToAccountNumber(),
                    BalanceUpdateRequest.builder()
                            .amount(request.getAmount())
                            .description("Transfer from "
                                    + request.getFromAccountNumber())
                            .build()
            );
        } catch (FeignException e) {
            // Step 2 failed — COMPENSATE Step 1
            log.error("Saga Step 2 FAILED: Could not credit {}. Compensating...",
                    request.getToAccountNumber());

            saveTransactionRecord(
                    TransactionType.TRANSFER_CREDIT,
                    TransactionStatus.FAILED,
                    null,
                    request.getToAccountNumber(),
                    request.getAmount(),
                    "FAILED: " + e.getMessage()
            );

            // ── COMPENSATION: Reverse the debit ───────────────
            compensateDebit(request.getFromAccountNumber(), request.getAmount());

            throw new TransferFailedException(
                    "Transfer failed: could not credit destination account. "
                    + "Source account has been refunded.");
        }

        // Record Step 2 success
        saveTransactionRecord(
                TransactionType.TRANSFER_CREDIT,
                TransactionStatus.SUCCESS,
                null,
                request.getToAccountNumber(),
                request.getAmount(),
                "Transfer credit from " + request.getFromAccountNumber()
        );
        log.info("Saga Step 2 SUCCESS: Credited {}", request.getToAccountNumber());

        // ── STEP 3: Save main transfer record ─────────────────
        log.info("Saga Step 3: Saving transfer record");
        Transaction transfer = Transaction.builder()
                .transactionNumber(transactionNumberGenerator.generate())
                .type(TransactionType.TRANSFER)
                .status(TransactionStatus.SUCCESS)
                .fromAccountNumber(request.getFromAccountNumber())
                .toAccountNumber(request.getToAccountNumber())
                .amount(request.getAmount())
                .description(request.getDescription())
                .build();

        Transaction saved = transactionRepository.save(transfer);
        log.info("Transfer saga completed successfully: {}",
                saved.getTransactionNumber());

        return mapToResponse(saved);
    }

    // ── COMPENSATION: Reverse Step 1 if Step 2 fails ──────────
    private void compensateDebit(String accountNumber,
                                  java.math.BigDecimal amount) {
        log.info("COMPENSATING: Reversing debit for {}", accountNumber);
        try {
            accountServiceClient.reverseDebit(
                    accountNumber,
                    BalanceUpdateRequest.builder()
                            .amount(amount)
                            .description("Saga compensation — transfer reversal")
                            .build()
            );

            // Record the compensation
            saveTransactionRecord(
                    TransactionType.TRANSFER_REVERSAL,
                    TransactionStatus.SUCCESS,
                    accountNumber,
                    null,
                    amount,
                    "Saga compensation — debit reversed"
            );
            log.info("COMPENSATION SUCCESS: Debit reversed for {}", accountNumber);

        } catch (FeignException e) {
            // Compensation itself failed — this is a critical situation
            // In production: alert operations team immediately!
            log.error("CRITICAL: Compensation FAILED for {}. Manual intervention required!",
                    accountNumber);

            saveTransactionRecord(
                    TransactionType.TRANSFER_REVERSAL,
                    TransactionStatus.FAILED,
                    accountNumber,
                    null,
                    amount,
                    "CRITICAL: Compensation failed — manual intervention required"
            );
            // Don't throw here — we've already failed, just log for manual fix
        }
    }

    // ── Private Helpers ────────────────────────────────────────
    private void saveTransactionRecord(
            TransactionType type,
            TransactionStatus status,
            String fromAccount,
            String toAccount,
            java.math.BigDecimal amount,
            String description) {

        Transaction record = Transaction.builder()
                .transactionNumber(transactionNumberGenerator.generate())
                .type(type)
                .status(status)
                .fromAccountNumber(fromAccount)
                .toAccountNumber(toAccount)
                .amount(amount)
                .description(description)
                .build();

        transactionRepository.save(record);
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