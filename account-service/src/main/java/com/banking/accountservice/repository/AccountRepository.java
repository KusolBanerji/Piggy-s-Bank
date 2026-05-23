package com.banking.accountservice.repository;

import com.banking.accountservice.entity.Account;
import com.banking.accountservice.entity.AccountStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {

    // All accounts belonging to a user
    List<Account> findByUserId(Long userId);

    // Find by account number
    Optional<Account> findByAccountNumber(String accountNumber);

    // All active accounts of a user
    List<Account> findByUserIdAndStatus(Long userId, AccountStatus status);

    // Check if user has any active accounts (used when deleting user)
    boolean existsByUserIdAndStatus(Long userId, AccountStatus status);
}