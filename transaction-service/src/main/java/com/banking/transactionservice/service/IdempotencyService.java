package com.banking.transactionservice.service;

import com.banking.transactionservice.entity.IdempotencyRecord;
import com.banking.transactionservice.repository.IdempotencyRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class IdempotencyService {

    private final IdempotencyRepository idempotencyRepository;
    private final ObjectMapper objectMapper;       // Spring auto-provides this bean
                                                   // converts objects ↔ JSON strings

    // ─── CHECK: Has this key been processed before? ───────────
    public Optional<IdempotencyRecord> findValidRecord(String idempotencyKey) {
        return idempotencyRepository
                .findByIdempotencyKeyAndExpiresAtAfter(
                        idempotencyKey,
                        LocalDateTime.now()        // only return non-expired records
                );
    }

    // ─── STORE: Save key + response after processing ──────────
    @Transactional
    public void saveRecord(String idempotencyKey,
                           String transactionNumber,
                           Object responseBody,
                           int httpStatus) {
        try {
            String responseJson = objectMapper.writeValueAsString(responseBody);

            IdempotencyRecord record = IdempotencyRecord.builder()
                    .idempotencyKey(idempotencyKey)
                    .transactionNumber(transactionNumber)
                    .responseBody(responseJson)
                    .httpStatus(httpStatus)
                    .build();

            idempotencyRepository.save(record);
            log.info("Saved idempotency record for key: {}", idempotencyKey);

        } catch (JsonProcessingException e) {
            log.error("Failed to serialize response for idempotency record: {}",
                    e.getMessage());
        }
    }

    // ─── DESERIALIZE: Convert stored JSON back to object ──────
    public <T> T deserializeResponse(String responseJson, Class<T> targetClass) {
        try {
            return objectMapper.readValue(responseJson, targetClass);
        } catch (JsonProcessingException e) {
            log.error("Failed to deserialize idempotency response: {}",
                    e.getMessage());
            throw new RuntimeException("Failed to deserialize stored response");
        }
    }

    // ─── CLEANUP: Delete expired records periodically ─────────
    @Scheduled(cron = "0 0 * * * *")   // runs every hour
    @Transactional
    public void cleanupExpiredRecords() {
        log.info("Cleaning up expired idempotency records");
        idempotencyRepository.deleteByExpiresAtBefore(LocalDateTime.now());
        log.info("Expired idempotency records cleaned up");
    }
}