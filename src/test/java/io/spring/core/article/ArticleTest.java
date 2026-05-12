package io.spring.core.article;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.Collections;
import org.joda.time.DateTime;
import org.junit.jupiter.api.Test;

public class ArticleTest {

  @Test
  void should_create_article_with_all_fields() {
    DateTime now = new DateTime();
    Article article =
        new Article(
            "My Title",
            "My Description",
            "My Body",
            Arrays.asList("java", "spring"),
            "user-id",
            now);

    assertNotNull(article.getId());
    assertEquals("my-title", article.getSlug());
    assertEquals("My Title", article.getTitle());
    assertEquals("My Description", article.getDescription());
    assertEquals("My Body", article.getBody());
    assertEquals("user-id", article.getUserId());
    assertEquals(2, article.getTags().size());
  }

  @Test
  void should_generate_slug_from_title() {
    Article article =
        new Article(
            "Hello World Article", "desc", "body", Collections.emptyList(), "uid", new DateTime());

    assertEquals("hello-world-article", article.getSlug());
  }

  @Test
  void should_create_tags_from_list() {
    Article article =
        new Article(
            "Title", "desc", "body", Arrays.asList("tag1", "tag2", "tag3"), "uid", new DateTime());

    assertEquals(3, article.getTags().size());
  }

  @Test
  void should_update_title_and_slug() {
    Article article =
        new Article("Old Title", "desc", "body", Collections.emptyList(), "uid", new DateTime());
    article.update("New Title", "", "");

    assertEquals("new-title", article.getSlug());
    assertEquals("New Title", article.getTitle());
  }

  @Test
  void should_update_description() {
    Article article =
        new Article("Title", "old desc", "body", Collections.emptyList(), "uid", new DateTime());
    article.update("", "new desc", "");

    assertEquals("new desc", article.getDescription());
  }

  @Test
  void should_update_body() {
    Article article =
        new Article("Title", "desc", "old body", Collections.emptyList(), "uid", new DateTime());
    article.update("", "", "new body");

    assertEquals("new body", article.getBody());
  }

  @Test
  void should_not_update_fields_with_empty_strings() {
    Article article =
        new Article("Title", "desc", "body", Collections.emptyList(), "uid", new DateTime());
    String originalSlug = article.getSlug();
    article.update("", "", "");

    assertEquals(originalSlug, article.getSlug());
    assertEquals("Title", article.getTitle());
    assertEquals("desc", article.getDescription());
    assertEquals("body", article.getBody());
  }

  @Test
  void should_have_created_and_updated_at() {
    DateTime now = new DateTime();
    Article article = new Article("Title", "desc", "body", Collections.emptyList(), "uid", now);

    assertNotNull(article.getCreatedAt());
    assertNotNull(article.getUpdatedAt());
  }
}
