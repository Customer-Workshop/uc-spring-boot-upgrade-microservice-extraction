package io.spring.articleservice.client;

import io.spring.common.data.ProfileData;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class UserServiceClient {

  private final RestTemplate restTemplate;
  private final String userServiceUrl;

  public UserServiceClient(
      RestTemplate restTemplate,
      @Value("${user-service.url}") String userServiceUrl) {
    this.restTemplate = restTemplate;
    this.userServiceUrl = userServiceUrl;
  }

  public Optional<ProfileData> getProfileByUserId(String userId) {
    try {
      ResponseEntity<ProfileData> response =
          restTemplate.getForEntity(
              userServiceUrl + "/internal/users/{userId}/profile", ProfileData.class, userId);
      return Optional.ofNullable(response.getBody());
    } catch (Exception e) {
      return Optional.empty();
    }
  }

  public boolean isUserFollowing(String userId, String anotherUserId) {
    try {
      ResponseEntity<Boolean> response =
          restTemplate.getForEntity(
              userServiceUrl + "/internal/users/{userId}/following/{anotherUserId}",
              Boolean.class,
              userId,
              anotherUserId);
      return Boolean.TRUE.equals(response.getBody());
    } catch (Exception e) {
      return false;
    }
  }

  public Set<String> followingAuthors(String userId, List<String> authorIds) {
    try {
      ResponseEntity<Set<String>> response =
          restTemplate.exchange(
              userServiceUrl + "/internal/users/{userId}/following-authors?ids={ids}",
              HttpMethod.GET,
              null,
              new ParameterizedTypeReference<Set<String>>() {},
              userId,
              String.join(",", authorIds));
      Set<String> body = response.getBody();
      return body != null ? body : Collections.emptySet();
    } catch (Exception e) {
      return Collections.emptySet();
    }
  }

  public List<String> followedUsers(String userId) {
    try {
      ResponseEntity<List<String>> response =
          restTemplate.exchange(
              userServiceUrl + "/internal/users/{userId}/followed",
              HttpMethod.GET,
              null,
              new ParameterizedTypeReference<List<String>>() {},
              userId);
      List<String> body = response.getBody();
      return body != null ? body : Collections.emptyList();
    } catch (Exception e) {
      return Collections.emptyList();
    }
  }
}
