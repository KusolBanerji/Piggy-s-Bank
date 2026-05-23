package com.banking.services.dto;

import jakarta.validation.constraints.*;
import lombok.*;

// DTO = Data Transfer Object — a simple class whose only job is to carry data between layers.
// DTO helps decouple entity classes from API req/resps, and allows us to add validation specific to the API layer without cluttering the entity.
// It contains only the fields needed for an API req/resp, and validation annotations to enforce constraints on incoming data.
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateUserRequest {

    @NotBlank(message = "First name is required")
    private String firstName;

    @NotBlank(message = "Last name is required")
    private String lastName;

    @Email(message = "Must be a valid email")
    @NotBlank(message = "Email is required")
    private String email;

    @NotBlank(message = "Phone is required")
    private String phone;

    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;
}