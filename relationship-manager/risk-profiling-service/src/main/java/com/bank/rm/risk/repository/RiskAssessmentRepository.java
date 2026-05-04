package com.bank.rm.risk.repository;

import com.bank.rm.risk.domain.RiskAssessment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RiskAssessmentRepository extends JpaRepository<RiskAssessment, UUID> {
    Optional<RiskAssessment> findByCustomerIdAndCurrentTrue(UUID customerId);
    List<RiskAssessment> findByCustomerIdOrderByCreatedAtDesc(UUID customerId);
}
