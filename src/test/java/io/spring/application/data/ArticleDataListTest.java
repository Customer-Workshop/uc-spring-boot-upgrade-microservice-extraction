package io.spring.application.data;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.Arrays;
import org.junit.jupiter.api.Test;

public class ArticleDataListTest {

  @Test
  public void should_create_article_data_list() {
    ArticleData article = new ArticleData();
    article.setTitle("Test");
    ArticleDataList list = new ArticleDataList(Arrays.asList(article), 1);
    assertEquals(1, list.getArticleDatas().size());
    assertEquals(1, list.getCount());
  }

  @Test
  public void should_create_empty_article_data_list() {
    ArticleDataList list = new ArticleDataList(new ArrayList<>(), 0);
    assertTrue(list.getArticleDatas().isEmpty());
    assertEquals(0, list.getCount());
  }

  @Test
  public void should_hold_count_independent_of_list_size() {
    ArticleData article = new ArticleData();
    ArticleDataList list = new ArticleDataList(Arrays.asList(article), 100);
    assertEquals(1, list.getArticleDatas().size());
    assertEquals(100, list.getCount());
  }
}
