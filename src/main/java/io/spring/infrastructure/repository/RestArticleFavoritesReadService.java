package io.spring.infrastructure.repository;

import io.spring.application.ArticleFavoritesReadServiceInterface;
import io.spring.application.data.ArticleFavoriteCount;
import io.spring.core.user.User;
import java.util.*;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Service
@Profile("microservice")
public class RestArticleFavoritesReadService implements ArticleFavoritesReadServiceInterface {
  private final RestTemplate restTemplate;
  private final String favoritesServiceUrl;

  public RestArticleFavoritesReadService(
      RestTemplate restTemplate, @Value("${services.favorites.url}") String favoritesServiceUrl) {
    this.restTemplate = restTemplate;
    this.favoritesServiceUrl = favoritesServiceUrl;
  }

  @Override
  public boolean isUserFavorite(String userId, String articleId) {
    try {
      String url =
          favoritesServiceUrl
              + "/api/favorites/is-favorite?userId="
              + userId
              + "&articleId="
              + articleId;
      ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
      if (response.getBody() != null) {
        Object isFavorite = response.getBody().get("isFavorite");
        return Boolean.TRUE.equals(isFavorite);
      }
      return false;
    } catch (RestClientException e) {
      return false;
    }
  }

  @Override
  public int articleFavoriteCount(String articleId) {
    try {
      String url = favoritesServiceUrl + "/api/favorites/count?articleId=" + articleId;
      ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
      if (response.getBody() != null) {
        Object count = response.getBody().get("count");
        if (count instanceof Number) {
          return ((Number) count).intValue();
        }
      }
      return 0;
    } catch (RestClientException e) {
      return 0;
    }
  }

  @Override
  @SuppressWarnings("unchecked")
  public List<ArticleFavoriteCount> articlesFavoriteCount(List<String> ids) {
    try {
      String articleIds = String.join(",", ids);
      String url = favoritesServiceUrl + "/api/favorites/count?articleIds=" + articleIds;
      ResponseEntity<List<Map<String, Object>>> response =
          restTemplate.exchange(
              url,
              HttpMethod.GET,
              null,
              new ParameterizedTypeReference<List<Map<String, Object>>>() {});
      if (response.getBody() != null) {
        return response.getBody().stream()
            .map(
                map ->
                    new ArticleFavoriteCount(
                        (String) map.get("id"), ((Number) map.get("count")).intValue()))
            .collect(Collectors.toList());
      }
      return Collections.emptyList();
    } catch (RestClientException e) {
      return Collections.emptyList();
    }
  }

  @Override
  public Set<String> userFavorites(List<String> ids, User currentUser) {
    try {
      String articleIds = String.join(",", ids);
      String url =
          favoritesServiceUrl
              + "/api/favorites/user/"
              + currentUser.getId()
              + "/check?articleIds="
              + articleIds;
      ResponseEntity<Set<String>> response =
          restTemplate.exchange(
              url, HttpMethod.GET, null, new ParameterizedTypeReference<Set<String>>() {});
      if (response.getBody() != null) {
        return response.getBody();
      }
      return Collections.emptySet();
    } catch (RestClientException e) {
      return Collections.emptySet();
    }
  }
}
