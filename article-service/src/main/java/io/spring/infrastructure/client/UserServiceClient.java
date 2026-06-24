package io.spring.infrastructure.client;

import io.spring.core.user.User;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class UserServiceClient {

  private final RestTemplate restTemplate;
  private final String monolithBaseUrl;

  public UserServiceClient(@Value("${monolith.base-url}") String monolithBaseUrl) {
    this.restTemplate = new RestTemplate();
    this.monolithBaseUrl = monolithBaseUrl;
  }

  public Optional<User> findUserById(String userId) {
    try {
      Map<String, Object> response =
          restTemplate.getForObject(
              monolithBaseUrl + "/api/internal/users/{id}", Map.class, userId);
      if (response == null) {
        return Optional.empty();
      }
      return Optional.of(mapToUser(response));
    } catch (Exception e) {
      return Optional.empty();
    }
  }

  public Optional<User> findUserByUsername(String username) {
    try {
      Map<String, Object> response =
          restTemplate.getForObject(
              monolithBaseUrl + "/api/internal/users/by-username/{username}", Map.class, username);
      if (response == null) {
        return Optional.empty();
      }
      return Optional.of(mapToUser(response));
    } catch (Exception e) {
      return Optional.empty();
    }
  }

  public boolean isUserFollowing(String userId, String targetUserId) {
    try {
      Map<String, Object> response =
          restTemplate.getForObject(
              monolithBaseUrl + "/api/internal/users/{id}/following/{targetId}",
              Map.class,
              userId,
              targetUserId);
      if (response == null) {
        return false;
      }
      return Boolean.TRUE.equals(response.get("following"));
    } catch (Exception e) {
      return false;
    }
  }

  public Set<String> followingAuthors(String userId, List<String> authorIds) {
    return Set.of();
  }

  public List<String> followedUsers(String userId) {
    return List.of();
  }

  private User mapToUser(Map<String, Object> data) {
    return new User(
        (String) data.getOrDefault("id", ""),
        (String) data.getOrDefault("email", ""),
        (String) data.getOrDefault("username", ""),
        (String) data.getOrDefault("bio", ""),
        (String) data.getOrDefault("image", ""));
  }
}
