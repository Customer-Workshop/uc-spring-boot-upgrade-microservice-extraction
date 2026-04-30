package io.spring.core.article;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.Collections;
import org.junit.jupiter.api.Test;

public class ArticleUpdateTest {

  @Test
  public void should_update_title() {
    Article article = new Article("Old Title", "desc", "body", Collections.emptyList(), "uid");
    article.update("New Title", "", "");
    assertEquals("New Title", article.getTitle());
    assertEquals("new-title", article.getSlug());
  }

  @Test
  public void should_update_description() {
    Article article = new Article("Title", "old desc", "body", Collections.emptyList(), "uid");
    article.update("", "new desc", "");
    assertEquals("new desc", article.getDescription());
  }

  @Test
  public void should_update_body() {
    Article article = new Article("Title", "desc", "old body", Collections.emptyList(), "uid");
    article.update("", "", "new body");
    assertEquals("new body", article.getBody());
  }

  @Test
  public void should_update_all_fields() {
    Article article = new Article("Title", "desc", "body", Collections.emptyList(), "uid");
    article.update("New", "new desc", "new body");
    assertEquals("New", article.getTitle());
    assertEquals("new desc", article.getDescription());
    assertEquals("new body", article.getBody());
  }

  @Test
  public void should_not_update_when_empty_strings() {
    Article article = new Article("Title", "desc", "body", Collections.emptyList(), "uid");
    String originalSlug = article.getSlug();
    article.update("", "", "");
    assertEquals("Title", article.getTitle());
    assertEquals(originalSlug, article.getSlug());
    assertEquals("desc", article.getDescription());
    assertEquals("body", article.getBody());
  }

  @Test
  public void should_not_update_when_null_values() {
    Article article = new Article("Title", "desc", "body", Collections.emptyList(), "uid");
    article.update(null, null, null);
    assertEquals("Title", article.getTitle());
  }

  @Test
  public void should_create_article_with_tags() {
    Article article = new Article("Title", "desc", "body", Arrays.asList("java", "spring"), "uid");
    assertFalse(article.getTags().isEmpty());
    assertNotNull(article.getId());
    assertNotNull(article.getCreatedAt());
    assertNotNull(article.getUpdatedAt());
    assertEquals("uid", article.getUserId());
  }

  @Test
  public void should_deduplicate_tags() {
    Article article = new Article("Title", "desc", "body", Arrays.asList("java", "java"), "uid");
    assertEquals(1, article.getTags().size());
  }
}
