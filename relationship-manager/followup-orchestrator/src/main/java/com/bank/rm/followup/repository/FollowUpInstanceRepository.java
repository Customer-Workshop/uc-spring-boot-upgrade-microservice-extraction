package com.bank.rm.followup.repository;

import com.bank.rm.followup.domain.FollowUpInstance;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface FollowUpInstanceRepository extends JpaRepository<FollowUpInstance, UUID> {
    List<FollowUpInstance> findByCustomerIdOrderByScheduledAtDesc(UUID customerId);
}
