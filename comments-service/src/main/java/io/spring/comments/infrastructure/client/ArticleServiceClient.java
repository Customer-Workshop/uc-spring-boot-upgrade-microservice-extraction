package io.spring.comments.infrastructure.client;

import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Component
public class ArticleServiceClient {
  private static final Logger logger = LoggerFactory.getLogger(ArticleServiceClient.class);

  private final RestTemplate restTemplate;
  private final String monolithUrl;

  public ArticleServiceClient(@Value("${services.monolith.url}") String monolithUrl) {
    this.restTemplate = new RestTemplate();
    this.monolithUrl = monolithUrl;
  }

  @SuppressWarnings("unchecked")
  public String resolveArticleIdBySlug(String slug) {
    try {
      String url = monolithUrl + "/articles/" + slug;
      Map<String, Object> response = restTemplate.getForObject(url, Map.class);
      if (response != null && response.containsKey("article")) {
        Map<String, Object> article = (Map<String, Object>) response.get("article");
        return (String) article.get("id");
      }
      return null;
    } catch (RestClientException e) {
      logger.warn("Failed to resolve article slug '{}': {}", slug, e.getMessage());
      return null;
    }
  }
}
