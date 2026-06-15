package io.spring.articleservice.infrastructure.client;

import io.spring.articleservice.application.data.ProfileData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class UserServiceClient {

  private static final Logger logger = LoggerFactory.getLogger(UserServiceClient.class);
  private static final String API_KEY_HEADER = "X-Internal-Api-Key";

  private final RestTemplate restTemplate;
  private final String monolithBaseUrl;
  private final String internalApiKey;

  public UserServiceClient(
      RestTemplate restTemplate,
      @Value("${monolith.service.url:http://localhost:8080}") String monolithBaseUrl,
      @Value("${monolith.internal.api.key:default-internal-key}") String internalApiKey) {
    this.restTemplate = restTemplate;
    this.monolithBaseUrl = monolithBaseUrl;
    this.internalApiKey = internalApiKey;
  }

  public ProfileData getProfile(String userId) {
    try {
      String url = monolithBaseUrl + "/api/internal/profiles/" + userId;
      ResponseEntity<ProfileResponse> response =
          restTemplate.exchange(url, HttpMethod.GET, entityWithApiKey(), ProfileResponse.class);
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
          restTemplate.exchange(url, HttpMethod.GET, entityWithApiKey(), UserLookupResponse.class);
      if (response.getBody() != null
          && response.getBody().getUser() != null
          && response.getBody().getUser().getId() != null) {
        return response.getBody().getUser().getId();
      }
    } catch (Exception e) {
      logger.warn("Failed to resolve username={}: {}", username, e.getMessage());
    }
    return null;
  }

  private HttpEntity<Void> entityWithApiKey() {
    HttpHeaders headers = new HttpHeaders();
    headers.set(API_KEY_HEADER, internalApiKey);
    return new HttpEntity<>(headers);
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
    private UserData user;

    @lombok.Data
    @lombok.NoArgsConstructor
    public static class UserData {
      private String id;
      private String username;
    }
  }
}
