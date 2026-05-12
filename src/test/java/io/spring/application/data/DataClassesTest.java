package io.spring.application.data;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import org.joda.time.DateTime;
import org.junit.jupiter.api.Test;

public class DataClassesTest {

  @Test
  void should_create_article_data() {
    DateTime now = new DateTime();
    ProfileData profile = new ProfileData("user-id", "testuser", "bio", "image", false);
    ArticleData articleData =
        new ArticleData(
            "article-id",
            "test-slug",
            "Title",
            "description",
            "body",
            true,
            5,
            now,
            now,
            Arrays.asList("java"),
            profile);

    assertEquals("article-id", articleData.getId());
    assertEquals("test-slug", articleData.getSlug());
    assertEquals("Title", articleData.getTitle());
    assertEquals("description", articleData.getDescription());
    assertEquals("body", articleData.getBody());
    assertTrue(articleData.isFavorited());
    assertEquals(5, articleData.getFavoritesCount());
    assertEquals(now, articleData.getCreatedAt());
    assertEquals(now, articleData.getUpdatedAt());
    assertEquals(1, articleData.getTagList().size());
    assertNotNull(articleData.getProfileData());
    assertNotNull(articleData.getCursor());
    assertNotNull(articleData.toString());
    assertNotNull(articleData.hashCode());
  }

  @Test
  void should_test_article_data_equals() {
    DateTime now = new DateTime();
    ProfileData profile = new ProfileData("user-id", "testuser", "bio", "image", false);
    ArticleData a1 =
        new ArticleData(
            "id", "slug", "Title", "desc", "body", false, 0, now, now, Arrays.asList(), profile);
    ArticleData a2 =
        new ArticleData(
            "id", "slug", "Title", "desc", "body", false, 0, now, now, Arrays.asList(), profile);

    assertEquals(a1, a2);
    assertEquals(a1.hashCode(), a2.hashCode());
    assertEquals(a1, a1);
    assertNotEquals(a1, null);
    assertNotEquals(a1, "string");
  }

  @Test
  void should_create_article_data_with_setters() {
    ArticleData data = new ArticleData();
    data.setId("id");
    data.setSlug("slug");
    data.setTitle("title");
    data.setDescription("desc");
    data.setBody("body");
    data.setFavorited(false);
    data.setFavoritesCount(0);
    data.setCreatedAt(new DateTime());
    data.setUpdatedAt(new DateTime());
    data.setTagList(Arrays.asList());
    data.setProfileData(new ProfileData());

    assertEquals("id", data.getId());
    assertEquals("slug", data.getSlug());
  }

  @Test
  void should_create_comment_data() {
    DateTime now = new DateTime();
    ProfileData profile = new ProfileData("user-id", "testuser", "bio", "image", false);
    CommentData commentData =
        new CommentData("comment-id", "body", "article-id", now, now, profile);

    assertEquals("comment-id", commentData.getId());
    assertEquals("body", commentData.getBody());
    assertEquals("article-id", commentData.getArticleId());
    assertEquals(now, commentData.getCreatedAt());
    assertEquals(now, commentData.getUpdatedAt());
    assertNotNull(commentData.getProfileData());
    assertNotNull(commentData.getCursor());
    assertNotNull(commentData.toString());
  }

  @Test
  void should_test_comment_data_equals() {
    DateTime now = new DateTime();
    ProfileData profile = new ProfileData("user-id", "testuser", "bio", "image", false);
    CommentData c1 = new CommentData("id", "body", "aid", now, now, profile);
    CommentData c2 = new CommentData("id", "body", "aid", now, now, profile);

    assertEquals(c1, c2);
    assertEquals(c1.hashCode(), c2.hashCode());
  }

  @Test
  void should_create_comment_data_with_setters() {
    CommentData data = new CommentData();
    data.setId("id");
    data.setBody("body");
    data.setArticleId("aid");
    data.setCreatedAt(new DateTime());
    data.setUpdatedAt(new DateTime());
    data.setProfileData(new ProfileData());

    assertEquals("id", data.getId());
    assertEquals("body", data.getBody());
  }

