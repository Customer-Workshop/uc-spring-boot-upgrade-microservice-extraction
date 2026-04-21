package io.spring.core.favorite;

import java.util.Optional;

// TODO: This repository has been extracted to the Favorite Service (favorite-service/).
// Once the Favorite Service is fully deployed, remove this interface and replace all usages
// with HTTP calls to the Favorite Service API.
public interface ArticleFavoriteRepository {
  void save(ArticleFavorite articleFavorite);

  Optional<ArticleFavorite> find(String articleId, String userId);

  void remove(ArticleFavorite favorite);
}
