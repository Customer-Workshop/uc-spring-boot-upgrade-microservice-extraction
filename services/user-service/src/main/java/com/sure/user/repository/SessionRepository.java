package com.sure.user.repository;

import com.sure.user.entity.Session;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SessionRepository extends JpaRepository<Session, UUID> {
    List<Session> findByUserIdAndActiveTrue(UUID userId);
}
