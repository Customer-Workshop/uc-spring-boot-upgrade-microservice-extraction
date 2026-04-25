package com.sure.transaction.repository;

import com.sure.transaction.entity.Trade;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TradeRepository extends JpaRepository<Trade, String> {
    Optional<Trade> findByEntryId(String entryId);
}
