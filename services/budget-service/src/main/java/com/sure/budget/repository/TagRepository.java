package com.sure.budget.repository;

import com.sure.budget.entity.Tag;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TagRepository extends JpaRepository<Tag, UUID> {
    List<Tag> findByFamilyId(UUID familyId);
}
