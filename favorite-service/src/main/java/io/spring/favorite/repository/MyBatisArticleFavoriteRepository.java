package io.spring.favorite.repository;

import io.spring.favorite.core.ArticleFavorite;
import io.spring.favorite.core.ArticleFavoriteRepository;
import io.spring.favorite.mapper.ArticleFavoriteMapper;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class MyBatisArticleFavoriteRepository implements ArticleFavoriteRepository {
  private ArticleFavoriteMapper mapper;

  @Autowired
  public MyBatisArticleFavoriteRepository(ArticleFavoriteMapper mapper) {
    this.mapper = mapper;
  }

  @Override
  public void save(ArticleFavorite articleFavorite) {
    if (mapper.find(articleFavorite.getArticleId(), articleFavorite.getUserId()) == null) {
      mapper.insert(articleFavorite);
    }
  }

  @Override
  public Optional<ArticleFavorite> find(String articleId, String userId) {
    return Optional.ofNullable(mapper.find(articleId, userId));
  }

  @Override
  public void remove(ArticleFavorite favorite) {
    mapper.delete(favorite);
  }
}
