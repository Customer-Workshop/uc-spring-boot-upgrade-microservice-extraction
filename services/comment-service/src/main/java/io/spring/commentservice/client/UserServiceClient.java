package io.spring.commentservice.client;

import io.spring.common.data.ProfileData;
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

  @SuppressWarnings("unchecked")
  public Optional<ProfileData> getProfileByUserId(String userId) {
    try {
      Map<String, Object> response =
          restTemplate.getForObject(
              userServiceUrl + "/users/{userId}", Map.class, userId);
      if (response != null && response.containsKey("user")) {
        Map<String, Object> userData = (Map<String, Object>) response.get("user");
        ProfileData profile = new ProfileData(
            (String) userData.get("id"),
            (String) userData.get("username"),
            (String) userData.get("bio"),
            (String) userData.get("image"),
            false);
        return Optional.of(profile);
      }
      return Optional.empty();
    } catch (HttpClientErrorException.NotFound e) {
      return Optional.empty();
    }
  }
}
