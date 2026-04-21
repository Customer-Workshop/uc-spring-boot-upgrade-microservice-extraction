package io.spring.favorite.client;

import java.util.Optional;

public interface ArticleServiceClient {
  Optional<ArticleData> findBySlug(String slug);
}
