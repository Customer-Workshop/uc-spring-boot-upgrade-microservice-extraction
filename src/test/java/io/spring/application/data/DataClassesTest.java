package io.spring.application.data;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import org.joda.time.DateTime;
import org.junit.jupiter.api.Test;

public class DataClassesTest {

  @Test
  public void should_test_article_data_getters_setters() {
    DateTime now = new DateTime();
    ArticleData a = new ArticleData();
    a.setId("id");
    a.setSlug("slug");
    a.setTitle("title");
    a.setDescription("desc");
    a.setBody("body");
    a.setFavorited(true);
    a.setFavoritesCount(5);
    a.setCreatedAt(now);
    a.setUpdatedAt(now);
    a.setTagList(Arrays.asList("java"));
    a.setProfileData(null);

    assertEquals("id", a.getId());
    assertEquals("slug", a.getSlug());
    assertEquals("title", a.getTitle());
    assertEquals("desc", a.getDescription());
    assertEquals("body", a.getBody());
    assertTrue(a.isFavorited());
    assertEquals(5, a.getFavoritesCount());
    assertEquals(now, a.getCreatedAt());
    assertEquals(now, a.getUpdatedAt());
    assertEquals(1, a.getTagList().size());
    assertNull(a.getProfileData());
  }

  @Test
  public void should_test_article_data_equals_and_hashcode() {
    DateTime now = new DateTime();
    ProfileData profile = new ProfileData("id", "user", "bio", "img", false);
    ArticleData a1 =
        new ArticleData(
            "id",
            "slug",
            "title",
            "desc",
            "body",
            false,
            0,
            now,
            now,
            Arrays.asList("java"),
            profile);
    ArticleData a2 =
        new ArticleData(
            "id",
            "slug",
            "title",
            "desc",
            "body",
            false,
            0,
            now,
            now,
            Arrays.asList("java"),
            profile);

    assertEquals(a1, a2);
    assertEquals(a1.hashCode(), a2.hashCode());
    assertNotNull(a1.toString());
    assertEquals(a1, a1);
    assertNotEquals(a1, null);
    assertNotEquals(a1, "string");
  }

  @Test
  public void should_test_article_data_cursor() {
    DateTime now = new DateTime();
    ArticleData a = new ArticleData();
    a.setUpdatedAt(now);
    assertNotNull(a.getCursor());
  }

  @Test
  public void should_test_profile_data_getters_setters() {
    ProfileData p = new ProfileData();
    p.setId("id");
    p.setUsername("user");
    p.setBio("bio");
    p.setImage("img");
    p.setFollowing(true);

    assertEquals("id", p.getId());
    assertEquals("user", p.getUsername());
    assertEquals("bio", p.getBio());
    assertEquals("img", p.getImage());
    assertTrue(p.isFollowing());
  }

  @Test
  public void should_test_profile_data_equals_and_hashcode() {
    ProfileData p1 = new ProfileData("id", "user", "bio", "img", false);
    ProfileData p2 = new ProfileData("id", "user", "bio", "img", false);

    assertEquals(p1, p2);
    assertEquals(p1.hashCode(), p2.hashCode());
    assertNotNull(p1.toString());
    assertEquals(p1, p1);
    assertNotEquals(p1, null);
    assertNotEquals(p1, "string");
  }

  @Test
  public void should_test_comment_data_getters_setters() {
    DateTime now = new DateTime();
    CommentData c = new CommentData();
    c.setId("id");
    c.setBody("body");
    c.setArticleId("artId");
    c.setCreatedAt(now);
    c.setUpdatedAt(now);
    c.setProfileData(null);

    assertEquals("id", c.getId());
    assertEquals("body", c.getBody());
    assertEquals("artId", c.getArticleId());
    assertEquals(now, c.getCreatedAt());
    assertEquals(now, c.getUpdatedAt());
    assertNull(c.getProfileData());
  }

  @Test
  public void should_test_comment_data_equals_and_hashcode() {
    DateTime now = new DateTime();
    ProfileData profile = new ProfileData("id", "user", "bio", "img", false);
    CommentData c1 = new CommentData("id", "body", "artId", now, now, profile);
    CommentData c2 = new CommentData("id", "body", "artId", now, now, profile);

    assertEquals(c1, c2);
    assertEquals(c1.hashCode(), c2.hashCode());
    assertNotNull(c1.toString());
    assertEquals(c1, c1);
    assertNotEquals(c1, null);
  }

  @Test
  public void should_test_comment_data_cursor() {
    DateTime now = new DateTime();
    CommentData c = new CommentData();
    c.setCreatedAt(now);
    assertNotNull(c.getCursor());
  }

  @Test
  public void should_test_user_data_getters_setters() {
    UserData u = new UserData();
    u.setId("id");
    u.setEmail("email");
    u.setUsername("user");
    u.setBio("bio");
    u.setImage("img");

    assertEquals("id", u.getId());
    assertEquals("email", u.getEmail());
    assertEquals("user", u.getUsername());
    assertEquals("bio", u.getBio());
    assertEquals("img", u.getImage());
  }

  @Test
  public void should_test_user_data_equals_and_hashcode() {
    UserData u1 = new UserData("id", "email", "user", "bio", "img");
    UserData u2 = new UserData("id", "email", "user", "bio", "img");

    assertEquals(u1, u2);
    assertEquals(u1.hashCode(), u2.hashCode());
    assertNotNull(u1.toString());
    assertEquals(u1, u1);
    assertNotEquals(u1, null);
  }

  @Test
  public void should_test_article_favorite_count() {
    ArticleFavoriteCount afc = new ArticleFavoriteCount("id", 5);

    assertEquals("id", afc.getId());
    assertEquals(5, afc.getCount());
    assertNotNull(afc.toString());

    ArticleFavoriteCount afc2 = new ArticleFavoriteCount("id", 5);
    assertEquals(afc, afc2);
    assertEquals(afc.hashCode(), afc2.hashCode());
  }

  @Test
  public void should_test_article_data_inequality() {
    DateTime now = new DateTime();
    ProfileData profile = new ProfileData("id", "user", "bio", "img", false);
    ArticleData a1 =
        new ArticleData(
            "id1",
            "slug1",
            "title",
            "desc",
            "body",
            false,
            0,
            now,
            now,
            Arrays.asList("java"),
            profile);
    ArticleData a2 =
        new ArticleData(
            "id2",
            "slug2",
            "title2",
            "desc2",
            "body2",
            true,
            1,
            now,
            now,
            Arrays.asList("spring"),
            profile);

    assertNotEquals(a1, a2);
  }

  @Test
  public void should_test_profile_data_inequality() {
    ProfileData p1 = new ProfileData("id1", "user1", "bio1", "img1", false);
    ProfileData p2 = new ProfileData("id2", "user2", "bio2", "img2", true);

    assertNotEquals(p1, p2);
  }

  @Test
  public void should_test_user_data_inequality() {
    UserData u1 = new UserData("id1", "email1", "user1", "bio1", "img1");
    UserData u2 = new UserData("id2", "email2", "user2", "bio2", "img2");

    assertNotEquals(u1, u2);
  }

  @Test
  public void should_test_comment_data_inequality() {
    DateTime now = new DateTime();
    ProfileData profile = new ProfileData("id", "user", "bio", "img", false);
    CommentData c1 = new CommentData("id1", "body1", "art1", now, now, profile);
    CommentData c2 = new CommentData("id2", "body2", "art2", now, now, profile);

    assertNotEquals(c1, c2);
  }
}
