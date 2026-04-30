package io.spring.core.favorite;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class ArticleFavoriteTest {

  @Test
  public void should_create_article_favorite() {
    ArticleFavorite fav = new ArticleFavorite("article1", "user1");
    assertEquals("article1", fav.getArticleId());
    assertEquals("user1", fav.getUserId());
  }

  @Test
  public void should_equal_same_ids() {
    ArticleFavorite fav1 = new ArticleFavorite("article1", "user1");
    ArticleFavorite fav2 = new ArticleFavorite("article1", "user1");
    assertEquals(fav1, fav2);
    assertEquals(fav1.hashCode(), fav2.hashCode());
  }

  @Test
  public void should_not_equal_different_ids() {
    ArticleFavorite fav1 = new ArticleFavorite("article1", "user1");
    ArticleFavorite fav2 = new ArticleFavorite("article2", "user1");
    assertNotEquals(fav1, fav2);
  }
}
