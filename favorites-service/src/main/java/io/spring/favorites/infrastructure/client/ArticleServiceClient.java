package io.spring.favorites.infrastructure.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Component
public class ArticleServiceClient {
  private final RestTemplate restTemplate;
  private final String monolithBaseUrl;

  public ArticleServiceClient(
      RestTemplate restTemplate, @Value("${services.monolith.url}") String monolithBaseUrl) {
    this.restTemplate = restTemplate;
    this.monolithBaseUrl = monolithBaseUrl;
  }

  public String resolveArticleIdBySlug(String slug) {
    try {
      String url = monolithBaseUrl + "/api/articles/" + slug + "/id";
      return restTemplate.getForObject(url, String.class);
    } catch (RestClientException e) {
      throw new RuntimeException("Failed to resolve article ID for slug: " + slug, e);
    }
  }
}
