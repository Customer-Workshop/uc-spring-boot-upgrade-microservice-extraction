package io.spring.core.user;

import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository {
  /** Saves a new user or persists changes to an existing user. */
  void save(User user);

  /** Finds a user by identifier. */
  Optional<User> findById(String id);

  /** Finds a user by username. */
  Optional<User> findByUsername(String username);

  /** Finds a user by email address. */
  Optional<User> findByEmail(String email);

  /** Saves a follow relationship when it does not already exist. */
  void saveRelation(FollowRelation followRelation);

  /** Finds a follow relationship between two users. */
  Optional<FollowRelation> findRelation(String userId, String targetId);

  /** Removes a follow relationship. */
  void removeRelation(FollowRelation followRelation);
}
