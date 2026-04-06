package io.spring.interaction.infrastructure.mybatis.mapper;

import io.spring.interaction.core.ArticleFavorite;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ArticleFavoriteMapper {
  void insert(@Param("articleFavorite") ArticleFavorite articleFavorite);

  ArticleFavorite find(@Param("articleId") String articleId, @Param("userId") String userId);

  void delete(@Param("articleFavorite") ArticleFavorite articleFavorite);

  int countByArticleId(@Param("articleId") String articleId);
}
