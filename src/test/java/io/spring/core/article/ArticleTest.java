package io.spring.core.article;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.Arrays;
import org.junit.jupiter.api.Test;

public class ArticleTest {

  @Test
  public void should_get_right_slug() {
    Article article = new Article("a new   title", "desc", "body", Arrays.asList("java"), "123");
    assertThat(article.getSlug(), is("a-new-title"));
  }

  @Test
  public void should_get_right_slug_with_number_in_title() {
    Article article = new Article("a new title 2", "desc", "body", Arrays.asList("java"), "123");
    assertThat(article.getSlug(), is("a-new-title-2"));
  }

  @Test
  public void should_get_lower_case_slug() {
    Article article = new Article("A NEW TITLE", "desc", "body", Arrays.asList("java"), "123");
    assertThat(article.getSlug(), is("a-new-title"));
  }

  @Test
  public void should_handle_other_language() {
    Article article = new Article("中文：标题", "desc", "body", Arrays.asList("java"), "123");
    assertThat(article.getSlug(), is("中文-标题"));
  }

  @Test
  public void should_handle_commas() {
    Article article = new Article("what?the.hell,w", "desc", "body", Arrays.asList("java"), "123");
    assertThat(article.getSlug(), is("what-the-hell-w"));
  }

  @Test
  public void should_update_description_without_changing_other_fields_when_title_and_body_empty() {
    Article article =
        new Article("title", "old description", "old body", Arrays.asList("java"), "123");

    article.update("", "new description", "");

    assertThat(article.getTitle(), is("title"));
    assertThat(article.getSlug(), is("title"));
    assertThat(article.getDescription(), is("new description"));
    assertThat(article.getBody(), is("old body"));
  }

  @Test
  public void should_update_body_without_changing_other_fields_when_title_and_description_empty() {
    Article article =
        new Article("title", "old description", "old body", Arrays.asList("java"), "123");

    article.update("", "", "new body");

    assertThat(article.getTitle(), is("title"));
    assertThat(article.getDescription(), is("old description"));
    assertThat(article.getBody(), is("new body"));
  }

  @Test
  public void should_not_equal_articles_with_different_ids() {
    Article first = new Article("title", "desc", "body", Arrays.asList("java"), "123");
    Article second = new Article("title", "desc", "body", Arrays.asList("java"), "123");

    assertThat(first.equals(second), is(false));
  }
}
