package com.sure.transaction.repository;

import com.sure.transaction.entity.Entry;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EntryRepository extends JpaRepository<Entry, String> {
    Page<Entry> findByAccountIdAndExcludedFalseOrderByDateDesc(String accountId, Pageable pageable);

    List<Entry> findByAccountIdAndDateBetweenAndExcludedFalseOrderByDateDesc(
            String accountId, LocalDate start, LocalDate end);

    @Query("SELECT e FROM Entry e WHERE e.accountId IN :accountIds AND e.excluded = false ORDER BY e.date DESC")
    Page<Entry> findByAccountIdInAndExcludedFalseOrderByDateDesc(
            @Param("accountIds") List<String> accountIds, Pageable pageable);

    List<Entry> findByAccountIdAndPendingTrue(String accountId);
}
