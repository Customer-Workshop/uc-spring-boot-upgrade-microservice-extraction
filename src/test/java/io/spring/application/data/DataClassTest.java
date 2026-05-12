package io.spring.application.data;

import static org.junit.jupiter.api.Assertions.*;

import io.spring.application.CursorPageParameter;
import io.spring.application.CursorPager.Direction;
import io.spring.application.Page;
import io.spring.core.article.Article;
import io.spring.core.article.Tag;
import io.spring.core.comment.Comment;
import io.spring.core.favorite.ArticleFavorite;
import io.spring.core.user.FollowRelation;
import java.util.Arrays;
import java.util.Collections;
import org.joda.time.DateTime;
import org.junit.jupiter.api.Test;

public class DataClassTest {

  @Test
  public void should_test_article_data_equals_and_hashcode() {
    ProfileData profile = new ProfileData("p1", "user", "bio", "img", false);
    DateTime now = new DateTime();
    ArticleData a1 =
        new ArticleData("id1", "slug", "title", "desc", "body", false, 0, now, now,
            Collections.singletonList("java"), profile);
    ArticleData a2 =
        new ArticleData("id1", "slug", "title", "desc", "body", false, 0, now, now,
            Collections.singletonList("java"), profile);

    assertEquals(a1, a2);
    assertEquals(a1.hashCode(), a2.hashCode());
    assertEquals(a1.toString(), a2.toString());

    a1.setFavoritesCount(5);
    assertEquals(5, a1.getFavoritesCount());
    a1.setFavorited(true);
    assertTrue(a1.isFavorited());
    assertNotNull(a1.getId());
    assertNotNull(a1.getSlug());
    assertNotNull(a1.getTitle());
    assertNotNull(a1.getDescription());
    assertNotNull(a1.getBody());
    assertNotNull(a1.getCreatedAt());
    assertNotNull(a1.getUpdatedAt());
    assertNotNull(a1.getTagList());
    assertNotNull(a1.getProfileData());
  }

  @Test
  public void should_test_comment_data_equals_and_hashcode() {
    ProfileData profile = new ProfileData("p1", "user", "bio", "img", false);
    DateTime now = new DateTime();
    CommentData c1 = new CommentData("id1", "body", "a1", now, now, profile);
    CommentData c2 = new CommentData("id1", "body", "a1", now, now, profile);

    assertEquals(c1, c2);
    assertEquals(c1.hashCode(), c2.hashCode());
    assertNotNull(c1.toString());
    assertNotNull(c1.getId());
    assertNotNull(c1.getBody());
    assertNotNull(c1.getArticleId());
    assertNotNull(c1.getCreatedAt());
    assertNotNull(c1.getUpdatedAt());
    assertNotNull(c1.getProfileData());
  }

  @Test
  public void should_test_user_data_equals_and_hashcode() {
    UserData u1 = new UserData("id1", "e@t.com", "user", "bio", "img");
    UserData u2 = new UserData("id1", "e@t.com", "user", "bio", "img");

    assertEquals(u1, u2);
    assertEquals(u1.hashCode(), u2.hashCode());
    assertNotNull(u1.toString());
    assertEquals("id1", u1.getId());
    assertEquals("e@t.com", u1.getEmail());
    assertEquals("user", u1.getUsername());
    assertEquals("bio", u1.getBio());
    assertEquals("img", u1.getImage());
  }

  @Test
  public void should_test_profile_data_equals_and_hashcode() {
    ProfileData p1 = new ProfileData("id1", "user", "bio", "img", true);
    ProfileData p2 = new ProfileData("id1", "user", "bio", "img", true);

    assertEquals(p1, p2);
    assertEquals(p1.hashCode(), p2.hashCode());
    assertNotNull(p1.toString());
    assertEquals("id1", p1.getId());
    assertEquals("user", p1.getUsername());
    assertEquals("bio", p1.getBio());
    assertEquals("img", p1.getImage());
    assertTrue(p1.isFollowing());
    p1.setFollowing(false);
    assertFalse(p1.isFollowing());
  }

