package com.banking.transactionservice.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transactions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, updatable = false)
    private String transactionNumber;       // TXN-000001

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, updatable = false)
    private TransactionType type;           // DEPOSIT, WITHDRAWAL, TRANSFER

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionStatus status;       // SUCCESS, FAILED

    @Column(updatable = false)
    private String fromAccountNumber;       // null for deposits

    @Column(updatable = false)
    private String toAccountNumber;         // null for withdrawals

    @Column(nullable = false, precision = 15, scale = 2, updatable = false)
    private BigDecimal amount;

    @Column(updatable = false)
    private String description;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;        // no updatedAt — immutable!

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}