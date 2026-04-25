package com.sure.budget.repository;

import com.sure.budget.entity.Category;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, String> {
    List<Category> findByFamilyId(String familyId);

    List<Category> findByFamilyIdAndParentIdIsNull(String familyId);
}
