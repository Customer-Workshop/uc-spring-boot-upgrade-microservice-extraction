package io.spring.articleservice.infrastructure.repository;

import io.spring.articleservice.core.favorite.ArticleFavorite;
import io.spring.articleservice.core.favorite.ArticleFavoriteRepository;
import io.spring.articleservice.infrastructure.mybatis.mapper.ArticleFavoriteMapper;
import java.util.Optional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@AllArgsConstructor
public class MyBatisArticleFavoriteRepository implements ArticleFavoriteRepository {
  private ArticleFavoriteMapper articleFavoriteMapper;

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
