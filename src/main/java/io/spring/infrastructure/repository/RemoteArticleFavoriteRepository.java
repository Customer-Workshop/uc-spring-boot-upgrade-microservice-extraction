package io.spring.infrastructure.repository;

import io.spring.core.favorite.ArticleFavorite;
import io.spring.core.favorite.ArticleFavoriteRepository;
import java.util.Map;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Repository
@Profile("microservice")
public class RemoteArticleFavoriteRepository implements ArticleFavoriteRepository {
  private final RestTemplate restTemplate;
  private final String favoritesServiceUrl;

  public RemoteArticleFavoriteRepository(
      RestTemplate restTemplate, @Value("${favorites-service.url}") String favoritesServiceUrl) {
    this.restTemplate = restTemplate;
    this.favoritesServiceUrl = favoritesServiceUrl;
  }

  @Override
  public void save(ArticleFavorite articleFavorite) {
    try {
      HttpHeaders headers = new HttpHeaders();
      headers.set("X-User-Id", articleFavorite.getUserId());
      HttpEntity<Void> entity = new HttpEntity<>(headers);

      restTemplate.exchange(
          favoritesServiceUrl + "/articles/{articleId}/favorite",
          HttpMethod.POST,
          entity,
          Map.class,
          articleFavorite.getArticleId());
    } catch (RestClientException e) {
      throw new RuntimeException(
          "Failed to save favorite via favorites-service: " + e.getMessage(), e);
    }
  }

  @Override
  public Optional<ArticleFavorite> find(String articleId, String userId) {
    try {
      ResponseEntity<Map<String, Object>> response =
          restTemplate.exchange(
              favoritesServiceUrl + "/articles/{articleId}/favorites/status?userId={userId}",
              HttpMethod.GET,
              null,
              new ParameterizedTypeReference<Map<String, Object>>() {},
              articleId,
              userId);
      Map<String, Object> body = response.getBody();
      if (body != null && Boolean.TRUE.equals(body.get("favorited"))) {
        return Optional.of(new ArticleFavorite(articleId, userId));
      }
      return Optional.empty();
    } catch (RestClientException e) {
      return Optional.empty();
    }
  }

  @Override
  public void remove(ArticleFavorite favorite) {
    try {
      HttpHeaders headers = new HttpHeaders();
      headers.set("X-User-Id", favorite.getUserId());
      HttpEntity<Void> entity = new HttpEntity<>(headers);

      restTemplate.exchange(
          favoritesServiceUrl + "/articles/{articleId}/favorite",
          HttpMethod.DELETE,
          entity,
          Map.class,
          favorite.getArticleId());
    } catch (RestClientException e) {
      throw new RuntimeException(
          "Failed to remove favorite via favorites-service: " + e.getMessage(), e);
    }
  }
}
