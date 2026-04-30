package io.spring.application.data;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class ArticleFavoriteCountTest {

  @Test
  public void should_create_with_values() {
    ArticleFavoriteCount count = new ArticleFavoriteCount("article-id", 5);

    assertEquals("article-id", count.getId());
    assertEquals(5, count.getCount());
  }

  @Test
  public void should_have_equals_and_hashcode() {
    ArticleFavoriteCount count1 = new ArticleFavoriteCount("id", 5);
    ArticleFavoriteCount count2 = new ArticleFavoriteCount("id", 5);
    ArticleFavoriteCount count3 = new ArticleFavoriteCount("id2", 10);

    assertEquals(count1, count2);
    assertNotEquals(count1, count3);
    assertEquals(count1.hashCode(), count2.hashCode());
  }

  @Test
  public void should_have_to_string() {
    ArticleFavoriteCount count = new ArticleFavoriteCount("article-id", 5);
    String str = count.toString();
    assertNotNull(str);
    assertTrue(str.contains("article-id"));
    assertTrue(str.contains("5"));
  }
}
