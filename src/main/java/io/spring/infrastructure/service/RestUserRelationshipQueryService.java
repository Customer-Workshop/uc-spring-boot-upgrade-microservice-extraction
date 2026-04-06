package io.spring.infrastructure.service;

import io.spring.infrastructure.mybatis.readservice.UserRelationshipQueryService;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Service
public class RestUserRelationshipQueryService implements UserRelationshipQueryService {

  private final RestTemplate restTemplate;
  private final String userServiceUrl;

  public RestUserRelationshipQueryService(
      RestTemplate restTemplate,
      @Value("${user-service.url}") String userServiceUrl) {
    this.restTemplate = restTemplate;
    this.userServiceUrl = userServiceUrl;
  }

  @Override
  public boolean isUserFollowing(String userId, String anotherUserId) {
    try {
      Boolean result =
          restTemplate.getForObject(
              userServiceUrl + "/api/internal/users/{userId}/following/{targetUserId}",
              Boolean.class,
              userId,
              anotherUserId);
      return Boolean.TRUE.equals(result);
    } catch (HttpClientErrorException.NotFound e) {
      return false;
    }
  }

  @Override
  public Set<String> followingAuthors(String userId, List<String> ids) {
    if (ids == null || ids.isEmpty()) {
      return Collections.emptySet();
    }
    try {
      String url =
          UriComponentsBuilder.fromHttpUrl(
                  userServiceUrl + "/api/internal/users/{userId}/following-authors")
              .queryParam("ids", ids.toArray())
              .buildAndExpand(userId)
              .toUriString();
      ResponseEntity<Set<String>> response =
          restTemplate.exchange(url, HttpMethod.GET, null, new ParameterizedTypeReference<Set<String>>() {});
      return response.getBody() != null ? response.getBody() : Collections.emptySet();
    } catch (HttpClientErrorException.NotFound e) {
      return Collections.emptySet();
    }
  }

  @Override
  public List<String> followedUsers(String userId) {
    try {
      ResponseEntity<List<String>> response =
          restTemplate.exchange(
              userServiceUrl + "/api/internal/users/{userId}/followed-users",
              HttpMethod.GET,
              null,
              new ParameterizedTypeReference<List<String>>() {},
              userId);
      return response.getBody() != null ? response.getBody() : Collections.emptyList();
    } catch (HttpClientErrorException.NotFound e) {
      return Collections.emptyList();
    }
  }
}
