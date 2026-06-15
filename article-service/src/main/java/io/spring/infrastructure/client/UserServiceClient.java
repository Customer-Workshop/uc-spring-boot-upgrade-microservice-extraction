package io.spring.infrastructure.client;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Component
public class UserServiceClient {

  private static final Logger logger = LoggerFactory.getLogger(UserServiceClient.class);

  private final RestTemplate restTemplate;
  private final String userServiceUrl;

  public UserServiceClient(
      RestTemplate restTemplate, @Value("${user-service.url}") String userServiceUrl) {
    this.restTemplate = restTemplate;
    this.userServiceUrl = userServiceUrl;
  }

  public Optional<UserProfileDto> findUserById(String userId) {
    try {
      String url = userServiceUrl + "/users/" + userId;
      UserProfileResponse response = restTemplate.getForObject(url, UserProfileResponse.class);
      if (response != null && response.getUser() != null) {
        return Optional.of(response.getUser());
      }
      return Optional.empty();
    } catch (RestClientException e) {
      logger.warn("Failed to fetch user profile for userId={}: {}", userId, e.getMessage());
      return Optional.empty();
    }
  }

  public List<String> getFollowedUserIds(String userId) {
    try {
      String url = userServiceUrl + "/users/" + userId + "/following";
      FollowedUsersResponse response = restTemplate.getForObject(url, FollowedUsersResponse.class);
      if (response != null && response.getFollowedUserIds() != null) {
        return response.getFollowedUserIds();
      }
      return Collections.emptyList();
    } catch (RestClientException e) {
      logger.warn("Failed to fetch followed users for userId={}: {}", userId, e.getMessage());
      return Collections.emptyList();
    }
  }

  public Optional<String> findUserIdByUsername(String username) {
    try {
      String url = userServiceUrl + "/profiles/" + username;
      ProfileResponse response = restTemplate.getForObject(url, ProfileResponse.class);
      if (response != null && response.getProfile() != null) {
        return Optional.ofNullable(response.getProfile().getId());
      }
      return Optional.empty();
    } catch (RestClientException e) {
      logger.warn("Failed to resolve username={}: {}", username, e.getMessage());
      return Optional.empty();
    }
  }

  @lombok.Data
  @lombok.NoArgsConstructor
  private static class UserProfileResponse {
    private UserProfileDto user;
  }

  @lombok.Data
  @lombok.NoArgsConstructor
  private static class FollowedUsersResponse {
    private List<String> followedUserIds;
  }

  @lombok.Data
  @lombok.NoArgsConstructor
  private static class ProfileResponse {
    private UserProfileDto profile;
  }
}
