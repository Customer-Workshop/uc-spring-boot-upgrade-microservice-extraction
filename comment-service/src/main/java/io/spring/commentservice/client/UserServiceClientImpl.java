package io.spring.commentservice.client;

import java.util.Optional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Component
public class UserServiceClientImpl implements UserServiceClient {

  private final RestTemplate restTemplate;
  private final String userServiceUrl;

  public UserServiceClientImpl(
      RestTemplate restTemplate, @Value("${user-service.url}") String userServiceUrl) {
    this.restTemplate = restTemplate;
    this.userServiceUrl = userServiceUrl;
  }

  @Override
  public Optional<UserResponse> getUserById(String userId) {
    try {
      UserResponse response =
          restTemplate.getForObject(
              userServiceUrl + "/api/internal/users/{id}", UserResponse.class, userId);
      return Optional.ofNullable(response);
    } catch (RestClientException e) {
      return Optional.empty();
    }
  }

  @Override
  public boolean isFollowing(String userId, String targetUserId) {
    try {
      Boolean result =
          restTemplate.getForObject(
              userServiceUrl + "/api/internal/users/{userId}/following/{targetUserId}",
              Boolean.class,
              userId,
              targetUserId);
      return result != null && result;
    } catch (RestClientException e) {
      return false;
    }
  }
}
