package io.spring.article.client;

import io.spring.article.application.data.ArticleFavoriteCount;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

/**
 * Stub implementation of FavoriteServiceClient. TODO: Replace with HTTP client calling Favorite
 * Service.
 */
@Component
public class StubFavoriteServiceClient implements FavoriteServiceClient {

  @Override
  public boolean isUserFavorite(String userId, String articleId) {
    // TODO: Call Favorite Service GET /api/internal/favorites/{articleId}/user/{userId}
    return false;
  }

  @Override
  public int articleFavoriteCount(String articleId) {
    // TODO: Call Favorite Service GET /api/internal/favorites/{articleId}/count
    return 0;
  }

  @Override
  public List<ArticleFavoriteCount> articlesFavoriteCount(List<String> articleIds) {
    // TODO: Call Favorite Service GET /api/internal/favorites/count?articleIds={ids}
    return articleIds.stream()
        .map(id -> new ArticleFavoriteCount(id, 0))
        .collect(Collectors.toList());
  }

  @Override
  public Set<String> userFavorites(List<String> articleIds, String userId) {
    // TODO: Call Favorite Service GET /api/internal/favorites/user/{userId}?articleIds={ids}
    return new HashSet<>();
  }
}
