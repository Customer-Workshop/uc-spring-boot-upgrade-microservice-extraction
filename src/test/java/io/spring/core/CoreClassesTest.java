package io.spring.core;

import static org.junit.jupiter.api.Assertions.*;

import io.spring.core.article.Article;
import io.spring.core.article.Tag;
import io.spring.core.comment.Comment;
import io.spring.core.favorite.ArticleFavorite;
import io.spring.core.user.FollowRelation;
import io.spring.core.user.User;
import java.util.List;
import org.junit.jupiter.api.Test;

public class CoreClassesTest {

  @Test
  void should_test_follow_relation() {
    FollowRelation r1 = new FollowRelation("u1", "u2");
    assertEquals("u1", r1.getUserId());
    assertEquals("u2", r1.getTargetId());

    FollowRelation r2 = new FollowRelation("u1", "u2");
    assertEquals(r1, r2);
    assertEquals(r1.hashCode(), r2.hashCode());
    assertNotEquals(r1, null);
    assertNotNull(r1.toString());
  }

  @Test
  void should_test_article_favorite() {
    ArticleFavorite af = new ArticleFavorite("a1", "u1");
    assertEquals("a1", af.getArticleId());
    assertEquals("u1", af.getUserId());

    ArticleFavorite af2 = new ArticleFavorite("a1", "u1");
    assertEquals(af, af2);
    assertEquals(af.hashCode(), af2.hashCode());
    assertNotEquals(af, null);
    assertNotNull(af.toString());
  }

  @Test
  void should_test_tag() {
    Tag t = new Tag("java");
    assertEquals("java", t.getName());
    assertNotNull(t.getId());

    Tag t2 = new Tag("java");
    assertEquals(t, t2);
    Tag t3 = new Tag("python");
    assertNotEquals(t, t3);
    assertNotNull(t.toString());
    assertNotNull(t.hashCode());
  }

  @Test
  void should_test_comment_getters() {
    Comment c = new Comment("body", "u1", "a1");
    assertEquals("body", c.getBody());
    assertEquals("u1", c.getUserId());
    assertEquals("a1", c.getArticleId());
    assertNotNull(c.getId());
    assertNotNull(c.getCreatedAt());

    Comment c2 = new Comment("body", "u1", "a1");
    assertNotEquals(c, c2);
    assertNotNull(c.toString());
    assertNotNull(c.hashCode());
  }

  @Test
  void should_test_article_update_with_nonempty() {
    Article article = new Article("Old Title", "old desc", "old body", List.of(), "u1");
    article.update("New Title", "new desc", "new body");
    assertEquals("New Title", article.getTitle());
    assertEquals("new-title", article.getSlug());
    assertEquals("new desc", article.getDescription());
    assertEquals("new body", article.getBody());
  }

  @Test
  void should_test_user_update() {
    User u = new User("old@test.com", "olduser", "pass", "bio", "img");
    u.update("new@test.com", "newuser", "newpass", "newbio", "newimg");
    assertEquals("new@test.com", u.getEmail());
    assertEquals("newuser", u.getUsername());
    assertEquals("newpass", u.getPassword());
    assertEquals("newbio", u.getBio());
    assertEquals("newimg", u.getImage());
    assertNotNull(u.toString());
    assertNotNull(u.hashCode());
  }

  @Test
  void should_test_user_equality() {
    User u1 = new User("a@b.com", "user1", "pass", "", "");
    User u2 = new User("a@b.com", "user2", "pass", "", "");
    assertNotEquals(u1, u2);
    assertEquals(u1, u1);
    assertNotEquals(u1, null);
  }
}
