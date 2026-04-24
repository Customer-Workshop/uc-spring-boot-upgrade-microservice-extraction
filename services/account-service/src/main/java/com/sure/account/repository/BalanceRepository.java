package com.sure.account.repository;

import com.sure.account.entity.Balance;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BalanceRepository extends JpaRepository<Balance, UUID> {
    List<Balance> findByAccountIdOrderByDateDesc(UUID accountId);

    Optional<Balance> findByAccountIdAndDate(UUID accountId, LocalDate date);

    List<Balance> findByAccountIdAndDateBetweenOrderByDateAsc(UUID accountId, LocalDate start, LocalDate end);
}
