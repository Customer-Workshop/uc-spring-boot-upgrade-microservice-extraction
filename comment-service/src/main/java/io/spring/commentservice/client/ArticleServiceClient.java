package io.spring.commentservice.client;

import java.util.Map;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Component
public class ArticleServiceClient {

  private final RestTemplate restTemplate;
  private final String monolithBaseUrl;

  public ArticleServiceClient(
      RestTemplate restTemplate,
      @Value("${monolith.base-url:http://localhost:8080}") String monolithBaseUrl) {
    this.restTemplate = restTemplate;
    this.monolithBaseUrl = monolithBaseUrl;
  }

  @SuppressWarnings("unchecked")
  public Optional<String> getArticleIdBySlug(String slug) {
    try {
      Map<String, Object> response =
          restTemplate.getForObject(monolithBaseUrl + "/articles/{slug}", Map.class, slug);
      if (response != null && response.containsKey("article")) {
        Map<String, Object> article = (Map<String, Object>) response.get("article");
        return Optional.ofNullable((String) article.get("id"));
      }
      return Optional.empty();
    } catch (HttpClientErrorException.NotFound e) {
      return Optional.empty();
    }
  }
}
