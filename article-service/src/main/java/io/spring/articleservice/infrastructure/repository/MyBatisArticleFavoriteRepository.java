package io.spring.articleservice.infrastructure.repository;

import io.spring.articleservice.core.favorite.ArticleFavorite;
import io.spring.articleservice.core.favorite.ArticleFavoriteRepository;
import io.spring.articleservice.infrastructure.mybatis.mapper.ArticleFavoriteMapper;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class MyBatisArticleFavoriteRepository implements ArticleFavoriteRepository {
  private final ArticleFavoriteMapper articleFavoriteMapper;

  public MyBatisArticleFavoriteRepository(ArticleFavoriteMapper articleFavoriteMapper) {
    this.articleFavoriteMapper = articleFavoriteMapper;
  }

  @Override
  public void save(ArticleFavorite articleFavorite) {
    articleFavoriteMapper.insert(articleFavorite);
  }

  @Override
  public Optional<ArticleFavorite> find(String articleId, String userId) {
    return Optional.ofNullable(articleFavoriteMapper.find(articleId, userId));
  }

  @Override
  public void remove(ArticleFavorite favorite) {
    articleFavoriteMapper.delete(favorite);
  }
}
