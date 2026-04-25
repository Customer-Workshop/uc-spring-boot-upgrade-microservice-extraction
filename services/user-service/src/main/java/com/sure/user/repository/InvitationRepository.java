package com.sure.user.repository;

import com.sure.user.entity.Invitation;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InvitationRepository extends JpaRepository<Invitation, String> {
    Optional<Invitation> findByToken(String token);

    List<Invitation> findByFamilyIdAndAcceptedAtIsNull(String familyId);
}
