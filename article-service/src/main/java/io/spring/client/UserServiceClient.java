package io.spring.client;

import io.spring.core.user.User;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Component
public class UserServiceClient {

  private final RestTemplate restTemplate;
  private final String userServiceUrl;

  public UserServiceClient(
      @Value("${user-service.url:http://localhost:8080}") String userServiceUrl) {
    this.restTemplate = new RestTemplate();
    this.userServiceUrl = userServiceUrl;
  }

  public boolean isUserFollowing(String userId, String targetId) {
    try {
      String url = userServiceUrl + "/api/internal/users/" + userId + "/following/" + targetId;
      ResponseEntity<Boolean> response = restTemplate.getForEntity(url, Boolean.class);
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
              new HttpEntity<>(authorIds),
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
              url, HttpMethod.GET, null, new ParameterizedTypeReference<List<String>>() {});
      List<String> body = response.getBody();
      return body != null ? body : Collections.emptyList();
    } catch (RestClientException e) {
      return Collections.emptyList();
    }
  }

  public Optional<User> findUserById(String userId) {
    try {
      String url = userServiceUrl + "/api/internal/users/" + userId;
      ResponseEntity<UserDto> response = restTemplate.getForEntity(url, UserDto.class);
      UserDto dto = response.getBody();
      if (dto == null) {
        return Optional.empty();
      }
      return Optional.of(
          new User(dto.getEmail(), dto.getUsername(), "", dto.getBio(), dto.getImage()));
    } catch (RestClientException e) {
      return Optional.empty();
    }
  }
}
