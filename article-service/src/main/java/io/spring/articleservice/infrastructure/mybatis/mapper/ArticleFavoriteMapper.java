package io.spring.articleservice.infrastructure.mybatis.mapper;

import io.spring.articleservice.core.favorite.ArticleFavorite;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ArticleFavoriteMapper {
  void insert(@Param("articleFavorite") ArticleFavorite articleFavorite);

  ArticleFavorite find(@Param("articleId") String articleId, @Param("userId") String userId);

  void delete(@Param("articleFavorite") ArticleFavorite articleFavorite);
}
