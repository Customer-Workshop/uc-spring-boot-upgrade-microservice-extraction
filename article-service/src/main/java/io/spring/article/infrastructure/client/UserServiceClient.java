package io.spring.article.infrastructure.client;

import io.spring.article.application.data.ProfileData;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Component
public class UserServiceClient {

  private final RestTemplate restTemplate;
  private final String userServiceUrl;

  public UserServiceClient(@Value("${user-service.url}") String userServiceUrl) {
    this.restTemplate = new RestTemplate();
    this.userServiceUrl = userServiceUrl;
  }

  public Optional<ProfileData> getProfileByUserId(String userId) {
    try {
      ProfileData profile =
          restTemplate.getForObject(
              userServiceUrl + "/api/users/{userId}/profile", ProfileData.class, userId);
      return Optional.ofNullable(profile);
    } catch (RestClientException e) {
      return Optional.empty();
    }
  }

  public List<String> getFollowedUsers(String userId) {
    try {
      String[] followedIds =
          restTemplate.getForObject(
              userServiceUrl + "/api/users/{userId}/following", String[].class, userId);
      if (followedIds != null) {
        return List.of(followedIds);
      }
      return Collections.emptyList();
    } catch (RestClientException e) {
      return Collections.emptyList();
    }
  }

  public boolean isFollowing(String userId, String targetUserId) {
    try {
      Boolean result =
          restTemplate.getForObject(
              userServiceUrl + "/api/users/{userId}/following/{targetId}",
              Boolean.class,
              userId,
              targetUserId);
      return result != null && result;
    } catch (RestClientException e) {
      return false;
    }
  }
}
