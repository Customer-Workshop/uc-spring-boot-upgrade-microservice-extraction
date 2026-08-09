package io.spring.core.article;

import java.util.Optional;

public interface ArticleRepository {

  /** Saves a new article or persists changes to an existing article. */
  void save(Article article);

  /** Finds an article by identifier. */
  Optional<Article> findById(String id);

  /** Finds an article by slug. */
  Optional<Article> findBySlug(String slug);

  /** Removes an article. */
  void remove(Article article);
}
