package io.spring.application.data;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import org.joda.time.DateTime;
import org.junit.jupiter.api.Test;

public class DataClassTest {

  @Test
  void should_create_article_data_and_verify_all_fields() {
    DateTime now = new DateTime();
    ProfileData profile = new ProfileData("id1", "user", "bio", "img", false);
    ArticleData article =
        new ArticleData(
            "a1",
            "slug",
            "Title",
            "desc",
            "body",
            false,
            5,
            now,
            now,
            Arrays.asList("java"),
            profile);

    assertEquals("a1", article.getId());
    assertEquals("slug", article.getSlug());
    assertEquals("Title", article.getTitle());
    assertEquals("desc", article.getDescription());
    assertEquals("body", article.getBody());
    assertFalse(article.isFavorited());
    assertEquals(5, article.getFavoritesCount());
    assertEquals(now, article.getCreatedAt());
    assertEquals(now, article.getUpdatedAt());
    assertEquals(1, article.getTagList().size());
    assertEquals(profile, article.getProfileData());

    article.setFavorited(true);
    assertTrue(article.isFavorited());
    article.setFavoritesCount(10);
    assertEquals(10, article.getFavoritesCount());
    article.setSlug("new-slug");
    assertEquals("new-slug", article.getSlug());
    article.setProfileData(null);
    assertNull(article.getProfileData());
  }

  @Test
  void should_test_article_data_equals_and_hashcode() {
    DateTime now = new DateTime();
    ArticleData a1 =
        new ArticleData(
            "a1", "slug", "Title", "desc", "body", false, 0, now, now, Arrays.asList(), null);
    ArticleData a2 =
        new ArticleData(
            "a1", "slug", "Title", "desc", "body", false, 0, now, now, Arrays.asList(), null);
    ArticleData a3 =
        new ArticleData(
            "a2", "slug2", "Title2", "desc2", "body2", true, 1, now, now, Arrays.asList(), null);

    assertEquals(a1, a2);
    assertNotEquals(a1, a3);
    assertEquals(a1.hashCode(), a2.hashCode());
    assertNotNull(a1.toString());
    assertEquals(a1, a1);
    assertNotEquals(a1, null);
    assertNotEquals(a1, "string");
  }

  @Test
  void should_test_article_data_no_args_constructor() {
    ArticleData data = new ArticleData();
    assertNull(data.getId());
    assertNull(data.getSlug());
  }

  @Test
  void should_create_comment_data_and_verify_all_fields() {
    DateTime now = new DateTime();
    ProfileData profile = new ProfileData("id1", "user", "bio", "img", false);
    CommentData comment = new CommentData("c1", "body", "articleId", now, now, profile);

    assertEquals("c1", comment.getId());
    assertEquals("body", comment.getBody());
    assertEquals("articleId", comment.getArticleId());
    assertEquals(now, comment.getCreatedAt());
    assertEquals(now, comment.getUpdatedAt());
    assertEquals(profile, comment.getProfileData());
  }

  @Test
  void should_test_comment_data_equals_and_hashcode() {
    DateTime now = new DateTime();
    ProfileData profile = new ProfileData("id1", "user", "bio", "img", false);
    CommentData c1 = new CommentData("c1", "body", "a1", now, now, profile);
    CommentData c2 = new CommentData("c1", "body", "a1", now, now, profile);
    CommentData c3 = new CommentData("c2", "body2", "a2", now, now, profile);

    assertEquals(c1, c2);
    assertNotEquals(c1, c3);
    assertEquals(c1.hashCode(), c2.hashCode());
    assertNotNull(c1.toString());
    assertEquals(c1, c1);
    assertNotEquals(c1, null);
    assertNotEquals(c1, "string");
  }

  @Test
  void should_create_user_data_and_verify_fields() {
    UserData userData = new UserData("u1", "test@test.com", "testuser", "bio", "image");

    assertEquals("u1", userData.getId());
    assertEquals("test@test.com", userData.getEmail());
    assertEquals("testuser", userData.getUsername());
    assertEquals("bio", userData.getBio());
    assertEquals("image", userData.getImage());
  }

  @Test
  void should_test_user_data_equals_and_hashcode() {
    UserData u1 = new UserData("u1", "test@test.com", "testuser", "bio", "image");
    UserData u2 = new UserData("u1", "test@test.com", "testuser", "bio", "image");
    UserData u3 = new UserData("u2", "other@test.com", "other", "bio2", "img2");

    assertEquals(u1, u2);
    assertNotEquals(u1, u3);
    assertEquals(u1.hashCode(), u2.hashCode());
    assertNotNull(u1.toString());
    assertEquals(u1, u1);
    assertNotEquals(u1, null);
    assertNotEquals(u1, "string");
  }

  @Test
  void should_create_profile_data_and_verify_fields() {
    ProfileData profile = new ProfileData("id1", "user", "bio", "img", true);

    assertEquals("id1", profile.getId());
    assertEquals("user", profile.getUsername());
    assertEquals("bio", profile.getBio());
    assertEquals("img", profile.getImage());
    assertTrue(profile.isFollowing());
  }

  @Test
  void should_test_profile_data_equals_and_hashcode() {
    ProfileData p1 = new ProfileData("id1", "user", "bio", "img", false);
    ProfileData p2 = new ProfileData("id1", "user", "bio", "img", false);
    ProfileData p3 = new ProfileData("id2", "other", "bio2", "img2", true);

    assertEquals(p1, p2);
    assertNotEquals(p1, p3);
    assertEquals(p1.hashCode(), p2.hashCode());
    assertNotNull(p1.toString());
    assertEquals(p1, p1);
    assertNotEquals(p1, null);
    assertNotEquals(p1, "string");
    p1.setFollowing(true);
    assertTrue(p1.isFollowing());
    p1.setUsername("changed");
    assertEquals("changed", p1.getUsername());
    p1.setBio("newbio");
    assertEquals("newbio", p1.getBio());
    p1.setImage("newimg");
    assertEquals("newimg", p1.getImage());
  }

  @Test
  void should_test_profile_data_no_args_constructor() {
    ProfileData data = new ProfileData();
    assertNull(data.getId());
    assertNull(data.getUsername());
  }

  @Test
  void should_create_article_favorite_count() {
    ArticleFavoriteCount count = new ArticleFavoriteCount("a1", 5);
    assertEquals("a1", count.getId());
    assertEquals(5, count.getCount());
    assertNotNull(count.toString());
    assertEquals(count, new ArticleFavoriteCount("a1", 5));
    assertEquals(count.hashCode(), new ArticleFavoriteCount("a1", 5).hashCode());
    assertNotEquals(count, new ArticleFavoriteCount("a2", 3));
    assertNotEquals(count, null);
    assertNotEquals(count, "string");
  }

  @Test
  void should_test_article_data_cursor() {
    DateTime now = new DateTime();
    ProfileData profile = new ProfileData("id1", "user", "bio", "img", false);
    ArticleData article =
        new ArticleData(
            "a1", "slug", "Title", "desc", "body", false, 0, now, now, Arrays.asList(), profile);
    assertNotNull(article.getCursor());
  }

  @Test
  void should_test_comment_data_cursor() {
    DateTime now = new DateTime();
    ProfileData profile = new ProfileData("id1", "user", "bio", "img", false);
    CommentData comment = new CommentData("c1", "body", "a1", now, now, profile);
    assertNotNull(comment.getCursor());
  }
}
