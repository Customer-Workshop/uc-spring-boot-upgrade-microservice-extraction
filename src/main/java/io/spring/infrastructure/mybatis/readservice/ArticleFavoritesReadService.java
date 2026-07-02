package io.spring.infrastructure.mybatis.readservice;

import io.spring.application.data.ArticleFavoriteCount;
import io.spring.core.user.User;
import java.util.List;
import java.util.Set;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/** MyBatis mapper for querying article favorite counts and user-favorite relationships. */
@Mapper
public interface ArticleFavoritesReadService {

  /** Checks whether a specific user has favorited a specific article. */
  boolean isUserFavorite(@Param("userId") String userId, @Param("articleId") String articleId);

  /** Returns the total number of favorites for a single article. */
  int articleFavoriteCount(@Param("articleId") String articleId);

  /** Returns favorite counts for a batch of article IDs. */
  List<ArticleFavoriteCount> articlesFavoriteCount(@Param("ids") List<String> ids);

  /** Returns the set of article IDs that the current user has favorited from the given list. */
  Set<String> userFavorites(@Param("ids") List<String> ids, @Param("currentUser") User currentUser);
}
