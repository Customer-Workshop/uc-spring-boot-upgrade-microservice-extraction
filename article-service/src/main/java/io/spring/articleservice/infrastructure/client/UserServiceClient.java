package io.spring.articleservice.infrastructure.client;

import io.spring.articleservice.application.data.ProfileData;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class UserServiceClient {

  private static final Logger log = LoggerFactory.getLogger(UserServiceClient.class);

  private final RestTemplate restTemplate;
  private final String userServiceUrl;

  public UserServiceClient(
      RestTemplate restTemplate,
      @Value("${user-service.url:http://localhost:8080}") String userServiceUrl) {
    this.restTemplate = restTemplate;
    this.userServiceUrl = userServiceUrl;
  }

  public ProfileData getProfile(String userId) {
    try {
      ResponseEntity<UserProfileResponse> response =
          restTemplate.getForEntity(
              userServiceUrl + "/api/internal/users/{id}/profile",
              UserProfileResponse.class,
              userId);
      UserProfileResponse body = response.getBody();
      if (body != null) {
        return new ProfileData(
            body.getId(), body.getUsername(), body.getBio(), body.getImage(), false);
      }
    } catch (Exception e) {
      log.warn("Failed to fetch user profile for userId={}: {}", userId, e.getMessage());
    }
    return new ProfileData(userId, "unknown", null, null, false);
  }

  public boolean isUserFollowing(String userId, String targetId) {
    try {
      ResponseEntity<Boolean> response =
          restTemplate.getForEntity(
              userServiceUrl + "/api/internal/users/{userId}/following/{targetId}",
              Boolean.class,
              userId,
              targetId);
      return Boolean.TRUE.equals(response.getBody());
    } catch (Exception e) {
      log.warn(
          "Failed to check follow status userId={}, targetId={}: {}",
          userId,
          targetId,
          e.getMessage());
    }
    return false;
  }

  public Set<String> followingAuthors(String userId, List<String> authorIds) {
    try {
      FollowingAuthorsRequest request = new FollowingAuthorsRequest(userId, authorIds);
      ResponseEntity<List<String>> response =
          restTemplate.exchange(
              userServiceUrl + "/api/internal/users/following-authors",
              HttpMethod.POST,
              new HttpEntity<>(request),
              new ParameterizedTypeReference<List<String>>() {});
      List<String> body = response.getBody();
      if (body != null) {
        return body.stream().collect(Collectors.toSet());
      }
    } catch (Exception e) {
      log.warn("Failed to fetch following authors for userId={}: {}", userId, e.getMessage());
    }
    return Collections.emptySet();
  }

  public List<String> followedUsers(String userId) {
    try {
      ResponseEntity<List<String>> response =
          restTemplate.exchange(
              userServiceUrl + "/api/internal/users/{userId}/followed",
              HttpMethod.GET,
              null,
              new ParameterizedTypeReference<List<String>>() {},
              userId);
      List<String> body = response.getBody();
      if (body != null) {
        return body;
      }
    } catch (Exception e) {
      log.warn("Failed to fetch followed users for userId={}: {}", userId, e.getMessage());
    }
    return Collections.emptyList();
  }
}
