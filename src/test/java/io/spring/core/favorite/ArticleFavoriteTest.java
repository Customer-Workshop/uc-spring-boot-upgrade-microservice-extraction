package io.spring.core.favorite;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class ArticleFavoriteTest {

  @Test
  void should_create_article_favorite() {
    ArticleFavorite favorite = new ArticleFavorite("article-id", "user-id");

    assertEquals("article-id", favorite.getArticleId());
    assertEquals("user-id", favorite.getUserId());
  }

  @Test
  void should_support_no_args_constructor() {
    ArticleFavorite favorite = new ArticleFavorite();
    assertNull(favorite.getArticleId());
    assertNull(favorite.getUserId());
  }

  @Test
  void should_support_equals() {
    ArticleFavorite fav1 = new ArticleFavorite("art1", "usr1");
    ArticleFavorite fav2 = new ArticleFavorite("art1", "usr1");

    assertEquals(fav1, fav2);
    assertEquals(fav1.hashCode(), fav2.hashCode());
  }

  @Test
  void should_not_equal_different_favorites() {
    ArticleFavorite fav1 = new ArticleFavorite("art1", "usr1");
    ArticleFavorite fav2 = new ArticleFavorite("art2", "usr1");

    assertNotEquals(fav1, fav2);
  }

  @Test
  void should_store_article_and_user_ids() {
    ArticleFavorite fav = new ArticleFavorite("my-article", "my-user");

    assertEquals("my-article", fav.getArticleId());
    assertEquals("my-user", fav.getUserId());
  }
}
