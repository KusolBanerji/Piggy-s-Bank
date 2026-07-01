package com.banking.transactionservice.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "idempotency_records")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IdempotencyRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String idempotencyKey;          // the UUID from client header

    @Column(nullable = false)
    private String transactionNumber;        // what transaction was created

    @Column(nullable = false, length = 2000)
    private String responseBody;             // stored JSON response as string

    @Column(nullable = false)
    private Integer httpStatus;              // stored HTTP status code

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime expiresAt;         // createdAt + 24 hours

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        expiresAt = createdAt.plusHours(24); // expires after 24 hours
    }
}