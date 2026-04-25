package com.sure.account.repository;

import com.sure.account.entity.Holding;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HoldingRepository extends JpaRepository<Holding, String> {
    List<Holding> findByAccountId(String accountId);

    List<Holding> findByAccountIdAndDate(String accountId, LocalDate date);
}
