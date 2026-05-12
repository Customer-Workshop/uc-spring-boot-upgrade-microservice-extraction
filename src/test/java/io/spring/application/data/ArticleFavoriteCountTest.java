package io.spring.application.data;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class ArticleFavoriteCountTest {

  @Test
  void should_create_article_favorite_count() {
    ArticleFavoriteCount count = new ArticleFavoriteCount("article-id", 5);

    assertEquals("article-id", count.getId());
    assertEquals(5, count.getCount());
  }

  @Test
  void should_create_with_zero_count() {
    ArticleFavoriteCount count = new ArticleFavoriteCount("id", 0);

    assertEquals("id", count.getId());
    assertEquals(0, count.getCount());
  }

  @Test
  void should_support_equals_and_hashcode() {
    ArticleFavoriteCount c1 = new ArticleFavoriteCount("id", 3);
    ArticleFavoriteCount c2 = new ArticleFavoriteCount("id", 3);

    assertEquals(c1, c2);
    assertEquals(c1.hashCode(), c2.hashCode());
  }

  @Test
  void should_not_equal_different_counts() {
    ArticleFavoriteCount c1 = new ArticleFavoriteCount("id", 3);
    ArticleFavoriteCount c2 = new ArticleFavoriteCount("id", 5);

    assertNotEquals(c1, c2);
  }
}
