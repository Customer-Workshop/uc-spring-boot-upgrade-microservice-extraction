package io.spring.infrastructure.service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

/**
 * REST client for communicating with the Article microservice. Used by the monolith to delegate
 * article CRUD operations to the standalone article-service.
 */
@Component
public class ArticleServiceClient {

  private final RestTemplate restTemplate;
  private final String articleServiceUrl;

  public ArticleServiceClient(
      RestTemplate restTemplate,
      @Value("${article.service.url:http://localhost:8081}") String articleServiceUrl) {
    this.restTemplate = restTemplate;
    this.articleServiceUrl = articleServiceUrl;
  }

  public Map<String, Object> createArticle(Map<String, Object> request) {
    ResponseEntity<Map<String, Object>> response =
        restTemplate.exchange(
            articleServiceUrl + "/api/articles",
            HttpMethod.POST,
            new HttpEntity<>(request),
            new ParameterizedTypeReference<Map<String, Object>>() {});
    return response.getBody();
  }

  public Optional<Map<String, Object>> getArticleBySlug(String slug) {
    try {
      ResponseEntity<Map<String, Object>> response =
          restTemplate.exchange(
              articleServiceUrl + "/api/articles/" + slug,
              HttpMethod.GET,
              null,
              new ParameterizedTypeReference<Map<String, Object>>() {});
      return Optional.ofNullable(response.getBody());
    } catch (HttpClientErrorException.NotFound e) {
      return Optional.empty();
    }
  }

  public List<Map<String, Object>> listArticles() {
    ResponseEntity<List<Map<String, Object>>> response =
        restTemplate.exchange(
            articleServiceUrl + "/api/articles",
            HttpMethod.GET,
            null,
            new ParameterizedTypeReference<List<Map<String, Object>>>() {});
    return response.getBody();
  }

  public Map<String, Object> updateArticle(String slug, Map<String, Object> request) {
    ResponseEntity<Map<String, Object>> response =
        restTemplate.exchange(
            articleServiceUrl + "/api/articles/" + slug,
            HttpMethod.PUT,
            new HttpEntity<>(request),
            new ParameterizedTypeReference<Map<String, Object>>() {});
    return response.getBody();
  }

  public void deleteArticle(String slug) {
    restTemplate.delete(articleServiceUrl + "/api/articles/" + slug);
  }

  public List<String> getTags() {
    ResponseEntity<List<String>> response =
        restTemplate.exchange(
            articleServiceUrl + "/api/articles/tags",
            HttpMethod.GET,
            null,
            new ParameterizedTypeReference<List<String>>() {});
    return response.getBody();
  }
}
