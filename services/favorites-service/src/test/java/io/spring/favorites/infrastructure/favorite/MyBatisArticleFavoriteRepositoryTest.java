package io.spring.favorites.infrastructure.favorite;

import io.spring.favorites.core.favorite.ArticleFavorite;
import io.spring.favorites.core.favorite.ArticleFavoriteRepository;
import io.spring.favorites.infrastructure.DbTestBase;
import io.spring.favorites.infrastructure.mybatis.mapper.ArticleFavoriteMapper;
import io.spring.favorites.infrastructure.repository.MyBatisArticleFavoriteRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;

@Import({MyBatisArticleFavoriteRepository.class})
public class MyBatisArticleFavoriteRepositoryTest extends DbTestBase {
  @Autowired private ArticleFavoriteRepository articleFavoriteRepository;

  @Autowired private ArticleFavoriteMapper articleFavoriteMapper;

  @Test
  public void should_save_and_fetch_articleFavorite_success() {
    ArticleFavorite articleFavorite = new ArticleFavorite("123", "456");
    articleFavoriteRepository.save(articleFavorite);
    Assertions.assertNotNull(
        articleFavoriteMapper.find(articleFavorite.getArticleId(), articleFavorite.getUserId()));
  }

  @Test
  public void should_remove_favorite_success() {
    ArticleFavorite articleFavorite = new ArticleFavorite("123", "456");
    articleFavoriteRepository.save(articleFavorite);
    articleFavoriteRepository.remove(articleFavorite);
    Assertions.assertFalse(articleFavoriteRepository.find("123", "456").isPresent());
  }
}
