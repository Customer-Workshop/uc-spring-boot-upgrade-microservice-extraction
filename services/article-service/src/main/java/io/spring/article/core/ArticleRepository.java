package io.spring.article.core;

import java.util.Optional;

public interface ArticleRepository {
  void save(Article article);

  Optional<Article> findById(String id);

  Optional<Article> findBySlug(String slug);

  void remove(Article article);
}
