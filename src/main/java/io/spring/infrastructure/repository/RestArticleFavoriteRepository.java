package io.spring.infrastructure.repository;

import io.spring.core.favorite.ArticleFavorite;
import io.spring.core.favorite.ArticleFavoriteRepository;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Repository
@Profile("microservice")
public class RestArticleFavoriteRepository implements ArticleFavoriteRepository {
  private final RestTemplate restTemplate;
  private final String favoritesServiceUrl;

  public RestArticleFavoriteRepository(
      RestTemplate restTemplate, @Value("${services.favorites.url}") String favoritesServiceUrl) {
    this.restTemplate = restTemplate;
    this.favoritesServiceUrl = favoritesServiceUrl;
  }

  @Override
  public void save(ArticleFavorite articleFavorite) {
    try {
      Map<String, String> body = new HashMap<>();
      body.put("articleId", articleFavorite.getArticleId());
      body.put("userId", articleFavorite.getUserId());
      restTemplate.postForEntity(favoritesServiceUrl + "/api/favorites", body, Map.class);
    } catch (RestClientException e) {
      throw new RuntimeException("Failed to save favorite via favorites service", e);
    }
  }

  @Override
  @SuppressWarnings("unchecked")
  public Optional<ArticleFavorite> find(String articleId, String userId) {
    try {
      String url =
          favoritesServiceUrl + "/api/favorites?articleId=" + articleId + "&userId=" + userId;
      ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
      if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
        Map<String, String> body = response.getBody();
        return Optional.of(new ArticleFavorite(body.get("articleId"), body.get("userId")));
      }
      return Optional.empty();
    } catch (RestClientException e) {
      return Optional.empty();
    }
  }

  @Override
  public void remove(ArticleFavorite favorite) {
    try {
      String url =
          favoritesServiceUrl
              + "/api/favorites?articleId="
              + favorite.getArticleId()
              + "&userId="
              + favorite.getUserId();
      restTemplate.delete(url);
    } catch (RestClientException e) {
      throw new RuntimeException("Failed to remove favorite via favorites service", e);
    }
  }
}
