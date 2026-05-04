package com.bank.rm.projection.repository;

import com.bank.rm.projection.domain.WealthProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface WealthProjectionRepository extends JpaRepository<WealthProjection, UUID> {
    Optional<WealthProjection> findFirstByCustomerIdOrderByCreatedAtDesc(UUID customerId);
}
