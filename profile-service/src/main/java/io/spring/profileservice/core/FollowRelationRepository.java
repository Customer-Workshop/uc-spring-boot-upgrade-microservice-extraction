package io.spring.profileservice.core;

import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public interface FollowRelationRepository {

  void saveRelation(FollowRelation followRelation);

  Optional<FollowRelation> findRelation(String userId, String targetId);

  void removeRelation(FollowRelation followRelation);
}
