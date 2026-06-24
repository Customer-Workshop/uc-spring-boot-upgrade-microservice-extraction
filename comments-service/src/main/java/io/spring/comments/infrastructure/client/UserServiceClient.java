package io.spring.comments.infrastructure.client;

import io.spring.comments.application.data.ProfileData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * Calls back into the monolith to resolve author profile information for a comment. The comments
 * service does not own the {@code users} table, so author data is fetched over HTTP.
 */
@Slf4j
@Component
public class UserServiceClient {

  private final RestTemplate restTemplate;
  private final String monolithUrl;

  public UserServiceClient(
      RestTemplate restTemplate, @Value("${monolith.url}") String monolithUrl) {
    this.restTemplate = restTemplate;
    this.monolithUrl = monolithUrl;
  }

  public ProfileData getProfile(String userId, String viewerId) {
    String url =
        UriComponentsBuilder.fromHttpUrl(monolithUrl)
            .path("/internal/profiles/{userId}")
            .queryParamIfPresent(
                "viewerId",
                viewerId == null || viewerId.isEmpty()
                    ? java.util.Optional.empty()
                    : java.util.Optional.of(viewerId))
            .buildAndExpand(userId)
            .toUriString();
    try {
      ProfileResponse response = restTemplate.getForObject(url, ProfileResponse.class);
      if (response == null) {
        return fallback(userId);
      }
      return new ProfileData(
          response.getId() == null ? userId : response.getId(),
          response.getUsername(),
          response.getBio(),
          response.getImage(),
          response.isFollowing());
    } catch (Exception e) {
      log.warn("Failed to resolve profile for user {} from monolith at {}", userId, url, e);
      return fallback(userId);
    }
  }

  private ProfileData fallback(String userId) {
    return new ProfileData(userId, null, null, null, false);
  }
}
