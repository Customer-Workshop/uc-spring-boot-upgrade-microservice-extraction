package io.spring.core.article;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.Collections;
import org.joda.time.DateTime;
import org.junit.jupiter.api.Test;

public class ArticleTest {

  @Test
  public void should_create_article_with_tags() {
    Article article =
        new Article("Test Title", "Desc", "Body", Arrays.asList("java", "spring"), "user-id");

    assertEquals("Test Title", article.getTitle());
    assertEquals("Desc", article.getDescription());
    assertEquals("Body", article.getBody());
    assertEquals("test-title", article.getSlug());
    assertEquals("user-id", article.getUserId());
    assertNotNull(article.getId());
    assertNotNull(article.getCreatedAt());
    assertNotNull(article.getUpdatedAt());
    assertEquals(2, article.getTags().size());
  }

  @Test
  public void should_create_article_with_custom_created_at() {
    DateTime customDate = new DateTime(2023, 1, 15, 10, 30, 0);
    Article article =
        new Article("Title", "Desc", "Body", Collections.emptyList(), "user-id", customDate);

    assertEquals(customDate, article.getCreatedAt());
    assertEquals(customDate, article.getUpdatedAt());
  }

  @Test
  public void should_create_article_with_no_args_constructor() {
    Article article = new Article();

    assertNull(article.getId());
    assertNull(article.getTitle());
  }

  @Test
  public void should_update_title() {
    Article article = new Article("Old Title", "Desc", "Body", Collections.emptyList(), "user-id");
    DateTime originalUpdatedAt = article.getUpdatedAt();

    article.update("New Title", "", "");

    assertEquals("New Title", article.getTitle());
    assertEquals("new-title", article.getSlug());
    assertEquals("Desc", article.getDescription());
    assertEquals("Body", article.getBody());
  }

  @Test
  public void should_update_description() {
    Article article = new Article("Title", "Old Desc", "Body", Collections.emptyList(), "user-id");

    article.update("", "New Desc", "");

    assertEquals("Title", article.getTitle());
    assertEquals("New Desc", article.getDescription());
  }

  @Test
  public void should_update_body() {
    Article article = new Article("Title", "Desc", "Old Body", Collections.emptyList(), "user-id");

    article.update("", "", "New Body");

    assertEquals("Title", article.getTitle());
    assertEquals("New Body", article.getBody());
  }

  @Test
  public void should_update_all_fields() {
    Article article = new Article("Title", "Desc", "Body", Collections.emptyList(), "user-id");

    article.update("New Title", "New Desc", "New Body");

    assertEquals("New Title", article.getTitle());
    assertEquals("New Desc", article.getDescription());
    assertEquals("New Body", article.getBody());
  }

  @Test
  public void should_not_update_with_empty_values() {
    Article article = new Article("Title", "Desc", "Body", Collections.emptyList(), "user-id");

    article.update("", "", "");

    assertEquals("Title", article.getTitle());
    assertEquals("Desc", article.getDescription());
    assertEquals("Body", article.getBody());
  }

  @Test
  public void should_not_update_with_null_values() {
    Article article = new Article("Title", "Desc", "Body", Collections.emptyList(), "user-id");

    article.update(null, null, null);

    assertEquals("Title", article.getTitle());
    assertEquals("Desc", article.getDescription());
    assertEquals("Body", article.getBody());
  }

  @Test
  public void should_generate_slug_from_title() {
    assertEquals("hello-world", Article.toSlug("Hello World"));
    assertEquals("test-article", Article.toSlug("Test Article"));
    assertEquals("special-chars", Article.toSlug("Special & Chars"));
  }

  @Test
  public void should_implement_equals_by_id() {
    Article article1 = new Article("Title", "Desc", "Body", Collections.emptyList(), "user-id");
    Article article2 = new Article("Title", "Desc", "Body", Collections.emptyList(), "user-id");

    assertNotEquals(article1, article2);
    assertEquals(article1, article1);
  }

  @Test
  public void should_implement_hashcode_by_id() {
    Article article = new Article("Title", "Desc", "Body", Collections.emptyList(), "user-id");

    assertEquals(article.hashCode(), article.hashCode());
  }

  @Test
  public void should_deduplicate_tags() {
    Article article =
        new Article("Title", "Desc", "Body", Arrays.asList("java", "java", "spring"), "user-id");

    assertEquals(2, article.getTags().size());
  }
}
