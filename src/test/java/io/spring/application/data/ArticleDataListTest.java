package io.spring.application.data;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.Collections;
import org.joda.time.DateTime;
import org.junit.jupiter.api.Test;

public class ArticleDataListTest {

  @Test
  public void should_create_with_articles_and_count() {
    DateTime now = new DateTime();
    ArticleData article =
        new ArticleData("id", "slug", "title", "desc", "body", false, 0, now, now, null, null);
    ArticleDataList list = new ArticleDataList(Arrays.asList(article), 1);

    assertEquals(1, list.getArticleDatas().size());
    assertEquals(1, list.getCount());
  }

  @Test
  public void should_create_empty_list() {
    ArticleDataList list = new ArticleDataList(Collections.emptyList(), 0);

    assertTrue(list.getArticleDatas().isEmpty());
    assertEquals(0, list.getCount());
  }
}
