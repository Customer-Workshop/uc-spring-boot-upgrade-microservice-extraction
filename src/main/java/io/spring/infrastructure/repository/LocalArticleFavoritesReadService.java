package io.spring.infrastructure.repository;

import io.spring.application.ArticleFavoritesReadServiceInterface;
import io.spring.application.data.ArticleFavoriteCount;
import io.spring.core.user.User;
import io.spring.infrastructure.mybatis.readservice.ArticleFavoritesReadService;
import java.util.List;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile("!microservice")
public class LocalArticleFavoritesReadService implements ArticleFavoritesReadServiceInterface {
  private final ArticleFavoritesReadService articleFavoritesReadService;

  @Autowired
  public LocalArticleFavoritesReadService(ArticleFavoritesReadService articleFavoritesReadService) {
    this.articleFavoritesReadService = articleFavoritesReadService;
  }

  @Override
  public boolean isUserFavorite(String userId, String articleId) {
    return articleFavoritesReadService.isUserFavorite(userId, articleId);
  }

  @Override
  public int articleFavoriteCount(String articleId) {
    return articleFavoritesReadService.articleFavoriteCount(articleId);
  }

  @Override
  public List<ArticleFavoriteCount> articlesFavoriteCount(List<String> ids) {
    return articleFavoritesReadService.articlesFavoriteCount(ids);
  }

  @Override
  public Set<String> userFavorites(List<String> ids, User currentUser) {
    return articleFavoritesReadService.userFavorites(ids, currentUser);
  }
}
