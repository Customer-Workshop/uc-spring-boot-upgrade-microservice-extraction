package io.spring.core.user;

import java.util.Optional;
import org.springframework.stereotype.Repository;

/** Repository interface for {@link User} aggregate and {@link FollowRelation} persistence. */
@Repository
public interface UserRepository {

  /**
   * Persists a user. Creates a new record if the user does not exist, or updates the existing
   * record.
   *
   * @param user the user entity to save
   */
  void save(User user);

  /**
   * Finds a user by unique identifier.
   *
   * @param id the user's unique identifier
   * @return the user, or empty if not found
   */
  Optional<User> findById(String id);

  /**
   * Finds a user by username.
   *
   * @param username the username to search for
   * @return the user, or empty if not found
   */
  Optional<User> findByUsername(String username);

  /**
   * Finds a user by email address.
   *
   * @param email the email to search for
   * @return the user, or empty if not found
   */
  Optional<User> findByEmail(String email);

  /**
   * Persists a follow relationship. Duplicate relations are silently ignored.
   *
   * @param followRelation the follow relationship to save
   */
  void saveRelation(FollowRelation followRelation);

  /**
   * Finds a follow relationship between two users.
   *
   * @param userId the follower's unique identifier
   * @param targetId the followed user's unique identifier
   * @return the follow relationship, or empty if not following
   */
  Optional<FollowRelation> findRelation(String userId, String targetId);

  /**
   * Removes a follow relationship.
   *
   * @param followRelation the follow relationship to remove
   */
  void removeRelation(FollowRelation followRelation);
}
