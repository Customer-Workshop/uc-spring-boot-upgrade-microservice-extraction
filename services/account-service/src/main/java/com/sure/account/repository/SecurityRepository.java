package com.sure.account.repository;

import com.sure.account.entity.Security;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SecurityRepository extends JpaRepository<Security, UUID> {
    Optional<Security> findByTicker(String ticker);
}
