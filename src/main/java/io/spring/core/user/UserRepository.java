package io.spring.core.user;

import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository {
  void save(User user);

  Optional<User> findById(String id);

  Optional<User> findByUsername(String username);

  Optional<User> findByEmail(String email);

  // TODO: Follow-related methods below have been extracted to the Profile Service (profile-service/).
  // Replace these with HTTP calls to the Profile Service's internal APIs.
  // See: profile-service/src/main/java/io/spring/profileservice/api/InternalFollowApi.java
  void saveRelation(FollowRelation followRelation);

  Optional<FollowRelation> findRelation(String userId, String targetId);

  void removeRelation(FollowRelation followRelation);
}
