package com.banking.transactionservice.repository;

import com.banking.transactionservice.entity.IdempotencyRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface IdempotencyRepository extends JpaRepository<IdempotencyRecord, Long> {

    // Find by key if not expired
    Optional<IdempotencyRecord> findByIdempotencyKeyAndExpiresAtAfter(
            String idempotencyKey, LocalDateTime now);
    // Spring generates:
    // WHERE idempotency_key = ? AND expires_at > ?
    // "give me the record for this key IF it hasn't expired yet"

    // Cleanup old expired records (called periodically)
    void deleteByExpiresAtBefore(LocalDateTime now);
}