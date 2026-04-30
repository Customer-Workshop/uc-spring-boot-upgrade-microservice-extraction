package io.spring.application.data;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class ArticleFavoriteCountTest {

  @Test
  public void should_create_article_favorite_count() {
    ArticleFavoriteCount afc = new ArticleFavoriteCount("id1", 5);
    assertEquals("id1", afc.getId());
    assertEquals(5, afc.getCount());
  }

  @Test
  public void should_create_with_zero_count() {
    ArticleFavoriteCount afc = new ArticleFavoriteCount("id2", 0);
    assertEquals("id2", afc.getId());
    assertEquals(0, afc.getCount());
  }
}
