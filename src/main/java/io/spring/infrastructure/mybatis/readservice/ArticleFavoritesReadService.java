package io.spring.infrastructure.mybatis.readservice;

import io.spring.application.data.ArticleFavoriteCount;
import io.spring.core.user.User;
import java.util.List;
import java.util.Set;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

// TODO: This read service has been extracted to the Favorite Service (favorite-service/).
// Once the Favorite Service is fully deployed, remove this interface and replace usages
// with HTTP calls to the Favorite Service internal APIs:
//   GET /api/internal/favorites/count?articleIds=...
//   GET /api/internal/favorites/check?articleIds=...&userId=...
//   GET /api/internal/favorites/count/{articleId}
//   GET /api/internal/favorites/is-favorited?articleId=...&userId=...
@Mapper
public interface ArticleFavoritesReadService {
  boolean isUserFavorite(@Param("userId") String userId, @Param("articleId") String articleId);

  int articleFavoriteCount(@Param("articleId") String articleId);

  List<ArticleFavoriteCount> articlesFavoriteCount(@Param("ids") List<String> ids);

  Set<String> userFavorites(@Param("ids") List<String> ids, @Param("currentUser") User currentUser);
}
