package io.spring.infrastructure.mybatis.mapper;

import io.spring.core.favorite.ArticleFavorite;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/** Maps article favorite persistence operations to SQL statements. */
@Mapper
public interface ArticleFavoriteMapper {
  /** Finds a favorite by article and user identifiers. */
  ArticleFavorite find(@Param("articleId") String articleId, @Param("userId") String userId);

  /** Inserts an article favorite. */
  void insert(@Param("articleFavorite") ArticleFavorite articleFavorite);

  /** Deletes an article favorite. */
  void delete(@Param("favorite") ArticleFavorite favorite);
}
