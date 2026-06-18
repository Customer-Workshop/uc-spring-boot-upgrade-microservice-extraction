package io.spring.comments.infrastructure.client;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Component
public class UserServiceClient {
  private static final Logger logger = LoggerFactory.getLogger(UserServiceClient.class);

  private final RestTemplate restTemplate;
  private final String monolithUrl;

  public UserServiceClient(@Value("${services.monolith.url}") String monolithUrl) {
    this.restTemplate = new RestTemplate();
    this.monolithUrl = monolithUrl;
  }

  public boolean isUserFollowing(String userId, String anotherUserId) {
    try {
      String url = monolithUrl + "/api/users/" + userId + "/following/" + anotherUserId;
      Boolean result = restTemplate.getForObject(url, Boolean.class);
      return result != null && result;
    } catch (RestClientException e) {
      logger.warn("Failed to check user following status: {}", e.getMessage());
      return false;
    }
  }

  public Set<String> followingAuthors(String userId, List<String> authorIds) {
    try {
      String ids = String.join(",", authorIds);
      String url = monolithUrl + "/api/users/" + userId + "/following?ids=" + ids;
      String[] result = restTemplate.getForObject(url, String[].class);
      if (result != null) {
        Set<String> set = new HashSet<>();
        Collections.addAll(set, result);
        return set;
      }
      return Collections.emptySet();
    } catch (RestClientException e) {
      logger.warn("Failed to fetch following authors: {}", e.getMessage());
      return Collections.emptySet();
    }
  }
}
