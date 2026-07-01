package com.banking.accountservice.entity;

import jakarta.persistence.*;
//import jakarta.validation.constraints.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "accounts")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, updatable = false)
    private String accountNumber;           // e.g. "PB-000001"

    @Column(nullable = false)
    private Long userId;                    // plain number — no FK, no @ManyToOne!

    @Enumerated(EnumType.STRING)            // stores "SAVINGS" not 0, 1, 2
    @Column(nullable = false)
    private AccountType accountType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AccountStatus status;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal balance;             // BigDecimal for money — NEVER use double!

    @Version                        // ← ADD THIS — one line, Hibernate does the rest
    private Long version;           // starts at 0, increments on every update
    
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        status = AccountStatus.ACTIVE;      // always starts as ACTIVE
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}