package io.spring.favoritesservice.infrastructure.repository;

import io.spring.favoritesservice.core.ArticleFavorite;
import io.spring.favoritesservice.core.ArticleFavoriteRepository;
import io.spring.favoritesservice.infrastructure.mybatis.mapper.ArticleFavoriteMapper;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class MyBatisArticleFavoriteRepository implements ArticleFavoriteRepository {
  private final ArticleFavoriteMapper mapper;

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

  @Override
  public int countByArticleId(String articleId) {
    return mapper.countByArticleId(articleId);
  }

  @Override
  public boolean isUserFavorite(String articleId, String userId) {
    return mapper.find(articleId, userId) != null;
  }
}
