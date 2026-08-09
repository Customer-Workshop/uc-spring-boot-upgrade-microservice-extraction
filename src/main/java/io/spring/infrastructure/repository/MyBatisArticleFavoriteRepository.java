package io.spring.infrastructure.repository;

import io.spring.core.favorite.ArticleFavorite;
import io.spring.core.favorite.ArticleFavoriteRepository;
import io.spring.infrastructure.mybatis.mapper.ArticleFavoriteMapper;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

/** Persists article favorites through the MyBatis mapper. */
@Repository
public class MyBatisArticleFavoriteRepository implements ArticleFavoriteRepository {
  private ArticleFavoriteMapper mapper;

  @Autowired
  public MyBatisArticleFavoriteRepository(ArticleFavoriteMapper mapper) {
    this.mapper = mapper;
  }

  /** Inserts a favorite only when the article-user pair is not already present. */
  @Override
  public void save(ArticleFavorite articleFavorite) {
    if (mapper.find(articleFavorite.getArticleId(), articleFavorite.getUserId()) == null) {
      mapper.insert(articleFavorite);
    }
  }

  /** Finds a favorite by article and user identifiers. */
  @Override
  public Optional<ArticleFavorite> find(String articleId, String userId) {
    return Optional.ofNullable(mapper.find(articleId, userId));
  }

  /** Deletes the supplied favorite. */
  @Override
  public void remove(ArticleFavorite favorite) {
    mapper.delete(favorite);
  }
}
