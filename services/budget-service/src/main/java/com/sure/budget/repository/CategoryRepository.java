package com.sure.budget.repository;

import com.sure.budget.entity.Category;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, UUID> {
    List<Category> findByFamilyId(UUID familyId);

    List<Category> findByFamilyIdAndParentIdIsNull(UUID familyId);
}
