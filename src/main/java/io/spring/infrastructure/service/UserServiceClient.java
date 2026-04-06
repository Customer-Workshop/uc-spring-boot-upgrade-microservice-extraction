package io.spring.infrastructure.service;

import io.spring.core.user.FollowRelation;
import io.spring.core.user.User;
import io.spring.core.user.UserRepository;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Repository
public class UserServiceClient implements UserRepository {

  private final RestTemplate restTemplate;
  private final String userServiceUrl;

  public UserServiceClient(
      RestTemplate restTemplate,
      @Value("${user-service.url}") String userServiceUrl) {
    this.restTemplate = restTemplate;
    this.userServiceUrl = userServiceUrl;
  }

  @Override
  public void save(User user) {
    // User persistence is handled by user-service directly.
    // This method is not called from the monolith after extraction.
    throw new UnsupportedOperationException("User persistence is handled by user-service");
  }

  @Override
  public Optional<User> findById(String id) {
    try {
      User user =
          restTemplate.getForObject(
              userServiceUrl + "/api/internal/users/{userId}", User.class, id);
      return Optional.ofNullable(user);
    } catch (HttpClientErrorException.NotFound e) {
      return Optional.empty();
    }
  }

  @Override
  public Optional<User> findByUsername(String username) {
    try {
      User user =
          restTemplate.getForObject(
              userServiceUrl + "/api/internal/users/by-username/{username}",
              User.class,
              username);
      return Optional.ofNullable(user);
    } catch (HttpClientErrorException.NotFound e) {
      return Optional.empty();
    }
  }

  @Override
  public Optional<User> findByEmail(String email) {
    try {
      User user =
          restTemplate.getForObject(
              userServiceUrl + "/api/internal/users/by-email/{email}", User.class, email);
      return Optional.ofNullable(user);
    } catch (HttpClientErrorException.NotFound e) {
      return Optional.empty();
    }
  }

  @Override
  public void saveRelation(FollowRelation followRelation) {
    // Follow operations are handled by user-service directly via profile API.
    throw new UnsupportedOperationException("Follow operations are handled by user-service");
  }

  @Override
  public Optional<FollowRelation> findRelation(String userId, String targetId) {
    try {
      Boolean following =
          restTemplate.getForObject(
              userServiceUrl + "/api/internal/users/{userId}/following/{targetUserId}",
              Boolean.class,
              userId,
              targetId);
      if (Boolean.TRUE.equals(following)) {
        return Optional.of(new FollowRelation(userId, targetId));
      }
      return Optional.empty();
    } catch (HttpClientErrorException.NotFound e) {
      return Optional.empty();
    }
  }

  @Override
  public void removeRelation(FollowRelation followRelation) {
    // Follow operations are handled by user-service directly via profile API.
    throw new UnsupportedOperationException("Follow operations are handled by user-service");
  }
}
