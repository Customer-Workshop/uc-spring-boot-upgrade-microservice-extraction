package io.spring.core.bookmark;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class ArticleBookmarkTest {

  @Test
  void should_create_bookmark_with_article_and_user_id() {
    ArticleBookmark bookmark = new ArticleBookmark("article-1", "user-1");
    assertEquals("article-1", bookmark.getArticleId());
    assertEquals("user-1", bookmark.getUserId());
    assertNotNull(bookmark.getCreatedAt());
  }

  @Test
  void should_create_empty_bookmark_with_no_args() {
    ArticleBookmark bookmark = new ArticleBookmark();
    assertNull(bookmark.getArticleId());
    assertNull(bookmark.getUserId());
    assertNull(bookmark.getCreatedAt());
  }

  @Test
  void should_be_equal_when_same_article_and_user() {
    ArticleBookmark b1 = new ArticleBookmark("article-1", "user-1");
    ArticleBookmark b2 = new ArticleBookmark("article-1", "user-1");
    assertEquals(b1, b2);
    assertEquals(b1.hashCode(), b2.hashCode());
  }

  @Test
  void should_not_be_equal_when_different_article() {
    ArticleBookmark b1 = new ArticleBookmark("article-1", "user-1");
    ArticleBookmark b2 = new ArticleBookmark("article-2", "user-1");
    assertNotEquals(b1, b2);
  }
}
