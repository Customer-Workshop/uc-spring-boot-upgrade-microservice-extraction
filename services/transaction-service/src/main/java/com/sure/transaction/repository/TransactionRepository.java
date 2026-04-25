package com.sure.transaction.repository;

import com.sure.transaction.entity.Transaction;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionRepository extends JpaRepository<Transaction, String> {
    Optional<Transaction> findByEntryId(String entryId);
}
