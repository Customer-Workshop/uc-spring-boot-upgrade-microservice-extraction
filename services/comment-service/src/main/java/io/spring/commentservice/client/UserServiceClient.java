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
              userServiceUrl + "/internal/users/{userId}", Map.class, userId);
      if (response != null) {
        return Optional.of(response);
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
              userServiceUrl + "/internal/users/{userId}", Map.class, userId);
      if (response != null) {
        ProfileData profile = new ProfileData(
            (String) response.get("id"),
            (String) response.get("username"),
            (String) response.get("bio"),
            (String) response.get("image"),
            false);
        return Optional.of(profile);
      }
      return Optional.empty();
    } catch (HttpClientErrorException.NotFound e) {
      return Optional.empty();
    }
  }
}
