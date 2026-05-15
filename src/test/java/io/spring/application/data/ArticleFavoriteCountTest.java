package io.spring.application.data;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class ArticleFavoriteCountTest {

  @Test
  public void should_create_article_favorite_count() {
    ArticleFavoriteCount count = new ArticleFavoriteCount("articleId", 5);
    assertEquals("articleId", count.getId());
    assertEquals(5, count.getCount());
  }

  @Test
  public void should_have_zero_count() {
    ArticleFavoriteCount count = new ArticleFavoriteCount("articleId", 0);
    assertEquals(0, count.getCount());
  }

  @Test
  public void should_implement_equals() {
    ArticleFavoriteCount c1 = new ArticleFavoriteCount("id", 5);
    ArticleFavoriteCount c2 = new ArticleFavoriteCount("id", 5);
    assertEquals(c1, c2);
  }

  @Test
  public void should_not_equal_different_count() {
    ArticleFavoriteCount c1 = new ArticleFavoriteCount("id1", 5);
    ArticleFavoriteCount c2 = new ArticleFavoriteCount("id2", 10);
    assertNotEquals(c1, c2);
  }

  @Test
  public void should_implement_hashcode() {
    ArticleFavoriteCount c1 = new ArticleFavoriteCount("id", 5);
    ArticleFavoriteCount c2 = new ArticleFavoriteCount("id", 5);
    assertEquals(c1.hashCode(), c2.hashCode());
  }

  @Test
  public void should_implement_tostring() {
    ArticleFavoriteCount count = new ArticleFavoriteCount("articleId", 5);
    String str = count.toString();
    assertNotNull(str);
    assertTrue(str.contains("articleId"));
  }

  @Test
  public void should_equal_self() {
    ArticleFavoriteCount count = new ArticleFavoriteCount("id", 5);
    assertEquals(count, count);
  }

  @Test
  public void should_not_equal_null() {
    ArticleFavoriteCount count = new ArticleFavoriteCount("id", 5);
    assertNotEquals(null, count);
  }
}
