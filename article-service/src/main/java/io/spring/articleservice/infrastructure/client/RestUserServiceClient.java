package io.spring.articleservice.infrastructure.client;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Component
public class RestUserServiceClient implements UserServiceClient {

  private static final Logger log = LoggerFactory.getLogger(RestUserServiceClient.class);

  private final RestTemplate restTemplate;
  private final String userServiceUrl;

  public RestUserServiceClient(
      RestTemplate restTemplate, @Value("${user-service.url}") String userServiceUrl) {
    this.restTemplate = restTemplate;
    this.userServiceUrl = userServiceUrl;
  }

  @Override
  public UserProfileDto findUserProfileById(String userId) {
    try {
      ResponseEntity<UserProfileDto> response =
          restTemplate.getForEntity(
              userServiceUrl + "/api/users/{userId}/profile", UserProfileDto.class, userId);
      return response.getBody();
    } catch (RestClientException e) {
      log.warn("Failed to fetch user profile for userId={}: {}", userId, e.getMessage());
      return new UserProfileDto(userId, "unknown", "", "");
    }
  }

  @Override
  public boolean isUserFollowing(String userId, String targetUserId) {
    try {
      ResponseEntity<Boolean> response =
          restTemplate.getForEntity(
              userServiceUrl + "/api/users/{userId}/following?target={targetUserId}",
              Boolean.class,
              userId,
              targetUserId);
      return Boolean.TRUE.equals(response.getBody());
    } catch (RestClientException e) {
      log.warn("Failed to check follow status for userId={}: {}", userId, e.getMessage());
      return false;
    }
  }

  @Override
  public Set<String> followingAuthors(String userId, List<String> authorIds) {
    try {
      String ids = String.join(",", authorIds);
      ResponseEntity<String[]> response =
          restTemplate.getForEntity(
              userServiceUrl + "/api/users/{userId}/following-authors?ids={ids}",
              String[].class,
              userId,
              ids);
      String[] body = response.getBody();
      return body != null ? new HashSet<>(Arrays.asList(body)) : Collections.emptySet();
    } catch (RestClientException e) {
      log.warn("Failed to fetch following authors for userId={}: {}", userId, e.getMessage());
      return Collections.emptySet();
    }
  }

  @Override
  public List<String> followedUsers(String userId) {
    try {
      ResponseEntity<String[]> response =
          restTemplate.getForEntity(
              userServiceUrl + "/api/users/{userId}/followed-users",
              String[].class,
              userId);
      String[] body = response.getBody();
      return body != null ? Arrays.asList(body) : Collections.emptyList();
    } catch (RestClientException e) {
      log.warn("Failed to fetch followed users for userId={}: {}", userId, e.getMessage());
      return Collections.emptyList();
    }
  }
}
