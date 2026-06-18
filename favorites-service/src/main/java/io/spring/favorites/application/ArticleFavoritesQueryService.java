package io.spring.favorites.application;

import io.spring.favorites.application.data.ArticleFavoriteCount;
import io.spring.favorites.infrastructure.mybatis.readservice.ArticleFavoritesReadService;
import java.util.List;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ArticleFavoritesQueryService {
  private final ArticleFavoritesReadService articleFavoritesReadService;

  @Autowired
  public ArticleFavoritesQueryService(ArticleFavoritesReadService articleFavoritesReadService) {
    this.articleFavoritesReadService = articleFavoritesReadService;
  }

  public boolean isUserFavorite(String userId, String articleId) {
    return articleFavoritesReadService.isUserFavorite(userId, articleId);
  }

  public int articleFavoriteCount(String articleId) {
    return articleFavoritesReadService.articleFavoriteCount(articleId);
  }

  public List<ArticleFavoriteCount> articlesFavoriteCount(List<String> ids) {
    return articleFavoritesReadService.articlesFavoriteCount(ids);
  }

  public Set<String> userFavorites(List<String> ids, String userId) {
    return articleFavoritesReadService.userFavorites(ids, userId);
  }
}
