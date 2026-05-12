package io.spring.application.data;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class ArticleFavoriteCountTest {

  @Test
  void should_create_with_id_and_count() {
    ArticleFavoriteCount afc = new ArticleFavoriteCount("article-id", 42);

    assertEquals("article-id", afc.getId());
    assertEquals(42, afc.getCount());
  }

  @Test
  void should_handle_zero_count() {
    ArticleFavoriteCount afc = new ArticleFavoriteCount("id", 0);

    assertEquals(0, afc.getCount());
  }

  @Test
  void should_implement_equals_for_value_class() {
    ArticleFavoriteCount afc1 = new ArticleFavoriteCount("id", 5);
    ArticleFavoriteCount afc2 = new ArticleFavoriteCount("id", 5);

    assertEquals(afc1, afc2);
    assertEquals(afc1.hashCode(), afc2.hashCode());
  }

  @Test
  void should_not_equal_different_values() {
    ArticleFavoriteCount afc1 = new ArticleFavoriteCount("id1", 5);
    ArticleFavoriteCount afc2 = new ArticleFavoriteCount("id2", 10);

    assertNotEquals(afc1, afc2);
  }

  @Test
  void should_implement_toString() {
    ArticleFavoriteCount afc = new ArticleFavoriteCount("id", 5);
    assertNotNull(afc.toString());
    assertTrue(afc.toString().contains("5"));
  }
}
