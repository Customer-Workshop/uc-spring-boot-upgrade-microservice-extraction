package com.sure.transaction.repository;

import com.sure.transaction.entity.Transaction;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionRepository extends JpaRepository<Transaction, UUID> {
    Optional<Transaction> findByEntryId(UUID entryId);
}
