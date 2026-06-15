package io.spring.infrastructure.client;

import io.spring.application.data.ProfileData;
import io.spring.infrastructure.client.dto.UserProfileResponse;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class UserServiceClient {

  private final RestTemplate restTemplate;
  private final String userServiceUrl;

  public UserServiceClient(
      RestTemplate restTemplate,
      @Value("${user-service.url:http://localhost:8080}") String userServiceUrl) {
    this.restTemplate = restTemplate;
    this.userServiceUrl = userServiceUrl;
  }

  public ProfileData getProfile(String userId) {
    try {
      ResponseEntity<UserProfileResponse> response =
          restTemplate.getForEntity(
              userServiceUrl + "/internal/users/{id}/profile", UserProfileResponse.class, userId);
      UserProfileResponse body = response.getBody();
      if (body == null) {
        return defaultProfile(userId);
      }
      return new ProfileData(
          body.getId(), body.getUsername(), body.getBio(), body.getImage(), false);
    } catch (Exception e) {
      return defaultProfile(userId);
    }
  }

  public Map<String, ProfileData> getProfiles(List<String> userIds) {
    if (userIds == null || userIds.isEmpty()) {
      return Collections.emptyMap();
    }
    try {
      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.APPLICATION_JSON);
      HttpEntity<List<String>> request = new HttpEntity<>(userIds, headers);
      ResponseEntity<UserProfileResponse[]> response =
          restTemplate.exchange(
              userServiceUrl + "/internal/users/profiles",
              HttpMethod.POST,
              request,
              UserProfileResponse[].class);
      UserProfileResponse[] bodies = response.getBody();
      if (bodies == null) {
        return Collections.emptyMap();
      }
      Map<String, ProfileData> result = new HashMap<>();
      for (UserProfileResponse body : bodies) {
        result.put(
            body.getId(),
            new ProfileData(
                body.getId(), body.getUsername(), body.getBio(), body.getImage(), false));
      }
      return result;
    } catch (Exception e) {
      return Collections.emptyMap();
    }
  }

  public boolean isFollowing(String userId, String targetId) {
    try {
      ResponseEntity<Boolean> response =
          restTemplate.getForEntity(
              userServiceUrl + "/internal/users/{userId}/following/{targetId}",
              Boolean.class,
              userId,
              targetId);
      return Boolean.TRUE.equals(response.getBody());
    } catch (Exception e) {
      return false;
    }
  }

  public Set<String> getFollowingAuthors(String userId, List<String> authorIds) {
    if (authorIds == null || authorIds.isEmpty()) {
      return Collections.emptySet();
    }
    try {
      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.APPLICATION_JSON);
      HttpEntity<List<String>> request = new HttpEntity<>(authorIds, headers);
      ResponseEntity<String[]> response =
          restTemplate.exchange(
              userServiceUrl + "/internal/users/{userId}/following-authors",
              HttpMethod.POST,
              request,
              String[].class,
              userId);
      String[] body = response.getBody();
      if (body == null) {
        return Collections.emptySet();
      }
      return new HashSet<>(Arrays.asList(body));
    } catch (Exception e) {
      return Collections.emptySet();
    }
  }

  public List<String> getFollowedUserIds(String userId) {
    try {
      ResponseEntity<String[]> response =
          restTemplate.getForEntity(
              userServiceUrl + "/internal/users/{userId}/followed", String[].class, userId);
      String[] body = response.getBody();
      if (body == null) {
        return Collections.emptyList();
      }
      return Arrays.asList(body);
    } catch (Exception e) {
      return Collections.emptyList();
    }
  }

  private ProfileData defaultProfile(String userId) {
    return new ProfileData(userId, "unknown", "", "", false);
  }
}
