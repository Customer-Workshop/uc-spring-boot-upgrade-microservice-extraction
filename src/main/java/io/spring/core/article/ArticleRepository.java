package io.spring.core.article;

import java.util.Optional;

/** Repository interface for {@link Article} aggregate persistence. */
public interface ArticleRepository {

  /**
   * Persists an article. Creates a new record if the article does not exist, or updates the
   * existing record.
   *
   * @param article the article entity to save
   */
  void save(Article article);

  /**
   * Finds an article by its unique identifier.
   *
   * @param id the article's unique identifier
   * @return the article, or empty if not found
   */
  Optional<Article> findById(String id);

  /**
   * Finds an article by its URL-friendly slug.
   *
   * @param slug the article's slug
   * @return the article, or empty if not found
   */
  Optional<Article> findBySlug(String slug);

  /**
   * Removes an article and its associated tag relations from the data store.
   *
   * @param article the article entity to remove
   */
  void remove(Article article);
}
