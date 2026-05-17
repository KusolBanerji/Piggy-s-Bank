package com.banking.services.entity;

import jakarta.persistence.*;          // JPA annotations for mapping this class to a database table and columns
import jakarta.validation.constraints.*; // Validation annotations used to enforce field constraints like @NotBlank and @Email
import lombok.*;                         // Lombok annotations to generate boilerplate code (getters/setters/constructors/builder)
import java.time.LocalDateTime;         // Java date-time type for createdAt and updatedAt timestamps

@Entity                          // This class = a database table
@Table(name = "users")           // Table will be named "users"
@Data                            // Lombok: generates getters, setters, toString, equals, hashCode
@NoArgsConstructor               // Lombok: generates empty constructor (JPA requires this)
@AllArgsConstructor              // Lombok: generates constructor with all fields
@Builder                         // Lombok: enables User.builder().name("John").build()
                                 // @Builder makes it easy to construct User instances without manually writing a builder class.
public class User {

    @Id                                                    // This field is the primary key
    @GeneratedValue(strategy = GenerationType.IDENTITY)    // Auto increment: 1, 2, 3...
    private Long id;

    @NotBlank(message = "First name is required")
    @Column(nullable = false)                              // DB column cannot be null
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Column(nullable = false)
    private String lastName;

    @Email(message = "Must be a valid email")
    @NotBlank(message = "Email is required")
    @Column(nullable = false, unique = true)               // No two users same email
    private String email;

    @NotBlank(message = "Phone is required")
    @Column(nullable = false, unique = true)
    private String phone;

    @NotBlank(message = "Password is required")
    @Column(nullable = false)
    private String password;

    @Column(nullable = false, updatable = false)           // Set once, never updated
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist         // Runs automatically BEFORE saving to DB for the first time
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate          // Runs automatically BEFORE every update
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}