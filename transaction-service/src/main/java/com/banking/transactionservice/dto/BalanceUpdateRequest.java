package com.banking.transactionservice.dto;

import lombok.*;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BalanceUpdateRequest {
    private BigDecimal amount;
    private String description;
}