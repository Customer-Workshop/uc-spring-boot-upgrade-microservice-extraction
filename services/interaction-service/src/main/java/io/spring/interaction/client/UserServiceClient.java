package io.spring.interaction.client;

import io.spring.shared.dto.ProfileData;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
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
      ProfileData[] profiles =
          restTemplate.postForObject(
              userServiceUrl + "/internal/users/batch", userIds, ProfileData[].class);
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
}
