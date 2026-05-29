package io.spring.articleservice.client;

import io.spring.articleservice.config.UserServiceProperties;
import io.spring.articleservice.dto.ProfileDto;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class UserServiceClient {

  private static final Logger log = LoggerFactory.getLogger(UserServiceClient.class);
  private final RestClient restClient;

  public UserServiceClient(RestClient.Builder builder, UserServiceProperties properties) {
    this.restClient = builder.baseUrl(properties.getUrl()).build();
  }

  public Optional<ProfileDto> getProfileById(String userId) {
    try {
      ProfileDto profile =
          restClient
              .get()
              .uri("/api/internal/users/{userId}/profile", userId)
              .accept(MediaType.APPLICATION_JSON)
              .retrieve()
              .body(ProfileDto.class);
      return Optional.ofNullable(profile);
    } catch (RestClientException e) {
      log.warn("Failed to fetch profile for userId={}: {}", userId, e.getMessage());
      return Optional.empty();
    }
  }

  public Optional<ProfileDto> getProfileByUsername(String username) {
    try {
      ProfileDto profile =
          restClient
              .get()
              .uri("/api/internal/users/by-username/{username}", username)
              .accept(MediaType.APPLICATION_JSON)
              .retrieve()
              .body(ProfileDto.class);
      return Optional.ofNullable(profile);
    } catch (RestClientException e) {
      log.warn("Failed to fetch profile for username={}: {}", username, e.getMessage());
      return Optional.empty();
    }
  }

  public Set<String> getFollowingAuthors(String userId, List<String> authorIds) {
    if (authorIds.isEmpty()) {
      return Collections.emptySet();
    }
    try {
      Set<String> result =
          restClient
              .get()
              .uri(
                  uriBuilder ->
                      uriBuilder
                          .path("/api/internal/users/following-authors")
                          .queryParam("userId", userId)
                          .queryParam("authorIds", authorIds)
                          .build())
              .accept(MediaType.APPLICATION_JSON)
              .retrieve()
              .body(new ParameterizedTypeReference<Set<String>>() {});
      return result != null ? result : Collections.emptySet();
    } catch (RestClientException e) {
      log.warn("Failed to fetch following authors for userId={}: {}", userId, e.getMessage());
      return Collections.emptySet();
    }
  }

  public List<String> getFollowedUsers(String userId) {
    try {
      List<String> result =
          restClient
              .get()
              .uri("/api/internal/users/{userId}/followed", userId)
              .accept(MediaType.APPLICATION_JSON)
              .retrieve()
              .body(new ParameterizedTypeReference<List<String>>() {});
      return result != null ? result : Collections.emptyList();
    } catch (RestClientException e) {
      log.warn("Failed to fetch followed users for userId={}: {}", userId, e.getMessage());
      return Collections.emptyList();
    }
  }

  public Map<String, ProfileDto> getProfilesByIds(List<String> userIds) {
    if (userIds.isEmpty()) {
      return Collections.emptyMap();
    }
    List<String> uniqueIds = userIds.stream().distinct().collect(Collectors.toList());
    try {
      List<ProfileDto> profiles =
          restClient
              .get()
              .uri(
                  uriBuilder ->
                      uriBuilder
                          .path("/api/internal/users/profiles")
                          .queryParam("ids", uniqueIds)
                          .build())
              .accept(MediaType.APPLICATION_JSON)
              .retrieve()
              .body(new ParameterizedTypeReference<List<ProfileDto>>() {});
      if (profiles == null) {
        return Collections.emptyMap();
      }
      return profiles.stream().collect(Collectors.toMap(ProfileDto::id, p -> p, (a, b) -> a));
    } catch (RestClientException e) {
      log.warn("Failed to batch-fetch profiles: {}", e.getMessage());
      return Collections.emptyMap();
    }
  }
}
