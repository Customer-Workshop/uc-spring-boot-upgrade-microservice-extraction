package io.spring.infrastructure.mybatis.readservice;

import io.spring.application.data.ArticleFavoriteCount;
import io.spring.core.user.User;
import java.util.List;
import java.util.Set;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/** Reads favorite status and counts from article and favorite tables. */
@Mapper
public interface ArticleFavoritesReadService {
  /** Returns whether a user has favorited an article. */
  boolean isUserFavorite(@Param("userId") String userId, @Param("articleId") String articleId);

  /** Counts favorites for an article. */
  int articleFavoriteCount(@Param("articleId") String articleId);

  /** Counts favorites for each supplied article identifier. */
  List<ArticleFavoriteCount> articlesFavoriteCount(@Param("ids") List<String> ids);

  /** Returns supplied article identifiers favorited by the current user. */
  Set<String> userFavorites(@Param("ids") List<String> ids, @Param("currentUser") User currentUser);
}
