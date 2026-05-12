package io.spring.application.data;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class ArticleFavoriteCountTest {

  @Test
  void should_create_with_id_and_count() {
    ArticleFavoriteCount count = new ArticleFavoriteCount("articleId", 5);
    assertEquals("articleId", count.getId());
    assertEquals(5, count.getCount());
  }

  @Test
  void should_create_with_zero_count() {
    ArticleFavoriteCount count = new ArticleFavoriteCount("id", 0);
    assertEquals(0, count.getCount());
  }

  @Test
  void should_support_equals() {
    ArticleFavoriteCount c1 = new ArticleFavoriteCount("id", 5);
    ArticleFavoriteCount c2 = new ArticleFavoriteCount("id", 5);
    assertEquals(c1, c2);
    assertEquals(c1.hashCode(), c2.hashCode());
  }

  @Test
  void should_not_equal_different_values() {
    ArticleFavoriteCount c1 = new ArticleFavoriteCount("id1", 5);
    ArticleFavoriteCount c2 = new ArticleFavoriteCount("id2", 10);
    assertNotEquals(c1, c2);
  }

  @Test
  void should_support_to_string() {
    ArticleFavoriteCount count = new ArticleFavoriteCount("id", 5);
    assertNotNull(count.toString());
  }
}
