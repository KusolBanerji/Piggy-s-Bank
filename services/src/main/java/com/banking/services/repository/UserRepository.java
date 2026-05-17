package com.banking.services.repository;

import com.banking.services.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository     // Marks this as the data access layer
public interface UserRepository extends JpaRepository<User, Long> {
    // JpaRepository<User, Long> means:
    //   User → the entity this repository manages
    //   Long → the type of the primary key (id)

    // By extending JpaRepository, you get these for FREE — no code needed:
    // save(user)           → INSERT or UPDATE
    // findById(id)         → SELECT WHERE id = ?
    // findAll()            → SELECT * FROM users
    // deleteById(id)       → DELETE WHERE id = ?
    // existsById(id)       → check if record exists
    // count()              → SELECT COUNT(*)

    // Custom queries — Spring generates SQL just from the method name!
    Optional<User> findByEmail(String email);
    // Spring reads this and generates: SELECT * FROM users WHERE email = ?

    Optional<User> findByPhone(String phone);
    // Generates: SELECT * FROM users WHERE phone = ?

    boolean existsByEmail(String email);
    // Generates: SELECT COUNT(*) > 0 FROM users WHERE email = ?

    boolean existsByPhone(String phone);
    // Generates: SELECT COUNT(*) > 0 FROM users WHERE phone = ?
}