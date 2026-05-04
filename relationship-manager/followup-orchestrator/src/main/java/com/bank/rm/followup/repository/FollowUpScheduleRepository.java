package com.bank.rm.followup.repository;

import com.bank.rm.followup.domain.FollowUpSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FollowUpScheduleRepository extends JpaRepository<FollowUpSchedule, UUID> {
    Optional<FollowUpSchedule> findByCustomerIdAndActiveTrue(UUID customerId);

    @Query("SELECT s FROM FollowUpSchedule s WHERE s.active = true AND s.nextFollowUpAt <= :cutoff")
    List<FollowUpSchedule> findDueSchedules(LocalDateTime cutoff);
}