  @Test
  public void should_test_article_favorite_count() {
    ArticleFavoriteCount afc1 = new ArticleFavoriteCount("id1", 5);
    ArticleFavoriteCount afc2 = new ArticleFavoriteCount("id1", 5);

    assertEquals(afc1, afc2);
    assertEquals(afc1.hashCode(), afc2.hashCode());
    assertNotNull(afc1.toString());
    assertEquals("id1", afc1.getId());
    assertEquals(5, afc1.getCount());
  }

  @Test
  public void should_test_follow_relation() {
    FollowRelation fr1 = new FollowRelation("u1", "u2");
    FollowRelation fr2 = new FollowRelation("u1", "u2");

    assertEquals(fr1, fr2);
    assertEquals(fr1.hashCode(), fr2.hashCode());
    assertNotNull(fr1.toString());
    assertEquals("u1", fr1.getUserId());
    assertEquals("u2", fr1.getTargetId());
  }

  @Test
  public void should_test_article_favorite() {
    ArticleFavorite af1 = new ArticleFavorite("a1", "u1");
    ArticleFavorite af2 = new ArticleFavorite("a1", "u1");

    assertEquals(af1, af2);
    assertEquals(af1.hashCode(), af2.hashCode());
    assertNotNull(af1.toString());
    assertEquals("a1", af1.getArticleId());
    assertEquals("u1", af1.getUserId());
  }

  @Test
  public void should_test_tag() {
    Tag tag1 = new Tag("java");
    Tag tag2 = new Tag("java");

    assertEquals(tag1, tag2);
    assertEquals(tag1.hashCode(), tag2.hashCode());
    assertNotNull(tag1.toString());
    assertNotNull(tag1.getId());
    assertEquals("java", tag1.getName());
  }

  @Test
  public void should_test_comment() {
    Comment c1 = new Comment("body", "u1", "a1");
    assertNotNull(c1.getId());
    assertEquals("body", c1.getBody());
    assertEquals("u1", c1.getUserId());
    assertEquals("a1", c1.getArticleId());
    assertNotNull(c1.getCreatedAt());
    assertNotNull(c1.toString());
  }

  @Test
  public void should_test_page() {
    Page p1 = new Page();
    assertEquals(0, p1.getOffset());
    assertEquals(20, p1.getLimit());

    Page p2 = new Page(2, 10);
    assertEquals(2, p2.getOffset());
    assertEquals(10, p2.getLimit());
    assertNotNull(p2.toString());
    assertEquals(p2, new Page(2, 10));
    assertEquals(p2.hashCode(), new Page(2, 10).hashCode());
  }

  @Test
  public void should_test_cursor_page_parameter_equals() {
    CursorPageParameter<DateTime> cp1 = new CursorPageParameter<>(null, 10, Direction.NEXT);
    CursorPageParameter<DateTime> cp2 = new CursorPageParameter<>(null, 10, Direction.NEXT);

    assertEquals(cp1, cp2);
    assertEquals(cp1.hashCode(), cp2.hashCode());
    assertNotNull(cp1.toString());
  }

  @Test
  public void should_test_article_data_list() {
    ArticleDataList list = new ArticleDataList(Collections.emptyList(), 0);
    assertEquals(0, list.getCount());
    assertTrue(list.getArticleDatas().isEmpty());
  }

  @Test
  public void should_test_article_with_update() {
    Article article =
        new Article("title", "desc", "body", Arrays.asList("java"), "u1");
    assertNotNull(article.getSlug());
    assertNotNull(article.getId());

    article.update("new-title", "new-desc", "new-body");
    assertEquals("new-title", article.getTitle());
    assertEquals("new-desc", article.getDescription());
    assertEquals("new-body", article.getBody());
  }
}
