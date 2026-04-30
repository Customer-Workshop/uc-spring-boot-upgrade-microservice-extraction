package io.spring.application.data;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.joda.time.DateTime;
import org.junit.jupiter.api.Test;

public class ArticleDataListTest {

  @Test
  public void should_create_with_articles_and_count() {
    DateTime now = new DateTime();
    ArticleData article =
        new ArticleData("id", "slug", "title", "desc", "body", false, 0, now, now, null, null);
    List<ArticleData> articles = Arrays.asList(article);

    ArticleDataList list = new ArticleDataList(articles, 1);

    assertNotNull(list);
    assertEquals(1, list.getCount());
    assertEquals(1, list.getArticleDatas().size());
    assertEquals("id", list.getArticleDatas().get(0).getId());
  }

  @Test
  public void should_create_empty_list() {
    ArticleDataList list = new ArticleDataList(Collections.emptyList(), 0);

    assertNotNull(list);
    assertEquals(0, list.getCount());
    assertTrue(list.getArticleDatas().isEmpty());
  }

  @Test
  public void should_have_different_count_from_list_size() {
    DateTime now = new DateTime();
    ArticleData article =
        new ArticleData("id", "slug", "title", "desc", "body", false, 0, now, now, null, null);
    List<ArticleData> articles = Arrays.asList(article);

    ArticleDataList list = new ArticleDataList(articles, 100);

    assertEquals(1, list.getArticleDatas().size());
    assertEquals(100, list.getCount());
  }
}
