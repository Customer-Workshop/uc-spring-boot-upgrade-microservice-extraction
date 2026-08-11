package io.spring.core.article;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
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
  public void should_deduplicate_tags() {
    Article article =
        new Article("title", "desc", "body", Arrays.asList("java", "java", "spring"), "123");
    assertThat(article.getTags().size(), is(2));
  }

  @Test
  public void should_update_title_description_and_body() {
    Article article = newArticle();

    article.update("new title", "new desc", "new body");

    assertThat(article.getTitle(), is("new title"));
    assertThat(article.getSlug(), is("new-title"));
    assertThat(article.getDescription(), is("new desc"));
    assertThat(article.getBody(), is("new body"));
    assertThat(article.getUpdatedAt(), not(is(article.getCreatedAt())));
  }

  @Test
  public void should_not_update_title_with_empty_value() {
    Article article = newArticle();

    article.update("", "new desc", "new body");

    assertThat(article.getTitle(), is("old title"));
    assertThat(article.getSlug(), is("old-title"));
  }

  @Test
  public void should_not_update_description_with_empty_value() {
    Article article = newArticle();

    article.update("new title", "", "new body");

    assertThat(article.getDescription(), is("old desc"));
  }

  @Test
  public void should_not_update_body_with_empty_value() {
    Article article = newArticle();

    article.update("new title", "new desc", "");

    assertThat(article.getBody(), is("old body"));
  }

  @Test
  public void should_not_update_anything_with_null_values() {
    Article article = newArticle();
    DateTime updatedAt = article.getUpdatedAt();

    article.update(null, null, null);

    assertThat(article.getTitle(), is("old title"));
    assertThat(article.getDescription(), is("old desc"));
    assertThat(article.getBody(), is("old body"));
    assertThat(article.getUpdatedAt(), is(updatedAt));
  }

  @Test
  public void should_only_be_equal_to_article_with_same_id() {
    Article article = newArticle();

    assertThat(article.equals(article), is(true));
    assertThat(article.equals(newArticle()), is(false));
    assertThat(article.equals(null), is(false));
    assertThat(article.equals("not an article"), is(false));
    assertThat(article.equals(new Article()), is(false));
    assertThat(new Article().equals(article), is(false));
    assertThat(new Article().equals(new Article()), is(true));
  }

  @Test
  public void should_have_hash_code_consistent_with_id() {
    Article article = newArticle();

    assertThat(article.hashCode(), is(article.hashCode()));
    assertThat(article.hashCode(), not(is(0)));
    assertThat(article.hashCode(), not(is(newArticle().hashCode())));
    assertThat(new Article().hashCode(), notNullValue());
  }

  private Article newArticle() {
    return new Article(
        "old title",
        "old desc",
        "old body",
        Arrays.asList("java"),
        "123",
        new DateTime(2020, 1, 1, 0, 0));
  }
}
