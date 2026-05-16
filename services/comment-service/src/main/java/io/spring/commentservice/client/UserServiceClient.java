package io.spring.commentservice.client;

import io.spring.common.security.UserLookup;
import java.util.Map;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Component
public class UserServiceClient implements UserLookup {

  private final RestTemplate restTemplate;
  private final String userServiceUrl;

  public UserServiceClient(
      RestTemplate restTemplate,
      @Value("${user-service.url}") String userServiceUrl) {
    this.restTemplate = restTemplate;
    this.userServiceUrl = userServiceUrl;
  }

  @Override
  @SuppressWarnings("unchecked")
  public Optional<Object> findById(String userId) {
    try {
      Map<String, Object> response =
          restTemplate.getForObject(
              userServiceUrl + "/users/{userId}", Map.class, userId);
      if (response != null && response.containsKey("user")) {
        return Optional.of(response.get("user"));
      }
      return Optional.empty();
    } catch (HttpClientErrorException.NotFound e) {
      return Optional.empty();
    }
  }
}
