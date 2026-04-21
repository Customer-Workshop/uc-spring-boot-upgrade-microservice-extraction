package io.spring.commentservice.client;

import java.util.Optional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Component
public class ArticleServiceClientImpl implements ArticleServiceClient {

  private final RestTemplate restTemplate;
  private final String articleServiceUrl;

  public ArticleServiceClientImpl(
      RestTemplate restTemplate, @Value("${article-service.url}") String articleServiceUrl) {
    this.restTemplate = restTemplate;
    this.articleServiceUrl = articleServiceUrl;
  }

  @Override
  public Optional<ArticleResponse> getArticleBySlug(String slug) {
    try {
      ArticleResponse response =
          restTemplate.getForObject(
              articleServiceUrl + "/api/internal/articles/by-slug/{slug}",
              ArticleResponse.class,
              slug);
      return Optional.ofNullable(response);
    } catch (RestClientException e) {
      return Optional.empty();
    }
  }
}
