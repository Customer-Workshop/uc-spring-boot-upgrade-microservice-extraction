package io.spring.application.data;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.Arrays;
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

    ArticleDataList list = new ArticleDataList(articles, 10);

    assertEquals(1, list.getArticleDatas().size());
    assertEquals(10, list.getCount());
  }

  @Test
  void should_create_with_empty_list() {
    ArticleDataList list = new ArticleDataList(new ArrayList<>(), 0);

    assertEquals(0, list.getArticleDatas().size());
    assertEquals(0, list.getCount());
  }

  @Test
  void should_return_correct_count_different_from_list_size() {
    DateTime now = new DateTime();
    ArticleData a1 =
        new ArticleData("id1", "slug1", "t1", "d1", "b1", false, 0, now, now, null, null);
    ArticleData a2 =
        new ArticleData("id2", "slug2", "t2", "d2", "b2", false, 0, now, now, null, null);

    ArticleDataList list = new ArticleDataList(Arrays.asList(a1, a2), 100);

    assertEquals(2, list.getArticleDatas().size());
    assertEquals(100, list.getCount());
  }
}
