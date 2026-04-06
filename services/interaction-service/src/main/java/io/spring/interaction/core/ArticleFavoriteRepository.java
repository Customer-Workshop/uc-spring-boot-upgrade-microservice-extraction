package io.spring.interaction.core;

import java.util.Optional;

public interface ArticleFavoriteRepository {
  void save(ArticleFavorite articleFavorite);

  Optional<ArticleFavorite> find(String articleId, String userId);

  void remove(ArticleFavorite articleFavorite);

  int countByArticleId(String articleId);
}
