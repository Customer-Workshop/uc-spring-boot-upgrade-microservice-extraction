package io.spring.core.service;

import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class UserServiceClient {
  private static final Logger log = LoggerFactory.getLogger(UserServiceClient.class);

  private final RestTemplate restTemplate;
  private final String userServiceUrl;

  public UserServiceClient(
      RestTemplate restTemplate, @Value("${user-service.url}") String userServiceUrl) {
    this.restTemplate = restTemplate;
    this.userServiceUrl = userServiceUrl;
  }

  public Optional<UserDto> findUserById(String userId) {
    try {
      UserDto user =
          restTemplate.getForObject(userServiceUrl + "/internal/users/" + userId, UserDto.class);
      return Optional.ofNullable(user);
    } catch (Exception e) {
      log.warn("Failed to fetch user {} from user service: {}", userId, e.getMessage());
      return Optional.empty();
    }
  }

  public Optional<UserDto> findUserByUsername(String username) {
    try {
      UserDto user =
          restTemplate.getForObject(
              userServiceUrl + "/internal/users/by-username/" + username, UserDto.class);
      return Optional.ofNullable(user);
    } catch (Exception e) {
      log.warn(
          "Failed to fetch user by username {} from user service: {}", username, e.getMessage());
      return Optional.empty();
    }
  }
}
