package io.spring.core.favorite;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class ArticleFavoriteTest {

  @Test
  void should_create_article_favorite() {
    ArticleFavorite favorite = new ArticleFavorite("article123", "user456");

    assertEquals("article123", favorite.getArticleId());
    assertEquals("user456", favorite.getUserId());
  }

  @Test
  void should_be_equal_when_same_article_and_user() {
    ArticleFavorite fav1 = new ArticleFavorite("article1", "user1");
    ArticleFavorite fav2 = new ArticleFavorite("article1", "user1");

    assertEquals(fav1, fav2);
    assertEquals(fav1.hashCode(), fav2.hashCode());
  }

  @Test
  void should_not_be_equal_when_different_article() {
    ArticleFavorite fav1 = new ArticleFavorite("article1", "user1");
    ArticleFavorite fav2 = new ArticleFavorite("article2", "user1");

    assertNotEquals(fav1, fav2);
  }

  @Test
  void should_not_be_equal_when_different_user() {
    ArticleFavorite fav1 = new ArticleFavorite("article1", "user1");
    ArticleFavorite fav2 = new ArticleFavorite("article1", "user2");

    assertNotEquals(fav1, fav2);
  }
}
