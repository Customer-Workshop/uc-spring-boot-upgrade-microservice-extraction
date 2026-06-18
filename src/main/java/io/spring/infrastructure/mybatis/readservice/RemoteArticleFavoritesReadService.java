package io.spring.infrastructure.mybatis.readservice;

import io.spring.application.data.ArticleFavoriteCount;
import io.spring.core.user.User;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Service
@Profile("microservice")
@Primary
public class RemoteArticleFavoritesReadService implements ArticleFavoritesReadService {
  private final RestTemplate restTemplate;
  private final String favoritesServiceUrl;

  public RemoteArticleFavoritesReadService(
      RestTemplate restTemplate, @Value("${favorites-service.url}") String favoritesServiceUrl) {
    this.restTemplate = restTemplate;
    this.favoritesServiceUrl = favoritesServiceUrl;
  }

  @Override
  public boolean isUserFavorite(String userId, String articleId) {
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
      return body != null && Boolean.TRUE.equals(body.get("favorited"));
    } catch (RestClientException e) {
      return false;
    }
  }

  @Override
  public int articleFavoriteCount(String articleId) {
    try {
      ResponseEntity<Map<String, Object>> response =
          restTemplate.exchange(
              favoritesServiceUrl + "/articles/{articleId}/favorites/count",
              HttpMethod.GET,
              null,
              new ParameterizedTypeReference<Map<String, Object>>() {},
              articleId);
      Map<String, Object> body = response.getBody();
      if (body != null && body.containsKey("count")) {
        return ((Number) body.get("count")).intValue();
      }
      return 0;
    } catch (RestClientException e) {
      return 0;
    }
  }

  @Override
  public List<ArticleFavoriteCount> articlesFavoriteCount(List<String> ids) {
    List<ArticleFavoriteCount> result = new ArrayList<>();
    for (String id : ids) {
      int count = articleFavoriteCount(id);
      result.add(new ArticleFavoriteCount(id, count));
    }
    return result;
  }

  @Override
  public Set<String> userFavorites(List<String> ids, User currentUser) {
    Set<String> favorited = new HashSet<>();
    for (String id : ids) {
      if (isUserFavorite(currentUser.getId(), id)) {
        favorited.add(id);
      }
    }
    return favorited;
  }
}
