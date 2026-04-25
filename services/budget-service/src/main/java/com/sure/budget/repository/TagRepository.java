package com.sure.budget.repository;

import com.sure.budget.entity.Tag;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TagRepository extends JpaRepository<Tag, String> {
    List<Tag> findByFamilyId(String familyId);
}
