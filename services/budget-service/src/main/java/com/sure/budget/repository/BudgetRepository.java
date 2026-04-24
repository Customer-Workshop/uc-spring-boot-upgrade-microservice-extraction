package com.sure.budget.repository;

import com.sure.budget.entity.Budget;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BudgetRepository extends JpaRepository<Budget, UUID> {
    List<Budget> findByFamilyId(UUID familyId);
}
