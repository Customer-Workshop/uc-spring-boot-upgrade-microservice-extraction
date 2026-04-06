package io.spring.interaction.infrastructure.mybatis.readservice;

import io.spring.shared.dto.ArticleFavoriteCount;
import java.util.List;
import java.util.Set;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ArticleFavoritesReadService {
  boolean isUserFavorite(@Param("userId") String userId, @Param("articleId") String articleId);

  int articleFavoriteCount(@Param("articleId") String articleId);

  List<ArticleFavoriteCount> articlesFavoriteCount(@Param("ids") List<String> ids);

  Set<String> userFavorites(@Param("ids") List<String> ids, @Param("currentUserId") String currentUserId);

  List<String> articlesFavoritedByUser(@Param("userId") String userId);
}
