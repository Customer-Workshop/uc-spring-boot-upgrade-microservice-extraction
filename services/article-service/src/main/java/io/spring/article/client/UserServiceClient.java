package io.spring.article.client;

import io.spring.shared.dto.ProfileData;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class UserServiceClient {
  private final RestTemplate restTemplate;
  private final String userServiceUrl;

  public UserServiceClient(
      RestTemplate restTemplate,
      @Value("${services.user-service.url:http://localhost:8081}") String userServiceUrl) {
    this.restTemplate = restTemplate;
    this.userServiceUrl = userServiceUrl;
  }

  public ProfileData getUserProfile(String userId) {
    try {
      return restTemplate.getForObject(
          userServiceUrl + "/internal/users/" + userId, ProfileData.class);
    } catch (Exception e) {
      return new ProfileData(userId, "unknown", "", "", false);
    }
  }

  public List<ProfileData> batchGetProfiles(List<String> userIds) {
    try {
      String ids = String.join(",", userIds);
      ProfileData[] profiles =
          restTemplate.getForObject(
              userServiceUrl + "/internal/users/batch?ids=" + ids, ProfileData[].class);
      return profiles != null ? Arrays.asList(profiles) : Collections.emptyList();
    } catch (Exception e) {
      return Collections.emptyList();
    }
  }

  public boolean isFollowing(String userId, String targetId) {
    try {
      Boolean result =
          restTemplate.getForObject(
              userServiceUrl + "/internal/users/" + userId + "/following/" + targetId,
              Boolean.class);
      return result != null && result;
    } catch (Exception e) {
      return false;
    }
  }

  public String getUserIdByUsername(String username) {
    try {
      ProfileData profile =
          restTemplate.getForObject(
              userServiceUrl + "/internal/users/by-username/" + username, ProfileData.class);
      return profile != null ? profile.getId() : null;
    } catch (Exception e) {
      return null;
    }
  }

  public List<String> getFollowedUsers(String userId) {
    try {
      String[] users =
          restTemplate.getForObject(
              userServiceUrl + "/internal/users/" + userId + "/followed-users", String[].class);
      return users != null ? Arrays.asList(users) : Collections.emptyList();
    } catch (Exception e) {
      return Collections.emptyList();
    }
  }
}