  @Test
  void should_create_profile_data() {
    ProfileData data = new ProfileData("user-id", "testuser", "bio", "image", true);

    assertEquals("user-id", data.getId());
    assertEquals("testuser", data.getUsername());
    assertEquals("bio", data.getBio());
    assertEquals("image", data.getImage());
    assertTrue(data.isFollowing());
    assertNotNull(data.toString());
  }

  @Test
  void should_test_profile_data_equals() {
    ProfileData p1 = new ProfileData("id", "user", "bio", "img", false);
    ProfileData p2 = new ProfileData("id", "user", "bio", "img", false);

    assertEquals(p1, p2);
    assertEquals(p1.hashCode(), p2.hashCode());
  }

  @Test
  void should_create_profile_data_with_setters() {
    ProfileData data = new ProfileData();
    data.setId("id");
    data.setUsername("u");
    data.setBio("b");
    data.setImage("i");
    data.setFollowing(false);

    assertEquals("id", data.getId());
    assertEquals("u", data.getUsername());
  }

  @Test
  void should_create_user_data() {
    UserData userData = new UserData("user-id", "test@test.com", "testuser", "bio", "image");

    assertEquals("user-id", userData.getId());
    assertEquals("test@test.com", userData.getEmail());
    assertEquals("testuser", userData.getUsername());
    assertEquals("bio", userData.getBio());
    assertEquals("image", userData.getImage());
    assertNotNull(userData.toString());
  }

  @Test
  void should_test_user_data_equals() {
    UserData u1 = new UserData("id", "email", "user", "bio", "img");
    UserData u2 = new UserData("id", "email", "user", "bio", "img");

    assertEquals(u1, u2);
    assertEquals(u1.hashCode(), u2.hashCode());
  }

  @Test
  void should_create_user_data_with_setters() {
    UserData data = new UserData();
    data.setId("id");
    data.setEmail("e");
    data.setUsername("u");
    data.setBio("b");
    data.setImage("i");

    assertEquals("id", data.getId());
    assertEquals("e", data.getEmail());
  }

  @Test
  void should_create_user_with_token() {
    UserData userData = new UserData("user-id", "test@test.com", "testuser", "bio", "image");
    UserWithToken uwt = new UserWithToken(userData, "jwt-token");

    assertEquals("test@test.com", uwt.getEmail());
    assertEquals("testuser", uwt.getUsername());
    assertEquals("bio", uwt.getBio());
    assertEquals("image", uwt.getImage());
    assertEquals("jwt-token", uwt.getToken());
  }

  @Test
  void should_create_article_favorite_count() {
    ArticleFavoriteCount count = new ArticleFavoriteCount("article-id", 10);

    assertEquals("article-id", count.getId());
    assertEquals(10, count.getCount());
    assertNotNull(count.toString());
    assertNotNull(count.hashCode());
  }

  @Test
  void should_test_article_favorite_count_equals() {
    ArticleFavoriteCount c1 = new ArticleFavoriteCount("id", 5);
    ArticleFavoriteCount c2 = new ArticleFavoriteCount("id", 5);

    assertEquals(c1, c2);
    assertEquals(c1.hashCode(), c2.hashCode());
  }

  @Test
  void should_create_article_data_list() {
    DateTime now = new DateTime();
    ProfileData profile = new ProfileData("user-id", "testuser", "bio", "image", false);
    ArticleData a1 =
        new ArticleData(
            "id", "slug", "Title", "desc", "body", false, 0, now, now, Arrays.asList(), profile);
    ArticleDataList list = new ArticleDataList(Arrays.asList(a1), 1);

    assertEquals(1, list.getCount());
    assertEquals(1, list.getArticleDatas().size());
  }
}
