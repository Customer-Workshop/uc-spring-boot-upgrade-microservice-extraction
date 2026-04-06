package io.spring.interaction.infrastructure.repository;

import io.spring.interaction.core.ArticleFavorite;
import io.spring.interaction.core.ArticleFavoriteRepository;
import io.spring.interaction.infrastructure.mybatis.mapper.ArticleFavoriteMapper;
import java.util.Optional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@AllArgsConstructor
public class MyBatisArticleFavoriteRepository implements ArticleFavoriteRepository {
  private ArticleFavoriteMapper articleFavoriteMapper;

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

  @Override
  public int countByArticleId(String articleId) {
    return articleFavoriteMapper.countByArticleId(articleId);
  }
}
