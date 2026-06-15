package io.spring.articleservice.infrastructure.client;

import io.spring.articleservice.application.data.ProfileData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class UserServiceClient {

  private static final Logger logger = LoggerFactory.getLogger(UserServiceClient.class);

  private final RestTemplate restTemplate;
  private final String monolithBaseUrl;

  public UserServiceClient(
      RestTemplate restTemplate,
      @Value("${monolith.service.url:http://localhost:8080}") String monolithBaseUrl) {
    this.restTemplate = restTemplate;
    this.monolithBaseUrl = monolithBaseUrl;
  }

  public ProfileData getProfile(String userId) {
    try {
      String url = monolithBaseUrl + "/api/internal/profiles/" + userId;
      ResponseEntity<ProfileResponse> response =
          restTemplate.getForEntity(url, ProfileResponse.class);
      if (response.getBody() != null && response.getBody().getProfile() != null) {
        ProfileResponse.Profile p = response.getBody().getProfile();
        return new ProfileData(userId, p.getUsername(), p.getBio(), p.getImage(), false);
      }
    } catch (Exception e) {
      logger.warn("Failed to fetch profile for userId={}: {}", userId, e.getMessage());
    }
    return defaultProfile(userId);
  }

  public String resolveUsernameToUserId(String username) {
    try {
      String url = monolithBaseUrl + "/api/internal/users/by-username/" + username;
      ResponseEntity<UserLookupResponse> response =
          restTemplate.getForEntity(url, UserLookupResponse.class);
      if (response.getBody() != null && response.getBody().getId() != null) {
        return response.getBody().getId();
      }
    } catch (Exception e) {
      logger.warn("Failed to resolve username={}: {}", username, e.getMessage());
    }
    return null;
  }

  private ProfileData defaultProfile(String userId) {
    return new ProfileData(userId, userId, "", "", false);
  }

  @lombok.Data
  @lombok.NoArgsConstructor
  public static class ProfileResponse {
    private Profile profile;

    @lombok.Data
    @lombok.NoArgsConstructor
    public static class Profile {
      private String id;
      private String username;
      private String bio;
      private String image;
    }
  }

  @lombok.Data
  @lombok.NoArgsConstructor
  public static class UserLookupResponse {
    private String id;
    private String username;
  }
}
