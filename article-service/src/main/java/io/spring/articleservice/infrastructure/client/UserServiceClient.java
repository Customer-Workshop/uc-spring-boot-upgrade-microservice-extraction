package io.spring.articleservice.infrastructure.client;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Component
public class UserServiceClient {
  private final RestTemplate restTemplate;
  private final String userServiceUrl;

  public UserServiceClient(
      RestTemplate restTemplate, @Value("${user-service.url}") String userServiceUrl) {
    this.restTemplate = restTemplate;
    this.userServiceUrl = userServiceUrl;
  }

  public Optional<UserProfileResponse> getUserProfile(String userId) {
    try {
      String url = userServiceUrl + "/api/internal/users/" + userId + "/profile";
      ResponseEntity<UserProfileResponse> response =
          restTemplate.getForEntity(url, UserProfileResponse.class);
      return Optional.ofNullable(response.getBody());
    } catch (Exception e) {
      return Optional.empty();
    }
  }

  public Map<String, UserProfileResponse> getUserProfiles(List<String> userIds) {
    if (userIds == null || userIds.isEmpty()) {
      return Collections.emptyMap();
    }
    try {
      String url =
          UriComponentsBuilder.fromHttpUrl(userServiceUrl + "/api/internal/users/profiles")
              .queryParam("ids", String.join(",", userIds))
              .toUriString();
      ResponseEntity<Map<String, UserProfileResponse>> response =
          restTemplate.exchange(
              url,
              HttpMethod.GET,
              null,
              new ParameterizedTypeReference<Map<String, UserProfileResponse>>() {});
      Map<String, UserProfileResponse> body = response.getBody();
      return body != null ? body : Collections.emptyMap();
    } catch (Exception e) {
      return Collections.emptyMap();
    }
  }

  public boolean isUserFollowing(String userId, String targetId) {
    try {
      String url = userServiceUrl + "/api/internal/users/" + userId + "/following/" + targetId;
      ResponseEntity<Map<String, Boolean>> response =
          restTemplate.exchange(
              url, HttpMethod.GET, null, new ParameterizedTypeReference<Map<String, Boolean>>() {});
      Map<String, Boolean> body = response.getBody();
      return body != null && Boolean.TRUE.equals(body.get("following"));
    } catch (Exception e) {
      return false;
    }
  }

  public Set<String> followingAuthors(String userId, List<String> authorIds) {
    if (authorIds == null || authorIds.isEmpty()) {
      return Collections.emptySet();
    }
    try {
      String url =
          UriComponentsBuilder.fromHttpUrl(
                  userServiceUrl + "/api/internal/users/" + userId + "/following")
              .queryParam("ids", String.join(",", authorIds))
              .toUriString();
      ResponseEntity<UserFollowingResponse> response =
          restTemplate.getForEntity(url, UserFollowingResponse.class);
      UserFollowingResponse body = response.getBody();
      return body != null && body.getFollowingIds() != null
          ? body.getFollowingIds()
          : Collections.emptySet();
    } catch (Exception e) {
      return Collections.emptySet();
    }
  }

  public List<String> followedUsers(String userId) {
    try {
      String url = userServiceUrl + "/api/internal/users/" + userId + "/followed";
      ResponseEntity<List<String>> response =
          restTemplate.exchange(
              url, HttpMethod.GET, null, new ParameterizedTypeReference<List<String>>() {});
      List<String> body = response.getBody();
      return body != null ? body : Collections.emptyList();
    } catch (Exception e) {
      return Collections.emptyList();
    }
  }

  public Optional<String> getUsernameById(String userId) {
    return getUserProfile(userId).map(UserProfileResponse::getUsername);
  }

  public Optional<String> getUserIdByUsername(String username) {
    try {
      String url = userServiceUrl + "/api/internal/users/by-username/" + username;
      ResponseEntity<UserProfileResponse> response =
          restTemplate.getForEntity(url, UserProfileResponse.class);
      return Optional.ofNullable(response.getBody()).map(UserProfileResponse::getId);
    } catch (Exception e) {
      return Optional.empty();
    }
  }
}
