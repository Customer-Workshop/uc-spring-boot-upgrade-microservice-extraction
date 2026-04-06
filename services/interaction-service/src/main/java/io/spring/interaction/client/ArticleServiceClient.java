package io.spring.interaction.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class ArticleServiceClient {
  private final RestTemplate restTemplate;
  private final String articleServiceUrl;

  public ArticleServiceClient(
      RestTemplate restTemplate,
      @Value("${services.article-service.url:http://localhost:8082}") String articleServiceUrl) {
    this.restTemplate = restTemplate;
    this.articleServiceUrl = articleServiceUrl;
  }

  public boolean articleExists(String slug) {
    try {
      Boolean result =
          restTemplate.getForObject(
              articleServiceUrl + "/internal/articles/" + slug + "/exists", Boolean.class);
      return result != null && result;
    } catch (Exception e) {
      return false;
    }
  }

  public String getArticleId(String slug) {
    try {
      return restTemplate.getForObject(
          articleServiceUrl + "/internal/articles/" + slug + "/id", String.class);
    } catch (Exception e) {
      return null;
    }
  }
}
