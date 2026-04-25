package com.sure.budget.repository;

import com.sure.budget.entity.Budget;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BudgetRepository extends JpaRepository<Budget, String> {
    List<Budget> findByFamilyId(String familyId);
}
