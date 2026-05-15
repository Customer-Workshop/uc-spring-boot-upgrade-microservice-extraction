package io.spring.core.favorite;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class ArticleFavoriteTest {

  @Test
  public void should_create_article_favorite() {
    ArticleFavorite favorite = new ArticleFavorite("articleId", "userId");
    assertEquals("articleId", favorite.getArticleId());
    assertEquals("userId", favorite.getUserId());
  }

  @Test
  public void should_create_empty_article_favorite() {
    ArticleFavorite favorite = new ArticleFavorite();
    assertNull(favorite.getArticleId());
    assertNull(favorite.getUserId());
  }

  @Test
  public void should_be_equal_with_same_fields() {
    ArticleFavorite fav1 = new ArticleFavorite("articleId", "userId");
    ArticleFavorite fav2 = new ArticleFavorite("articleId", "userId");
    assertEquals(fav1, fav2);
  }

  @Test
  public void should_not_be_equal_with_different_article_id() {
    ArticleFavorite fav1 = new ArticleFavorite("article1", "userId");
    ArticleFavorite fav2 = new ArticleFavorite("article2", "userId");
    assertNotEquals(fav1, fav2);
  }

  @Test
  public void should_not_be_equal_with_different_user_id() {
    ArticleFavorite fav1 = new ArticleFavorite("articleId", "user1");
    ArticleFavorite fav2 = new ArticleFavorite("articleId", "user2");
    assertNotEquals(fav1, fav2);
  }

  @Test
  public void should_implement_hashcode() {
    ArticleFavorite fav1 = new ArticleFavorite("articleId", "userId");
    ArticleFavorite fav2 = new ArticleFavorite("articleId", "userId");
    assertEquals(fav1.hashCode(), fav2.hashCode());
  }

  @Test
  public void should_equal_self() {
    ArticleFavorite fav = new ArticleFavorite("articleId", "userId");
    assertEquals(fav, fav);
  }

  @Test
  public void should_not_equal_null() {
    ArticleFavorite fav = new ArticleFavorite("articleId", "userId");
    assertNotEquals(null, fav);
  }
}
