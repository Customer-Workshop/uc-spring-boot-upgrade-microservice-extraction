package io.spring.core.article;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.Arrays;
import org.joda.time.DateTime;
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
  public void should_update_title_description_and_body_when_values_are_not_empty() {
    Article article =
        new Article("old title", "old desc", "old body", Arrays.asList("java"), "123");

    article.update("new title", "new desc", "new body");

    assertThat(article.getTitle(), is("new title"));
    assertThat(article.getSlug(), is("new-title"));
    assertThat(article.getDescription(), is("new desc"));
    assertThat(article.getBody(), is("new body"));
  }

  @Test
  public void should_keep_current_values_when_update_values_are_empty() {
    Article article =
        new Article("old title", "old desc", "old body", Arrays.asList("java"), "123");

    article.update("", null, "");

    assertThat(article.getTitle(), is("old title"));
    assertThat(article.getSlug(), is("old-title"));
    assertThat(article.getDescription(), is("old desc"));
    assertThat(article.getBody(), is("old body"));
  }

  @Test
  public void should_update_only_the_provided_fields() {
    Article article =
        new Article("old title", "old desc", "old body", Arrays.asList("java"), "123");

    article.update("", "new desc", "");
    assertThat(article.getTitle(), is("old title"));
    assertThat(article.getDescription(), is("new desc"));
    assertThat(article.getBody(), is("old body"));

    article.update("", "", "new body");
    assertThat(article.getDescription(), is("new desc"));
    assertThat(article.getBody(), is("new body"));
  }

  @Test
  public void should_refresh_updated_at_only_when_something_changed() {
    Article article =
        new Article(
            "old title",
            "old desc",
            "old body",
            Arrays.asList("java"),
            "123",
            new DateTime(2020, 1, 1, 0, 0));
    DateTime originalUpdatedAt = article.getUpdatedAt();

    article.update("", "", "");
    assertThat(article.getUpdatedAt(), is(originalUpdatedAt));

    article.update("", "new desc", "");
    assertThat(article.getUpdatedAt().isAfter(originalUpdatedAt), is(true));
  }
}
