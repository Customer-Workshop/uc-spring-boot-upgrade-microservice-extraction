package io.spring.core;

import static org.junit.jupiter.api.Assertions.*;

import io.spring.core.article.Article;
import io.spring.core.article.Tag;
import io.spring.core.comment.Comment;
import io.spring.core.favorite.ArticleFavorite;
import io.spring.core.user.FollowRelation;
import io.spring.core.user.User;
import java.util.Arrays;
import org.junit.jupiter.api.Test;

public class CoreDomainTest {

  @Test
  public void should_create_article_favorite() {
    ArticleFavorite af = new ArticleFavorite("articleId", "userId");
    assertEquals("articleId", af.getArticleId());
    assertEquals("userId", af.getUserId());

    ArticleFavorite af2 = new ArticleFavorite("articleId", "userId");
    assertEquals(af, af2);
    assertEquals(af.hashCode(), af2.hashCode());

    ArticleFavorite af3 = new ArticleFavorite("other", "userId");
    assertNotEquals(af, af3);
  }

  @Test
  public void should_create_article_favorite_default() {
    ArticleFavorite af = new ArticleFavorite();
    assertNull(af.getArticleId());
    assertNull(af.getUserId());
  }

  @Test
  public void should_create_follow_relation() {
    FollowRelation fr = new FollowRelation("userId", "targetId");
    assertEquals("userId", fr.getUserId());
    assertEquals("targetId", fr.getTargetId());

    FollowRelation fr2 = new FollowRelation("userId", "targetId");
    assertEquals(fr, fr2);
    assertEquals(fr.hashCode(), fr2.hashCode());
    assertNotNull(fr.toString());
  }

  @Test
  public void should_create_follow_relation_with_setters() {
    FollowRelation fr = new FollowRelation();
    fr.setUserId("u");
    fr.setTargetId("t");
    assertEquals("u", fr.getUserId());
    assertEquals("t", fr.getTargetId());

    FollowRelation fr2 = new FollowRelation("other", "t");
    assertNotEquals(fr, fr2);
  }

  @Test
  public void should_create_tag() {
    Tag tag = new Tag("java");
    assertEquals("java", tag.getName());

    Tag tag2 = new Tag("java");
    assertEquals(tag, tag2);
    assertEquals(tag.hashCode(), tag2.hashCode());
    assertNotNull(tag.toString());

    Tag tag3 = new Tag("spring");
    assertNotEquals(tag, tag3);
  }

  @Test
  public void should_create_comment() {
    Comment comment = new Comment("body", "userId", "articleId");
    assertEquals("body", comment.getBody());
    assertEquals("userId", comment.getUserId());
    assertEquals("articleId", comment.getArticleId());
    assertNotNull(comment.getId());

    Comment comment2 = new Comment("body2", "userId2", "articleId2");
    assertNotEquals(comment.getId(), comment2.getId());
    assertNotNull(comment.toString());
    assertEquals(comment, comment);
    assertNotEquals(comment, null);
  }

  @Test
  public void should_create_article() {
    Article article =
        new Article("Title", "desc", "body", Arrays.asList("java", "spring"), "userId");
    assertEquals("title", article.getSlug());
    assertEquals("Title", article.getTitle());
    assertEquals("desc", article.getDescription());
    assertEquals("body", article.getBody());
    assertEquals("userId", article.getUserId());
    assertEquals(2, article.getTags().size());
    assertNotNull(article.getId());
    assertNotNull(article.getCreatedAt());
    assertNotNull(article.getUpdatedAt());
  }

  @Test
  public void should_update_article() {
    Article article = new Article("Title", "desc", "body", Arrays.asList("java"), "userId");

    article.update("New Title", "new desc", "new body");
    assertEquals("New Title", article.getTitle());
    assertEquals("new-title", article.getSlug());
    assertEquals("new desc", article.getDescription());
    assertEquals("new body", article.getBody());
  }

  @Test
  public void should_not_update_article_with_empty_values() {
    Article article = new Article("Title", "desc", "body", Arrays.asList("java"), "userId");

    article.update("", "", "");
    assertEquals("Title", article.getTitle());
    assertEquals("desc", article.getDescription());
    assertEquals("body", article.getBody());
  }

  @Test
  public void should_test_user_update() {
    User user = new User("test@test.com", "testuser", "password", "bio", "image");
    user.update("new@test.com", "newuser", "newpass", "newbio", "newimg");

    assertEquals("new@test.com", user.getEmail());
    assertEquals("newuser", user.getUsername());
    assertEquals("newbio", user.getBio());
    assertEquals("newimg", user.getImage());
  }

  @Test
  public void should_test_user_update_with_empty_values() {
    User user = new User("test@test.com", "testuser", "password", "bio", "image");
    user.update("", "", "", "", "");

    assertEquals("test@test.com", user.getEmail());
    assertEquals("testuser", user.getUsername());
    assertEquals("bio", user.getBio());
    assertEquals("image", user.getImage());
  }

  @Test
  public void should_test_article_equals() {
    Article a1 = new Article("Title", "desc", "body", Arrays.asList("java"), "userId");
    assertEquals(a1, a1);
    assertNotEquals(a1, null);
    assertNotEquals(a1, "not an article");
    assertNotNull(a1.toString());
  }

  @Test
  public void should_test_comment_equals() {
    Comment c1 = new Comment("body", "userId", "articleId");
    assertEquals(c1, c1);
    assertNotEquals(c1, null);
    assertNotEquals(c1, "not a comment");
  }
}
