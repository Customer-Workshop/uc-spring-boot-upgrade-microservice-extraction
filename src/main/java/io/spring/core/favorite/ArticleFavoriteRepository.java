package io.spring.core.favorite;

import java.util.Optional;

public interface ArticleFavoriteRepository {
  /** Saves an article favorite. */
  void save(ArticleFavorite articleFavorite);

  /** Finds a favorite for an article and user. */
  Optional<ArticleFavorite> find(String articleId, String userId);

  /** Removes an article favorite. */
  void remove(ArticleFavorite favorite);
}
