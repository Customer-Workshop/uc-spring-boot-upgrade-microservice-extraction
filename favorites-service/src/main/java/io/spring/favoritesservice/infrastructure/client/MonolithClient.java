package io.spring.favoritesservice.infrastructure.client;

import io.spring.favoritesservice.application.dto.ArticleDto;
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
}
