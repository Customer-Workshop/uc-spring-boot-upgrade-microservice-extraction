package io.spring.client;

import io.spring.core.user.User;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Component
public class UserServiceClient {

  private final RestTemplate restTemplate;
  private final String userServiceUrl;
  private final String apiKey;

  public UserServiceClient(
      @Value("${user-service.url:http://localhost:8080}") String userServiceUrl,
      @Value("${user-service.api-key:default-internal-key}") String apiKey) {
    this.restTemplate = new RestTemplate();
    this.userServiceUrl = userServiceUrl;
    this.apiKey = apiKey;
  }

  private HttpHeaders createHeaders() {
    HttpHeaders headers = new HttpHeaders();
    headers.set("X-Internal-Api-Key", apiKey);
    return headers;
  }

  public boolean isUserFollowing(String userId, String targetId) {
    try {
      String url = userServiceUrl + "/api/internal/users/" + userId + "/following/" + targetId;
      ResponseEntity<Boolean> response =
          restTemplate.exchange(
              url, HttpMethod.GET, new HttpEntity<>(createHeaders()), Boolean.class);
      return Boolean.TRUE.equals(response.getBody());
    } catch (RestClientException e) {
      return false;
    }
  }

  public Set<String> followingAuthors(String userId, List<String> authorIds) {
    try {
      String url = userServiceUrl + "/api/internal/users/" + userId + "/following-authors";
      ResponseEntity<Set<String>> response =
          restTemplate.exchange(
              url,
              HttpMethod.POST,
              new HttpEntity<>(authorIds, createHeaders()),
              new ParameterizedTypeReference<Set<String>>() {});
      Set<String> body = response.getBody();
      return body != null ? body : Collections.emptySet();
    } catch (RestClientException e) {
      return Collections.emptySet();
    }
  }

  public List<String> followedUsers(String userId) {
    try {
      String url = userServiceUrl + "/api/internal/users/" + userId + "/followed-users";
      ResponseEntity<List<String>> response =
          restTemplate.exchange(
              url,
              HttpMethod.GET,
              new HttpEntity<>(createHeaders()),
              new ParameterizedTypeReference<List<String>>() {});
      List<String> body = response.getBody();
      return body != null ? body : Collections.emptyList();
    } catch (RestClientException e) {
      return Collections.emptyList();
    }
  }

  public Optional<User> findUserById(String userId) {
    try {
      String url = userServiceUrl + "/api/internal/users/" + userId;
      ResponseEntity<UserDto> response =
          restTemplate.exchange(
              url, HttpMethod.GET, new HttpEntity<>(createHeaders()), UserDto.class);
      UserDto dto = response.getBody();
      if (dto == null) {
        return Optional.empty();
      }
      return Optional.of(
          new User(
              dto.getId(), dto.getEmail(), dto.getUsername(), "", dto.getBio(), dto.getImage()));
    } catch (RestClientException e) {
      return Optional.empty();
    }
  }
}
