package io.spring.application.data;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.List;
import org.joda.time.DateTime;
import org.junit.jupiter.api.Test;

class DataClassesTest {

  @Test
  void should_create_article_data_with_all_fields() {
    ProfileData profile = new ProfileData("author-id", "author", "bio", "image", true);
    DateTime now = new DateTime();
    ArticleData data =
        new ArticleData(
            "id",
            "slug",
            "title",
            "desc",
            "body",
            true,
            5,
            now,
            now,
            Arrays.asList("java"),
            profile);

    assertEquals("id", data.getId());
    assertEquals("slug", data.getSlug());
    assertEquals("title", data.getTitle());
    assertEquals("desc", data.getDescription());
    assertEquals("body", data.getBody());
    assertTrue(data.isFavorited());
    assertEquals(5, data.getFavoritesCount());
    assertEquals(now, data.getCreatedAt());
    assertEquals(now, data.getUpdatedAt());
    assertEquals(1, data.getTagList().size());
    assertEquals(profile, data.getProfileData());
    assertNotNull(data.getCursor());
  }

  @Test
  void should_create_article_data_with_no_args() {
    ArticleData data = new ArticleData();
    assertNull(data.getId());
    assertNull(data.getSlug());
  }

  @Test
  void should_test_article_data_setters() {
    ArticleData data = new ArticleData();
    data.setId("id");
    data.setSlug("slug");
    data.setTitle("title");
    data.setDescription("desc");
    data.setBody("body");
    data.setFavorited(true);
    data.setFavoritesCount(10);
    DateTime now = new DateTime();
    data.setCreatedAt(now);
    data.setUpdatedAt(now);
    data.setTagList(Arrays.asList("java"));
    ProfileData profile = new ProfileData("id", "user", "bio", "img", false);
    data.setProfileData(profile);

    assertEquals("id", data.getId());
    assertEquals("slug", data.getSlug());
    assertEquals("title", data.getTitle());
    assertEquals("desc", data.getDescription());
    assertEquals("body", data.getBody());
    assertTrue(data.isFavorited());
    assertEquals(10, data.getFavoritesCount());
    assertEquals(now, data.getCreatedAt());
    assertEquals(now, data.getUpdatedAt());
    assertEquals(1, data.getTagList().size());
    assertEquals(profile, data.getProfileData());
  }

  @Test
  void should_test_article_data_equals_and_hashcode() {
    DateTime now = new DateTime();
    ArticleData data1 =
        new ArticleData("id", "slug", "title", "desc", "body", false, 0, now, now, null, null);
    ArticleData data2 =
        new ArticleData("id", "slug", "title", "desc", "body", false, 0, now, now, null, null);
    assertEquals(data1, data2);
    assertEquals(data1.hashCode(), data2.hashCode());
  }

  @Test
  void should_test_article_data_to_string() {
    ArticleData data = new ArticleData();
    data.setId("id");
    assertNotNull(data.toString());
  }

  @Test
  void should_create_comment_data_with_all_fields() {
    ProfileData profile = new ProfileData("author-id", "author", "bio", "image", false);
    DateTime now = new DateTime();
    CommentData data = new CommentData("id", "body", "article-id", now, now, profile);

    assertEquals("id", data.getId());
    assertEquals("body", data.getBody());
    assertEquals("article-id", data.getArticleId());
    assertEquals(now, data.getCreatedAt());
    assertEquals(now, data.getUpdatedAt());
    assertEquals(profile, data.getProfileData());
    assertNotNull(data.getCursor());
  }

  @Test
  void should_create_comment_data_with_no_args() {
    CommentData data = new CommentData();
    assertNull(data.getId());
    assertNull(data.getBody());
  }

  @Test
  void should_test_comment_data_setters() {
    CommentData data = new CommentData();
    data.setId("id");
    data.setBody("body");
    data.setArticleId("article-id");
    DateTime now = new DateTime();
    data.setCreatedAt(now);
    data.setUpdatedAt(now);
    ProfileData profile = new ProfileData("id", "user", "bio", "img", false);
    data.setProfileData(profile);

    assertEquals("id", data.getId());
    assertEquals("body", data.getBody());
    assertEquals("article-id", data.getArticleId());
  }

  @Test
  void should_test_comment_data_equals_and_hashcode() {
    DateTime now = new DateTime();
    CommentData data1 = new CommentData("id", "body", "art-id", now, now, null);
    CommentData data2 = new CommentData("id", "body", "art-id", now, now, null);
    assertEquals(data1, data2);
    assertEquals(data1.hashCode(), data2.hashCode());
  }

  @Test
  void should_create_profile_data_with_all_fields() {
    ProfileData data = new ProfileData("id", "username", "bio", "image", true);

    assertEquals("id", data.getId());
    assertEquals("username", data.getUsername());
    assertEquals("bio", data.getBio());
    assertEquals("image", data.getImage());
    assertTrue(data.isFollowing());
  }

