package com.sure.account.repository;

import com.sure.account.entity.Balance;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BalanceRepository extends JpaRepository<Balance, String> {
    List<Balance> findByAccountIdOrderByDateDesc(String accountId);

    Optional<Balance> findByAccountIdAndDate(String accountId, LocalDate date);

    List<Balance> findByAccountIdAndDateBetweenOrderByDateAsc(String accountId, LocalDate start, LocalDate end);
}
