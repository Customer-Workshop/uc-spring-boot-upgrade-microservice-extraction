package com.sure.user.repository;

import com.sure.user.entity.Family;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FamilyRepository extends JpaRepository<Family, UUID> {}