  @Test
  void should_create_profile_data_with_no_args() {
    ProfileData data = new ProfileData();
    assertNull(data.getId());
    assertNull(data.getUsername());
  }

  @Test
  void should_test_profile_data_setters() {
    ProfileData data = new ProfileData();
    data.setId("id");
    data.setUsername("user");
    data.setBio("bio");
    data.setImage("image");
    data.setFollowing(true);

    assertEquals("id", data.getId());
    assertEquals("user", data.getUsername());
    assertEquals("bio", data.getBio());
    assertEquals("image", data.getImage());
    assertTrue(data.isFollowing());
  }

  @Test
  void should_test_profile_data_equals_and_hashcode() {
    ProfileData data1 = new ProfileData("id", "user", "bio", "img", true);
    ProfileData data2 = new ProfileData("id", "user", "bio", "img", true);
    assertEquals(data1, data2);
    assertEquals(data1.hashCode(), data2.hashCode());
  }

  @Test
  void should_create_user_data_with_all_fields() {
    UserData data = new UserData("id", "email@test.com", "username", "bio", "image");

    assertEquals("id", data.getId());
    assertEquals("email@test.com", data.getEmail());
    assertEquals("username", data.getUsername());
    assertEquals("bio", data.getBio());
    assertEquals("image", data.getImage());
  }

  @Test
  void should_create_user_data_with_no_args() {
    UserData data = new UserData();
    assertNull(data.getId());
    assertNull(data.getEmail());
  }

  @Test
  void should_test_user_data_setters() {
    UserData data = new UserData();
    data.setId("id");
    data.setEmail("email@test.com");
    data.setUsername("user");
    data.setBio("bio");
    data.setImage("image");

    assertEquals("id", data.getId());
    assertEquals("email@test.com", data.getEmail());
    assertEquals("user", data.getUsername());
    assertEquals("bio", data.getBio());
    assertEquals("image", data.getImage());
  }

  @Test
  void should_test_user_data_equals_and_hashcode() {
    UserData data1 = new UserData("id", "email", "user", "bio", "img");
    UserData data2 = new UserData("id", "email", "user", "bio", "img");
    assertEquals(data1, data2);
    assertEquals(data1.hashCode(), data2.hashCode());
  }

  @Test
  void should_create_user_with_token() {
    UserData userData = new UserData("id", "email@test.com", "username", "bio", "image");
    UserWithToken uwt = new UserWithToken(userData, "jwt-token");

    assertEquals("email@test.com", uwt.getEmail());
    assertEquals("username", uwt.getUsername());
    assertEquals("bio", uwt.getBio());
    assertEquals("image", uwt.getImage());
    assertEquals("jwt-token", uwt.getToken());
  }

  @Test
  void should_create_article_data_list() {
    DateTime now = new DateTime();
    ArticleData article =
        new ArticleData("id", "slug", "title", "desc", "body", false, 0, now, now, null, null);
    List<ArticleData> articles = Arrays.asList(article);
    ArticleDataList list = new ArticleDataList(articles, 10);

    assertEquals(1, list.getArticleDatas().size());
    assertEquals(10, list.getCount());
  }

  @Test
  void should_create_empty_article_data_list() {
    ArticleDataList list = new ArticleDataList(Arrays.asList(), 0);

    assertTrue(list.getArticleDatas().isEmpty());
    assertEquals(0, list.getCount());
  }

  @Test
  void should_create_article_favorite_count() {
    ArticleFavoriteCount count = new ArticleFavoriteCount("article-id", 42);

    assertEquals("article-id", count.getId());
    assertEquals(42, count.getCount());
  }

  @Test
  void should_test_article_favorite_count_equals_and_hashcode() {
    ArticleFavoriteCount c1 = new ArticleFavoriteCount("id", 5);
    ArticleFavoriteCount c2 = new ArticleFavoriteCount("id", 5);
    assertEquals(c1, c2);
    assertEquals(c1.hashCode(), c2.hashCode());
  }

  @Test
  void should_test_article_data_not_equal_to_different_object() {
    DateTime now = new DateTime();
    ArticleData data =
        new ArticleData("id", "slug", "title", "desc", "body", false, 0, now, now, null, null);
    assertNotEquals(data, "not an ArticleData");
    assertNotEquals(data, null);
  }

  @Test
  void should_test_profile_data_not_equal_to_different_values() {
    ProfileData data1 = new ProfileData("id1", "user1", "bio1", "img1", true);
    ProfileData data2 = new ProfileData("id2", "user2", "bio2", "img2", false);
    assertNotEquals(data1, data2);
  }
}
