package io.spring.article.infrastructure.repository;

import io.spring.article.core.favorite.ArticleFavorite;
import io.spring.article.core.favorite.ArticleFavoriteRepository;
import io.spring.article.infrastructure.mybatis.mapper.ArticleFavoriteMapper;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class MyBatisArticleFavoriteRepository implements ArticleFavoriteRepository {
  private ArticleFavoriteMapper articleFavoriteMapper;

  public MyBatisArticleFavoriteRepository(ArticleFavoriteMapper articleFavoriteMapper) {
    this.articleFavoriteMapper = articleFavoriteMapper;
  }

  @Override
  public void save(ArticleFavorite articleFavorite) {
    if (articleFavoriteMapper.find(articleFavorite.getArticleId(), articleFavorite.getUserId())
        == null) {
      articleFavoriteMapper.insert(articleFavorite);
    }
  }

  @Override
  public Optional<ArticleFavorite> find(String articleId, String userId) {
    return Optional.ofNullable(articleFavoriteMapper.find(articleId, userId));
  }

  @Override
  public void remove(ArticleFavorite articleFavorite) {
    articleFavoriteMapper.delete(articleFavorite);
  }
}
