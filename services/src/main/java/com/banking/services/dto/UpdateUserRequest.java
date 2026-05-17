package com.banking.services.dto;

import jakarta.validation.constraints.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateUserRequest {

    @NotBlank(message = "First name is required")
    private String firstName;

    @NotBlank(message = "Last name is required")
    private String lastName;

    @NotBlank(message = "Phone is required")
    private String phone;

    // ❌ No email  → email changes need a separate dedicated flow
    // ❌ No password → password changes need a separate dedicated flow
    // ❌ No id, createdAt, updatedAt → system managed, client can't touch these
}