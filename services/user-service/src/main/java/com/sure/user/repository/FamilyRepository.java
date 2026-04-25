package com.sure.user.repository;

import com.sure.user.entity.Family;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FamilyRepository extends JpaRepository<Family, String> {}
