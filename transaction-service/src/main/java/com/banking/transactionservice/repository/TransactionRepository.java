package com.banking.transactionservice.repository;

import com.banking.transactionservice.entity.Transaction;
import com.banking.transactionservice.entity.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    // All transactions involving an account (sent or received)
    List<Transaction> findByFromAccountNumberOrToAccountNumber(
            String fromAccount, String toAccount);

    // Transactions by type
    List<Transaction> findByType(TransactionType type);

    // Transaction by number
    java.util.Optional<Transaction> findByTransactionNumber(String transactionNumber);
}