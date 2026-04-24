package com.sure.account.repository;

import com.sure.account.entity.Account;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountRepository extends JpaRepository<Account, UUID> {
    List<Account> findByFamilyIdAndActiveTrue(UUID familyId);

    List<Account> findByFamilyId(UUID familyId);

    List<Account> findByFamilyIdAndAccountType(UUID familyId, String accountType);
}
