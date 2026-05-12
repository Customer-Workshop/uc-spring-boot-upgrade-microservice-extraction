package io.spring.application.data;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class ArticleFavoriteCountTest {

  @Test
  void should_create_with_id_and_count() {
    ArticleFavoriteCount afc = new ArticleFavoriteCount("articleId", 5);
    assertEquals("articleId", afc.getId());
    assertEquals(5, afc.getCount());
  }

  @Test
  void should_create_with_zero_count() {
    ArticleFavoriteCount afc = new ArticleFavoriteCount("id", 0);
    assertEquals(0, afc.getCount());
  }

  @Test
  void should_have_equals_and_hashcode() {
    ArticleFavoriteCount a1 = new ArticleFavoriteCount("id", 5);
    ArticleFavoriteCount a2 = new ArticleFavoriteCount("id", 5);
    assertEquals(a1, a2);
    assertEquals(a1.hashCode(), a2.hashCode());
  }

  @Test
  void should_have_toString() {
    ArticleFavoriteCount afc = new ArticleFavoriteCount("id", 3);
    assertNotNull(afc.toString());
    assertTrue(afc.toString().contains("id"));
  }
}
