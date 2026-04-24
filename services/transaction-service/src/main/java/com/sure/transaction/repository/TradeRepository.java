package com.sure.transaction.repository;

import com.sure.transaction.entity.Trade;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TradeRepository extends JpaRepository<Trade, UUID> {
    Optional<Trade> findByEntryId(UUID entryId);
}
