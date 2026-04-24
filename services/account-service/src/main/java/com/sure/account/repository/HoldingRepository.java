package com.sure.account.repository;

import com.sure.account.entity.Holding;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HoldingRepository extends JpaRepository<Holding, UUID> {
    List<Holding> findByAccountId(UUID accountId);

    List<Holding> findByAccountIdAndDate(UUID accountId, LocalDate date);
}
