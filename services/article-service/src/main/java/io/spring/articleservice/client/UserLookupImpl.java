package io.spring.articleservice.client;

import io.spring.articleservice.core.user.User;
import io.spring.common.security.UserLookup;
import java.util.Map;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class UserLookupImpl implements UserLookup {

  private final RestTemplate restTemplate;
  private final String userServiceUrl;

  public UserLookupImpl(
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
              userServiceUrl + "/internal/users/{userId}", Map.class, userId);
      if (response == null) {
        return Optional.empty();
      }
      User user =
          new User(
              (String) response.get("id"),
              (String) response.get("email"),
              (String) response.get("username"),
              (String) response.get("bio"),
              (String) response.get("image"));
      return Optional.of(user);
    } catch (Exception e) {
      return Optional.empty();
    }
  }
}
