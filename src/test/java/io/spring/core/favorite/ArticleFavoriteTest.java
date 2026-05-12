package io.spring.core.favorite;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class ArticleFavoriteTest {

  @Test
  public void should_create_with_constructor() {
    ArticleFavorite fav = new ArticleFavorite("article-id", "user-id");

    assertEquals("article-id", fav.getArticleId());
    assertEquals("user-id", fav.getUserId());
  }

  @Test
  public void should_create_with_no_args_constructor() {
    ArticleFavorite fav = new ArticleFavorite();

    assertNull(fav.getArticleId());
    assertNull(fav.getUserId());
  }

  @Test
  public void should_implement_equals_and_hashcode() {
    ArticleFavorite fav1 = new ArticleFavorite("article-id", "user-id");
    ArticleFavorite fav2 = new ArticleFavorite("article-id", "user-id");

    assertEquals(fav1, fav2);
    assertEquals(fav1.hashCode(), fav2.hashCode());
  }

  @Test
  public void should_not_equal_different_favorite() {
    ArticleFavorite fav1 = new ArticleFavorite("article-1", "user-1");
    ArticleFavorite fav2 = new ArticleFavorite("article-2", "user-2");

    assertNotEquals(fav1, fav2);
  }
}
