package com.bank.rm.auth.repository;

import com.bank.rm.auth.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByEmail(String email);
    Optional<User> findByAnonymousSessionId(UUID anonymousSessionId);
    boolean existsByEmail(String email);
}
