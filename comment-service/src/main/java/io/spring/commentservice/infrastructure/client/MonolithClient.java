package io.spring.commentservice.infrastructure.client;

import io.spring.commentservice.application.dto.ArticleDto;
import io.spring.commentservice.application.dto.ProfileDto;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Component
public class MonolithClient {
  private final RestTemplate restTemplate;
  private final String monolithUrl;

  public MonolithClient(RestTemplate restTemplate, @Value("${monolith.url}") String monolithUrl) {
    this.restTemplate = restTemplate;
    this.monolithUrl = monolithUrl;
  }

  @SuppressWarnings("unchecked")
  public ArticleDto getArticleBySlug(String slug) {
    try {
      Map<String, Object> response =
          restTemplate.getForObject(monolithUrl + "/articles/" + slug, Map.class);
      if (response == null || !response.containsKey("article")) {
        return null;
      }
      Map<String, Object> article = (Map<String, Object>) response.get("article");
      return new ArticleDto(
          (String) article.get("id"), (String) article.get("slug"), (String) article.get("title"));
    } catch (RestClientException e) {
      throw new RuntimeException("Failed to fetch article from monolith: " + e.getMessage(), e);
    }
  }

  @SuppressWarnings("unchecked")
  public ProfileDto getUserProfile(String userId) {
    try {
      Map<String, Object> response =
          restTemplate.getForObject(monolithUrl + "/profiles/" + userId, Map.class);
      if (response == null || !response.containsKey("profile")) {
        return new ProfileDto(userId, "unknown", null, null, false);
      }
      Map<String, Object> profile = (Map<String, Object>) response.get("profile");
      return new ProfileDto(
          userId,
          (String) profile.get("username"),
          (String) profile.get("bio"),
          (String) profile.get("image"),
          Boolean.TRUE.equals(profile.get("following")));
    } catch (RestClientException e) {
      return new ProfileDto(userId, "unknown", null, null, false);
    }
  }
}
