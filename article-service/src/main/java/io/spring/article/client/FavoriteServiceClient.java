package io.spring.article.client;

import io.spring.article.application.data.ArticleFavoriteCount;
import java.util.List;
import java.util.Set;

/**
 * HTTP client interface for calling the Favorite Service. In a full microservices deployment, this
 * would make HTTP calls to the Favorite Service.
 *
 * <p>Expected endpoints on Favorite Service:
 *
 * <ul>
 *   <li>GET /api/internal/favorites/count?articleIds={ids} - Get favorite counts for articles
 *   <li>GET /api/internal/favorites/user/{userId}?articleIds={ids} - Get which articles a user has
 *       favorited
 *   <li>GET /api/internal/favorites/{articleId}/count - Get favorite count for a single article
 *   <li>GET /api/internal/favorites/{articleId}/user/{userId} - Check if user favorited an article
 * </ul>
 */
public interface FavoriteServiceClient {

  boolean isUserFavorite(String userId, String articleId);

  int articleFavoriteCount(String articleId);

  List<ArticleFavoriteCount> articlesFavoriteCount(List<String> articleIds);

  Set<String> userFavorites(List<String> articleIds, String userId);
}
