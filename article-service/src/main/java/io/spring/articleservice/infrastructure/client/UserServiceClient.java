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
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class UserServiceClient {

  private static final Logger log = LoggerFactory.getLogger(UserServiceClient.class);

  private static final String API_KEY_HEADER = "X-Internal-Api-Key";

  private final RestTemplate restTemplate;
  private final String userServiceUrl;
  private final String internalApiKey;

  public UserServiceClient(
      RestTemplate restTemplate,
      @Value("${user-service.url:http://localhost:8080}") String userServiceUrl,
      @Value("${internal.api.key:default-internal-key}") String internalApiKey) {
    this.restTemplate = restTemplate;
    this.userServiceUrl = userServiceUrl;
    this.internalApiKey = internalApiKey;
  }

  private HttpHeaders createHeaders() {
    HttpHeaders headers = new HttpHeaders();
    headers.set(API_KEY_HEADER, internalApiKey);
    return headers;
  }

  public ProfileData getProfile(String userId) {
    try {
      ResponseEntity<UserProfileResponse> response =
          restTemplate.exchange(
              userServiceUrl + "/api/internal/users/{id}/profile",
              HttpMethod.GET,
              new HttpEntity<>(createHeaders()),
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
          restTemplate.exchange(
              userServiceUrl + "/api/internal/users/{userId}/following/{targetId}",
              HttpMethod.GET,
              new HttpEntity<>(createHeaders()),
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
      HttpHeaders headers = createHeaders();
      ResponseEntity<List<String>> response =
          restTemplate.exchange(
              userServiceUrl + "/api/internal/users/following-authors",
              HttpMethod.POST,
              new HttpEntity<>(request, headers),
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

  public String getUserIdByUsername(String username) {
    try {
      ResponseEntity<UserProfileResponse> response =
          restTemplate.exchange(
              userServiceUrl + "/api/internal/users/by-username/{username}",
              HttpMethod.GET,
              new HttpEntity<>(createHeaders()),
              UserProfileResponse.class,
              username);
      UserProfileResponse body = response.getBody();
      if (body != null) {
        return body.getId();
      }
    } catch (Exception e) {
      log.warn("Failed to resolve username={} to userId: {}", username, e.getMessage());
    }
    return null;
  }

  public List<String> followedUsers(String userId) {
    try {
      ResponseEntity<List<String>> response =
          restTemplate.exchange(
              userServiceUrl + "/api/internal/users/{userId}/followed",
              HttpMethod.GET,
              new HttpEntity<>(createHeaders()),
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
