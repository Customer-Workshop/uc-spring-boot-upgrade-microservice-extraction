package io.spring.core.article;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
  public void should_be_equal_to_itself_and_not_to_different_article() {
    Article article = new Article("title", "desc", "body", Arrays.asList("java"), "123");
    Article other = new Article("title", "desc", "body", Arrays.asList("java"), "123");
    assertTrue(article.equals(article));
    assertFalse(article.equals(other));
    assertFalse(article.equals(null));
    assertFalse(article.equals("not an article"));
  }

  @Test
  public void articles_with_null_ids_should_be_equal_with_same_hash_code() {
    Article first = new Article();
    Article second = new Article();
    assertEquals(first, second);
    assertEquals(first.hashCode(), second.hashCode());
  }

  @Test
  public void articles_with_different_ids_should_have_different_hash_codes() {
    Article article = new Article("title", "desc", "body", Arrays.asList("java"), "123");
    Article nullIdArticle = new Article();
    assertNotEquals(article.hashCode(), nullIdArticle.hashCode());
    assertNotEquals(article, nullIdArticle);
    assertNotEquals(nullIdArticle, article);
  }

  @Test
  public void can_equal_should_reject_non_article_types() {
    Article article = new Article("title", "desc", "body", Arrays.asList("java"), "123");
    assertFalse(article.canEqual("not an article"));
    assertTrue(article.canEqual(new Article()));
  }

  @Test
  public void update_with_only_title_should_not_change_description_or_body() {
    Article article = new Article("old title", "old desc", "old body", Arrays.asList("java"), "1");
    article.update("new title", "", "");
    assertThat(article.getTitle(), is("new title"));
    assertThat(article.getSlug(), is("new-title"));
    assertThat(article.getDescription(), is("old desc"));
    assertThat(article.getBody(), is("old body"));
  }

  @Test
  public void update_with_only_description_should_change_description_only() {
    Article article = new Article("old title", "old desc", "old body", Arrays.asList("java"), "1");
    article.update("", "new desc", "");
    assertThat(article.getTitle(), is("old title"));
    assertThat(article.getSlug(), is("old-title"));
    assertThat(article.getDescription(), is("new desc"));
    assertThat(article.getBody(), is("old body"));
  }

  @Test
  public void update_with_only_body_should_change_body_only() {
    Article article = new Article("old title", "old desc", "old body", Arrays.asList("java"), "1");
    article.update("", "", "new body");
    assertThat(article.getTitle(), is("old title"));
    assertThat(article.getSlug(), is("old-title"));
    assertThat(article.getDescription(), is("old desc"));
    assertThat(article.getBody(), is("new body"));
  }

  @Test
  public void update_with_empty_values_should_change_nothing() {
    Article article = new Article("old title", "old desc", "old body", Arrays.asList("java"), "1");
    article.update("", "", "");
    assertThat(article.getTitle(), is("old title"));
    assertThat(article.getSlug(), is("old-title"));
    assertThat(article.getDescription(), is("old desc"));
    assertThat(article.getBody(), is("old body"));
  }
}
