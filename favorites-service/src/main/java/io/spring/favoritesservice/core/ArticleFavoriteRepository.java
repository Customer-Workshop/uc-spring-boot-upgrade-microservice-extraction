package io.spring.favoritesservice.core;

import java.util.Optional;

public interface ArticleFavoriteRepository {
  void save(ArticleFavorite articleFavorite);

  Optional<ArticleFavorite> find(String articleId, String userId);

  void remove(ArticleFavorite favorite);

  int countByArticleId(String articleId);

  boolean isUserFavorite(String articleId, String userId);
}
