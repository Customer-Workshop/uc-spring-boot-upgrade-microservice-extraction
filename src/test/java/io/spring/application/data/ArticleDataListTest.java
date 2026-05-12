package io.spring.application.data;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.joda.time.DateTime;
import org.junit.jupiter.api.Test;

public class ArticleDataListTest {

  @Test
  void should_create_article_data_list() {
    DateTime now = new DateTime();
    ProfileData profile = new ProfileData("id", "user", "bio", "img", false);
    ArticleData article =
        new ArticleData(
            "id", "slug", "title", "desc", "body", false, 0, now, now, Arrays.asList(), profile);
    List<ArticleData> articles = Arrays.asList(article);

    ArticleDataList list = new ArticleDataList(articles, 1);

    assertEquals(articles, list.getArticleDatas());
    assertEquals(1, list.getCount());
  }

  @Test
  void should_create_empty_article_data_list() {
    ArticleDataList list = new ArticleDataList(Collections.emptyList(), 0);

    assertTrue(list.getArticleDatas().isEmpty());
    assertEquals(0, list.getCount());
  }

  @Test
  void should_store_count_independently_of_list_size() {
    DateTime now = new DateTime();
    ProfileData profile = new ProfileData("id", "user", "bio", "img", false);
    ArticleData article =
        new ArticleData(
            "id", "slug", "title", "desc", "body", false, 0, now, now, Arrays.asList(), profile);

    ArticleDataList list = new ArticleDataList(Arrays.asList(article), 100);

    assertEquals(1, list.getArticleDatas().size());
    assertEquals(100, list.getCount());
  }
}
