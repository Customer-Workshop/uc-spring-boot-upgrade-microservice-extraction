package io.spring.application.data;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class ArticleFavoriteCountTest {

  @Test
  public void should_create_with_id_and_count() {
    ArticleFavoriteCount count = new ArticleFavoriteCount("article-id", 5);

    assertEquals("article-id", count.getId());
    assertEquals(5, count.getCount());
  }

  @Test
  public void should_implement_equals_and_hashcode() {
    ArticleFavoriteCount count1 = new ArticleFavoriteCount("id", 5);
    ArticleFavoriteCount count2 = new ArticleFavoriteCount("id", 5);

    assertEquals(count1, count2);
    assertEquals(count1.hashCode(), count2.hashCode());
  }

  @Test
  public void should_not_equal_different_count() {
    ArticleFavoriteCount count1 = new ArticleFavoriteCount("id", 5);
    ArticleFavoriteCount count2 = new ArticleFavoriteCount("id", 10);

    assertNotEquals(count1, count2);
  }

  @Test
  public void should_implement_toString() {
    ArticleFavoriteCount count = new ArticleFavoriteCount("id", 5);

    String str = count.toString();

    assertNotNull(str);
    assertTrue(str.contains("id"));
    assertTrue(str.contains("5"));
  }
}
