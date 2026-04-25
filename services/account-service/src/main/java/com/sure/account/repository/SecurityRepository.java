package com.sure.account.repository;

import com.sure.account.entity.Security;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SecurityRepository extends JpaRepository<Security, String> {
    Optional<Security> findByTicker(String ticker);
}
