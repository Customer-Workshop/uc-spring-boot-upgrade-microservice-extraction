package io.spring.application.data;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.joda.time.DateTime;
import org.junit.jupiter.api.Test;

public class ArticleDataListTest {

  @Test
  void should_create_with_articles_and_count() {
    DateTime now = new DateTime();
    ArticleData article =
        new ArticleData("id", "slug", "title", "desc", "body", false, 0, now, now, null, null);
    List<ArticleData> articles = Arrays.asList(article);

    ArticleDataList list = new ArticleDataList(articles, 1);

    assertEquals(1, list.getArticleDatas().size());
    assertEquals(1, list.getCount());
  }

  @Test
  void should_create_empty_list() {
    ArticleDataList list = new ArticleDataList(Collections.emptyList(), 0);

    assertTrue(list.getArticleDatas().isEmpty());
    assertEquals(0, list.getCount());
  }

  @Test
  void should_count_differ_from_list_size() {
    ArticleDataList list = new ArticleDataList(Collections.emptyList(), 100);

    assertTrue(list.getArticleDatas().isEmpty());
    assertEquals(100, list.getCount());
  }
}
