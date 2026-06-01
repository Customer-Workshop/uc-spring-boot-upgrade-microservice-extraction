package io.spring.articleservice.infrastructure.mybatis.mapper;

import io.spring.articleservice.core.favorite.ArticleFavorite;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ArticleFavoriteMapper {
  void insert(@Param("articleFavorite") ArticleFavorite articleFavorite);

  void delete(@Param("favorite") ArticleFavorite favorite);

  ArticleFavorite find(@Param("articleId") String articleId, @Param("userId") String userId);
}
