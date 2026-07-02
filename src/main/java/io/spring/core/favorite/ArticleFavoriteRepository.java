package io.spring.core.favorite;

import java.util.Optional;

/** Repository interface for {@link ArticleFavorite} relationship persistence. */
public interface ArticleFavoriteRepository {

  /**
   * Saves a user-article favorite relationship. Duplicate favorites are silently ignored.
   *
   * @param articleFavorite the favorite relationship to persist
   */
  void save(ArticleFavorite articleFavorite);

  /**
   * Finds a favorite relationship between a user and an article.
   *
   * @param articleId the article's unique identifier
   * @param userId the user's unique identifier
   * @return the favorite relationship, or empty if the user has not favorited the article
   */
  Optional<ArticleFavorite> find(String articleId, String userId);

  /**
   * Removes a favorite relationship from the data store.
   *
   * @param favorite the favorite relationship to remove
   */
  void remove(ArticleFavorite favorite);
}
