package io.spring.favorite.client;

import java.util.Optional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Component
public class ArticleServiceClientImpl implements ArticleServiceClient {

  private final RestTemplate restTemplate;
  private final String articleServiceUrl;

  public ArticleServiceClientImpl(
      RestTemplate restTemplate,
      @Value("${article.service.url}") String articleServiceUrl) {
    this.restTemplate = restTemplate;
    this.articleServiceUrl = articleServiceUrl;
  }

  @Override
  public Optional<ArticleData> findBySlug(String slug) {
    try {
      ArticleData article =
          restTemplate.getForObject(
              articleServiceUrl + "/api/internal/articles/by-slug/{slug}",
              ArticleData.class,
              slug);
      return Optional.ofNullable(article);
    } catch (HttpClientErrorException.NotFound e) {
      return Optional.empty();
    }
  }
}
