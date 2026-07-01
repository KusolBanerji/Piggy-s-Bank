package com.banking.transactionservice.controller;

import com.banking.transactionservice.dto.*;
import com.banking.transactionservice.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
@Slf4j
public class TransactionController {

    private final TransactionService transactionService;

    // POST /api/transactions/deposit
    @PostMapping("/deposit")
    public ResponseEntity<TransactionResponse> deposit(
            @Valid @RequestBody DepositRequest request,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey) { // required = false → key is optional, won't fail if missing
        log.info("POST /api/transactions/deposit");
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(transactionService.deposit(request));
    }

    // POST /api/transactions/withdraw
    @PostMapping("/withdraw")
    public ResponseEntity<TransactionResponse> withdraw(
            @Valid @RequestBody WithdrawRequest request,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey) {
        log.info("POST /api/transactions/withdraw");
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(transactionService.withdraw(request));
    }

    // POST /api/transactions/transfer
    @PostMapping("/transfer")
    public ResponseEntity<TransactionResponse> transfer(
            @Valid @RequestBody TransferRequest request,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey) {
        log.info("POST /api/transactions/transfer");
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(transactionService.transfer(request));
    }

    // GET /api/transactions/history/{accountNumber}
    @GetMapping("/history/{accountNumber}")
    public ResponseEntity<List<TransactionResponse>> getHistory(
            @PathVariable String accountNumber) {
        log.info("GET /api/transactions/history/{}", accountNumber);
        return ResponseEntity.ok(transactionService.getTransactionHistory(accountNumber));
    }

    // GET /api/transactions/{transactionNumber}
    @GetMapping("/{transactionNumber}")
    public ResponseEntity<TransactionResponse> getByNumber(
            @PathVariable String transactionNumber) {
        log.info("GET /api/transactions/{}", transactionNumber);
        return ResponseEntity.ok(
                transactionService.getByTransactionNumber(transactionNumber));
    }
}