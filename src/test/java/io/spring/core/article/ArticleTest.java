package io.spring.core.article;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

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
  public void should_store_title_description_and_body() {
    Article article =
        new Article("My Title", "My Description", "My Body", Arrays.asList("java"), "user1");
    assertEquals("My Title", article.getTitle());
    assertEquals("My Description", article.getDescription());
    assertEquals("My Body", article.getBody());
  }

  @Test
  public void should_generate_id_and_timestamps_on_creation() {
    DateTime before = new DateTime();
    Article article = new Article("title", "desc", "body", Arrays.asList("tag1"), "user1");
    assertNotNull(article.getId());
    assertNotNull(article.getCreatedAt());
    assertNotNull(article.getUpdatedAt());
    assertEquals(article.getCreatedAt(), article.getUpdatedAt());
  }

  @Test
  public void should_store_user_id() {
    Article article = new Article("title", "desc", "body", Arrays.asList("tag1"), "user42");
    assertEquals("user42", article.getUserId());
  }

  @Test
  public void should_create_tags_from_tag_list() {
    Article article =
        new Article("title", "desc", "body", Arrays.asList("java", "spring"), "user1");
    assertEquals(2, article.getTags().size());
  }

  @Test
  public void should_deduplicate_tags() {
    Article article =
        new Article("title", "desc", "body", Arrays.asList("java", "java", "spring"), "user1");
    assertEquals(2, article.getTags().size());
  }

  @Test
  public void should_update_title_and_slug() {
    Article article =
        new Article(
            "Old Title",
            "desc",
            "body",
            Arrays.asList("java"),
            "user1",
            new DateTime().minusDays(1));
    DateTime originalUpdatedAt = article.getUpdatedAt();
    article.update("New Title", "", "");
    assertEquals("New Title", article.getTitle());
    assertEquals("new-title", article.getSlug());
    assertNotEquals(originalUpdatedAt, article.getUpdatedAt());
  }

  @Test
  public void should_update_description() {
    Article article =
        new Article(
            "title",
            "old desc",
            "body",
            Arrays.asList("java"),
            "user1",
            new DateTime().minusDays(1));
    DateTime originalUpdatedAt = article.getUpdatedAt();
    article.update("", "new desc", "");
    assertEquals("new desc", article.getDescription());
    assertNotEquals("old desc", article.getDescription());
    assertNotEquals(originalUpdatedAt, article.getUpdatedAt());
  }

  @Test
  public void should_update_body() {
    Article article =
        new Article(
            "title",
            "desc",
            "old body",
            Arrays.asList("java"),
            "user1",
            new DateTime().minusDays(1));
    DateTime originalUpdatedAt = article.getUpdatedAt();
    article.update("", "", "new body");
    assertEquals("new body", article.getBody());
    assertNotEquals(originalUpdatedAt, article.getUpdatedAt());
  }

  @Test
  public void should_not_update_when_all_params_empty() {
    Article article =
        new Article(
            "title", "desc", "body", Arrays.asList("java"), "user1", new DateTime().minusDays(1));
    DateTime originalUpdatedAt = article.getUpdatedAt();
    article.update("", "", "");
    assertEquals("title", article.getTitle());
    assertEquals("desc", article.getDescription());
    assertEquals("body", article.getBody());
    assertEquals(originalUpdatedAt, article.getUpdatedAt());
  }

  @Test
  public void should_not_update_when_all_params_null() {
    Article article =
        new Article(
            "title", "desc", "body", Arrays.asList("java"), "user1", new DateTime().minusDays(1));
    DateTime originalUpdatedAt = article.getUpdatedAt();
    article.update(null, null, null);
    assertEquals("title", article.getTitle());
    assertEquals("desc", article.getDescription());
    assertEquals("body", article.getBody());
    assertEquals(originalUpdatedAt, article.getUpdatedAt());
  }

  @Test
  public void should_update_all_fields() {
    Article article =
        new Article(
            "title", "desc", "body", Arrays.asList("java"), "user1", new DateTime().minusDays(1));
    article.update("new title", "new desc", "new body");
    assertEquals("new title", article.getTitle());
    assertEquals("new-title", article.getSlug());
    assertEquals("new desc", article.getDescription());
    assertEquals("new body", article.getBody());
  }

  @Test
  public void should_have_correct_equality_based_on_id() {
    Article article1 = new Article("title", "desc", "body", Arrays.asList("java"), "user1");
    Article article2 = new Article("title", "desc", "body", Arrays.asList("java"), "user1");
    assertNotEquals(article1, article2);
    assertEquals(article1, article1);
    assertNotEquals(article1, null);
    assertNotEquals(article1, "not an article");
  }

  @Test
  public void should_have_consistent_hashcode() {
    Article article = new Article("title", "desc", "body", Arrays.asList("java"), "user1");
    int hash1 = article.hashCode();
    int hash2 = article.hashCode();
    assertEquals(hash1, hash2);
    assertNotEquals(0, hash1);
  }

  @Test
  public void should_have_different_hashcodes_for_different_articles() {
    Article article1 = new Article("title", "desc", "body", Arrays.asList("java"), "user1");
    Article article2 = new Article("title", "desc", "body", Arrays.asList("java"), "user1");
    assertNotEquals(article1.hashCode(), article2.hashCode());
  }

  @Test
  public void should_handle_equality_with_null_id() {
    Article article1 = new Article();
    Article article2 = new Article();
    assertEquals(article1, article2);
    assertEquals(article1.hashCode(), article2.hashCode());
  }

  @Test
  public void should_not_equal_null_id_to_non_null_id() {
    Article withId = new Article("title", "desc", "body", Arrays.asList("java"), "user1");
    Article withoutId = new Article();
    assertNotEquals(withId, withoutId);
    assertNotEquals(withoutId, withId);
  }

  @Test
  public void should_not_equal_subclass_instance() {
    Article article = new Article("title", "desc", "body", Arrays.asList("java"), "user1");
    Object subclassInstance =
        new Article("title", "desc", "body", Arrays.asList("java"), "user1") {};
    assertNotEquals(article, subclassInstance);
  }

  @Test
  public void should_include_prime_factor_in_hashcode() {
    Article article = new Article("title", "desc", "body", Arrays.asList("java"), "user1");
    int idHash = article.getId().hashCode();
    assertEquals(59 + idHash, article.hashCode());
  }
}
