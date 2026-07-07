package io.spring.comments.client;

import io.spring.comments.application.data.ProfileData;
import io.spring.comments.client.dto.ProfileResponse;
import java.net.URI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * Calls back to the monolith (users/profiles bounded context) to resolve the author profile for a
 * comment. Failures are handled gracefully so the comments service stays available even when the
 * monolith is degraded.
 */
@Component
public class UserServiceClient {
  private static final Logger log = LoggerFactory.getLogger(UserServiceClient.class);

  private final RestTemplate restTemplate;
  private final String monolithBaseUrl;

  public UserServiceClient(
      RestTemplate restTemplate,
      @Value("${monolith.service.url:http://localhost:8080}") String monolithBaseUrl) {
    this.restTemplate = restTemplate;
    this.monolithBaseUrl = monolithBaseUrl;
  }

  public ProfileData getProfile(String userId, String viewerId) {
    UriComponentsBuilder builder =
        UriComponentsBuilder.fromHttpUrl(monolithBaseUrl)
            .path("/internal/profiles")
            .queryParam("userId", userId);
    if (viewerId != null) {
      builder.queryParam("viewerId", viewerId);
    }
    URI uri = builder.build().toUri();
    try {
      ProfileResponse response = restTemplate.getForObject(uri, ProfileResponse.class);
      if (response == null) {
        return fallback(userId);
      }
      return new ProfileData(
          response.getId(),
          response.getUsername(),
          response.getBio(),
          response.getImage(),
          response.isFollowing());
    } catch (Exception e) {
      log.warn("Failed to resolve profile for userId={} from monolith: {}", userId, e.getMessage());
      return fallback(userId);
    }
  }

  private ProfileData fallback(String userId) {
    return new ProfileData(userId, null, null, null, false);
  }
}
