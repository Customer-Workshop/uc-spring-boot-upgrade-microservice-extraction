package io.spring.commentservice.client;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Component
public class UserServiceClient {

  private final RestTemplate restTemplate;
  private final String monolithBaseUrl;

  public UserServiceClient(
      RestTemplate restTemplate,
      @Value("${monolith.base-url:http://localhost:8080}") String monolithBaseUrl) {
    this.restTemplate = restTemplate;
    this.monolithBaseUrl = monolithBaseUrl;
  }

  @SuppressWarnings("unchecked")
  public boolean isUserFollowing(String userId, String anotherUserId, String token) {
    try {
      HttpHeaders headers = new HttpHeaders();
      if (token != null) {
        headers.set("Authorization", "Token " + token);
      }
      HttpEntity<Void> entity = new HttpEntity<>(headers);
      ResponseEntity<Map<String, Object>> response =
          restTemplate.exchange(
              monolithBaseUrl + "/profiles/{username}",
              HttpMethod.GET,
              entity,
              new ParameterizedTypeReference<Map<String, Object>>() {},
              anotherUserId);
      if (response.getBody() != null && response.getBody().containsKey("profile")) {
        Map<String, Object> profile =
            (Map<String, Object>) response.getBody().get("profile");
        return Boolean.TRUE.equals(profile.get("following"));
      }
      return false;
    } catch (HttpClientErrorException e) {
      return false;
    }
  }

  public Set<String> followingAuthors(String userId, List<String> authorIds) {
    return Collections.emptySet();
  }
}
