package com.sure.user.repository;

import com.sure.user.entity.Session;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SessionRepository extends JpaRepository<Session, String> {
    List<Session> findByUserIdAndActiveTrue(String userId);
}
