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
    ProfileData profile = new ProfileData("uid", "user", "", "", false);
    ArticleData article1 =
        new ArticleData(
            "id1",
            "slug1",
            "title1",
            "desc1",
            "body1",
            false,
            0,
            now,
            now,
            Collections.emptyList(),
            profile);
    ArticleData article2 =
        new ArticleData(
            "id2",
            "slug2",
            "title2",
            "desc2",
            "body2",
            false,
            0,
            now,
            now,
            Collections.emptyList(),
            profile);
    List<ArticleData> articles = Arrays.asList(article1, article2);

    ArticleDataList list = new ArticleDataList(articles, 10);

    assertEquals(2, list.getArticleDatas().size());
    assertEquals(10, list.getCount());
  }

  @Test
  void should_handle_empty_list() {
    ArticleDataList list = new ArticleDataList(Collections.emptyList(), 0);

    assertTrue(list.getArticleDatas().isEmpty());
    assertEquals(0, list.getCount());
  }

  @Test
  void should_allow_count_different_from_list_size() {
    DateTime now = new DateTime();
    ProfileData profile = new ProfileData("uid", "user", "", "", false);
    ArticleData article =
        new ArticleData(
            "id1",
            "slug1",
            "title1",
            "desc1",
            "body1",
            false,
            0,
            now,
            now,
            Collections.emptyList(),
            profile);

    ArticleDataList list = new ArticleDataList(Collections.singletonList(article), 100);

    assertEquals(1, list.getArticleDatas().size());
    assertEquals(100, list.getCount());
  }
}
