package com.sure.account.repository;

import com.sure.account.entity.Account;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountRepository extends JpaRepository<Account, String> {
    List<Account> findByFamilyIdAndActiveTrue(String familyId);

    List<Account> findByFamilyId(String familyId);

    List<Account> findByFamilyIdAndAccountType(String familyId, String accountType);
}
