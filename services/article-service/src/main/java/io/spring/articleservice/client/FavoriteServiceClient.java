package io.spring.articleservice.client;

import io.spring.articleservice.application.data.ArticleFavoriteCount;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class FavoriteServiceClient {

  private final RestTemplate restTemplate;
  private final String favoriteServiceUrl;

  public FavoriteServiceClient(
      RestTemplate restTemplate,
      @Value("${favorite-service.url:http://favorite-service:8083}") String favoriteServiceUrl) {
    this.restTemplate = restTemplate;
    this.favoriteServiceUrl = favoriteServiceUrl;
  }

  public boolean isUserFavorite(String userId, String articleId) {
    try {
      ResponseEntity<Boolean> response =
          restTemplate.getForEntity(
              favoriteServiceUrl
                  + "/internal/favorites/check?userId={userId}&articleId={articleId}",
              Boolean.class,
              userId,
              articleId);
      return Boolean.TRUE.equals(response.getBody());
    } catch (Exception e) {
      return false;
    }
  }

  public int articleFavoriteCount(String articleId) {
    try {
      ResponseEntity<Integer> response =
          restTemplate.getForEntity(
              favoriteServiceUrl + "/internal/favorites/count/{articleId}",
              Integer.class,
              articleId);
      Integer body = response.getBody();
      return body != null ? body : 0;
    } catch (Exception e) {
      return 0;
    }
  }

  public List<ArticleFavoriteCount> articlesFavoriteCount(List<String> articleIds) {
    try {
      ResponseEntity<List<ArticleFavoriteCount>> response =
          restTemplate.exchange(
              favoriteServiceUrl
                  + "/internal/favorites/counts?ids={ids}",
              HttpMethod.GET,
              null,
              new ParameterizedTypeReference<List<ArticleFavoriteCount>>() {},
              String.join(",", articleIds));
      List<ArticleFavoriteCount> body = response.getBody();
      return body != null ? body : Collections.emptyList();
    } catch (Exception e) {
      return Collections.emptyList();
    }
  }

  public Set<String> userFavorites(List<String> articleIds, String userId) {
    try {
      ResponseEntity<Set<String>> response =
          restTemplate.exchange(
              favoriteServiceUrl
                  + "/internal/favorites/user/{userId}?ids={ids}",
              HttpMethod.GET,
              null,
              new ParameterizedTypeReference<Set<String>>() {},
              userId,
              String.join(",", articleIds));
      Set<String> body = response.getBody();
      return body != null ? body : Collections.emptySet();
    } catch (Exception e) {
      return Collections.emptySet();
    }
  }
}
