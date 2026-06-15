package io.spring.infrastructure.client;

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

  @lombok.Data
  @lombok.NoArgsConstructor
  private static class UserProfileResponse {
    private UserProfileDto user;
  }
}
