package com.sure.budget.repository;

import com.sure.budget.entity.Merchant;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MerchantRepository extends JpaRepository<Merchant, UUID> {
    Optional<Merchant> findByName(String name);
}
