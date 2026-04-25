package com.sure.budget.repository;

import com.sure.budget.entity.Merchant;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MerchantRepository extends JpaRepository<Merchant, String> {
    Optional<Merchant> findByName(String name);
}
