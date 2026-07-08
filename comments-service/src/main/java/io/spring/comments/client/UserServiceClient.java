package io.spring.comments.client;

import io.spring.comments.application.data.ProfileData;
import io.spring.comments.client.dto.ProfileDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * Client for resolving comment author profiles from the monolith (Users/Profiles bounded context).
 * The Comments microservice stores only {@code user_id}; author metadata lives in the monolith.
 */
@Component
public class UserServiceClient {

  private static final Logger log = LoggerFactory.getLogger(UserServiceClient.class);

  private final RestTemplate restTemplate;
  private final String monolithUrl;

  public UserServiceClient(
      RestTemplate restTemplate, @Value("${monolith.url}") String monolithUrl) {
    this.restTemplate = restTemplate;
    this.monolithUrl = monolithUrl;
  }

  /**
   * Fetches the profile for {@code userId}. {@code viewerId} (nullable) is the user requesting the
   * comment, used to compute the {@code following} flag. Falls back to a minimal profile when the
   * monolith is unavailable so comment reads degrade gracefully.
   */
  public ProfileData getProfile(String userId, String viewerId) {
    UriComponentsBuilder uri =
        UriComponentsBuilder.fromHttpUrl(monolithUrl)
            .path("/internal/profiles/{userId}")
            .queryParamIfPresent(
                "viewerId",
                viewerId == null || viewerId.isEmpty()
                    ? java.util.Optional.empty()
                    : java.util.Optional.of(viewerId));
    try {
      ProfileDto dto =
          restTemplate.getForObject(uri.buildAndExpand(userId).toUri(), ProfileDto.class);
      if (dto == null) {
        return fallback(userId);
      }
      return new ProfileData(
          dto.getId(), dto.getUsername(), dto.getBio(), dto.getImage(), dto.isFollowing());
    } catch (RestClientException e) {
      log.warn("Failed to resolve profile for user {} from monolith: {}", userId, e.getMessage());
      return fallback(userId);
    }
  }

  private ProfileData fallback(String userId) {
    return new ProfileData(userId, "", "", "", false);
  }
}
