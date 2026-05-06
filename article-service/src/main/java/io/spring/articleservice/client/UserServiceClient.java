package io.spring.articleservice.client;

import io.spring.articleservice.client.dto.UserResponse;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
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

  public Optional<UserResponse> getUserById(String userId) {
    try {
      UserResponse response =
          restTemplate.getForObject(
              userServiceUrl + "/internal/users/" + userId, UserResponse.class);
      return Optional.ofNullable(response);
    } catch (Exception e) {
      logger.warn("Failed to fetch user {}: {}", userId, e.getMessage());
      return Optional.empty();
    }
  }

  public Optional<UserResponse> getUserByUsername(String username) {
    try {
      UserResponse response =
          restTemplate.getForObject(
              userServiceUrl + "/internal/users/by-username/" + username, UserResponse.class);
      return Optional.ofNullable(response);
    } catch (Exception e) {
      logger.warn("Failed to fetch user by username {}: {}", username, e.getMessage());
      return Optional.empty();
    }
  }

  public Optional<UserResponse> getCurrentUser(String token) {
    try {
      HttpHeaders headers = new HttpHeaders();
      headers.set("Authorization", "Token " + token);
      HttpEntity<Void> entity = new HttpEntity<>(headers);
      var response =
          restTemplate.exchange(
              userServiceUrl + "/user", HttpMethod.GET, entity, UserResponse.class);
      return Optional.ofNullable(response.getBody());
    } catch (Exception e) {
      logger.warn("Failed to fetch current user: {}", e.getMessage());
      return Optional.empty();
    }
  }
}
