package io.spring.application.data;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.Collections;
import org.joda.time.DateTime;
import org.junit.jupiter.api.Test;

public class ArticleDataListTest {

  @Test
  public void should_create_article_data_list() {
    ArticleData article =
        new ArticleData(
            "id",
            "slug",
            "title",
            "desc",
            "body",
            false,
            0,
            new DateTime(),
            new DateTime(),
            Arrays.asList("java"),
            new ProfileData("uid", "user", "bio", "img", false));
    ArticleDataList list = new ArticleDataList(Arrays.asList(article), 1);

    assertEquals(1, list.getArticleDatas().size());
    assertEquals(1, list.getCount());
  }

  @Test
  public void should_create_empty_article_data_list() {
    ArticleDataList list = new ArticleDataList(Collections.emptyList(), 0);
    assertTrue(list.getArticleDatas().isEmpty());
    assertEquals(0, list.getCount());
  }
}
