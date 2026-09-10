package io.spring.favorites.infrastructure.favorite;

import io.spring.favorites.core.favorite.ArticleFavorite;
import io.spring.favorites.core.favorite.ArticleFavoriteRepository;
import io.spring.favorites.infrastructure.DbTestBase;
import io.spring.favorites.infrastructure.mybatis.readservice.ArticleFavoritesReadService;
import io.spring.favorites.infrastructure.repository.MyBatisArticleFavoriteRepository;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;

@Import({MyBatisArticleFavoriteRepository.class})
public class ArticleFavoritesReadServiceTest extends DbTestBase {
  @Autowired private ArticleFavoriteRepository articleFavoriteRepository;

  @Autowired private ArticleFavoritesReadService articleFavoritesReadService;

  @Test
  public void should_count_favorites_without_joining_articles() {
    articleFavoriteRepository.save(new ArticleFavorite("read-1", "reader-1"));
    articleFavoriteRepository.save(new ArticleFavorite("read-1", "reader-2"));
    articleFavoriteRepository.save(new ArticleFavorite("read-2", "reader-1"));

    Assertions.assertEquals(2, articleFavoritesReadService.articleFavoriteCount("read-1"));
    Assertions.assertTrue(articleFavoritesReadService.isUserFavorite("reader-1", "read-1"));
    Assertions.assertFalse(articleFavoritesReadService.isUserFavorite("reader-3", "read-1"));

    List<String> ids = Arrays.asList("read-1", "read-2", "read-3");
    Assertions.assertEquals(2, articleFavoritesReadService.articlesFavoriteCount(ids).size());

    Set<String> favorites = articleFavoritesReadService.userFavorites(ids, "reader-2");
    Assertions.assertEquals(1, favorites.size());
    Assertions.assertTrue(favorites.contains("read-1"));
  }
}
