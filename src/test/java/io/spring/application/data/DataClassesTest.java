package io.spring.application.data;

import static org.junit.jupiter.api.Assertions.*;

import io.spring.application.DateTimeCursor;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.joda.time.DateTime;
import org.junit.jupiter.api.Test;

class DataClassesTest {

  // === ArticleData tests ===

  @Test
  void should_create_article_data_with_all_args() {
    DateTime now = DateTime.now();
    ProfileData profile = new ProfileData("userId", "username", "bio", "image", false);

    ArticleData data =
        new ArticleData(
            "id1", "slug", "title", "desc", "body", false, 0, now, now,
            Arrays.asList("java", "spring"), profile);

    assertEquals("id1", data.getId());
    assertEquals("slug", data.getSlug());
    assertEquals("title", data.getTitle());
    assertEquals("desc", data.getDescription());
    assertEquals("body", data.getBody());
    assertFalse(data.isFavorited());
    assertEquals(0, data.getFavoritesCount());
    assertEquals(now, data.getCreatedAt());
    assertEquals(now, data.getUpdatedAt());
    assertEquals(2, data.getTagList().size());
    assertEquals(profile, data.getProfileData());
  }

  @Test
  void should_create_article_data_with_no_args() {
    ArticleData data = new ArticleData();

    assertNull(data.getId());
    assertNull(data.getSlug());
    assertNull(data.getTitle());
  }

  @Test
  void should_get_cursor_from_article_data() {
    DateTime now = DateTime.now();
    ArticleData data = new ArticleData();
    data.setUpdatedAt(now);

    DateTimeCursor cursor = data.getCursor();

    assertNotNull(cursor);
  }

  @Test
  void should_set_article_data_fields() {
    ArticleData data = new ArticleData();
    data.setId("id");
    data.setSlug("slug");
    data.setTitle("title");
    data.setDescription("desc");
    data.setBody("body");
    data.setFavorited(true);
    data.setFavoritesCount(5);

    assertEquals("id", data.getId());
    assertEquals("slug", data.getSlug());
    assertEquals("title", data.getTitle());
    assertEquals("desc", data.getDescription());
    assertEquals("body", data.getBody());
    assertTrue(data.isFavorited());
    assertEquals(5, data.getFavoritesCount());
  }

  // === CommentData tests ===

  @Test
  void should_create_comment_data_with_all_args() {
    DateTime now = DateTime.now();
    ProfileData profile = new ProfileData("userId", "username", "bio", "image", false);

    CommentData data = new CommentData("id1", "body", "articleId", now, now, profile);

    assertEquals("id1", data.getId());
    assertEquals("body", data.getBody());
    assertEquals("articleId", data.getArticleId());
    assertEquals(now, data.getCreatedAt());
    assertEquals(now, data.getUpdatedAt());
    assertEquals(profile, data.getProfileData());
  }

  @Test
  void should_create_comment_data_with_no_args() {
    CommentData data = new CommentData();

    assertNull(data.getId());
    assertNull(data.getBody());
  }

  @Test
  void should_get_cursor_from_comment_data() {
    DateTime now = DateTime.now();
    CommentData data = new CommentData();
    data.setCreatedAt(now);

    DateTimeCursor cursor = data.getCursor();

    assertNotNull(cursor);
  }

  @Test
  void should_set_comment_data_fields() {
    CommentData data = new CommentData();
    data.setId("id");
    data.setBody("body");
    data.setArticleId("articleId");

    assertEquals("id", data.getId());
    assertEquals("body", data.getBody());
    assertEquals("articleId", data.getArticleId());
  }

  // === ProfileData tests ===

  @Test
  void should_create_profile_data_with_all_args() {
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
    assertFalse(data.isFollowing());
  }

  @Test
  void should_set_profile_data_fields() {
    ProfileData data = new ProfileData();
    data.setId("id");
    data.setUsername("user");
    data.setBio("bio");
    data.setImage("img");
    data.setFollowing(true);

    assertEquals("id", data.getId());
    assertEquals("user", data.getUsername());
    assertEquals("bio", data.getBio());
    assertEquals("img", data.getImage());
    assertTrue(data.isFollowing());
  }

  @Test
  void should_test_profile_data_equals_and_hashcode() {
    ProfileData data1 = new ProfileData("id", "username", "bio", "image", true);
    ProfileData data2 = new ProfileData("id", "username", "bio", "image", true);
    ProfileData data3 = new ProfileData("id2", "other", "bio", "image", false);

    assertEquals(data1, data2);
    assertEquals(data1.hashCode(), data2.hashCode());
    assertNotEquals(data1, data3);
  }

  @Test
  void should_test_profile_data_to_string() {
    ProfileData data = new ProfileData("id", "username", "bio", "image", true);

    String str = data.toString();
    assertNotNull(str);
    assertTrue(str.contains("username"));
  }

  // === UserData tests ===

  @Test
  void should_create_user_data_with_all_args() {
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
  void should_set_user_data_fields() {
    UserData data = new UserData();
    data.setId("id");
    data.setEmail("email@test.com");
    data.setUsername("username");
    data.setBio("bio");
    data.setImage("image");

    assertEquals("id", data.getId());
    assertEquals("email@test.com", data.getEmail());
    assertEquals("username", data.getUsername());
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

  // === UserWithToken tests ===

  @Test
  void should_create_user_with_token() {
    UserData userData = new UserData("id", "email@test.com", "username", "bio", "image");

    UserWithToken uwt = new UserWithToken(userData, "jwt-token-123");

    assertEquals("email@test.com", uwt.getEmail());
    assertEquals("username", uwt.getUsername());
    assertEquals("bio", uwt.getBio());
    assertEquals("image", uwt.getImage());
    assertEquals("jwt-token-123", uwt.getToken());
  }

  @Test
  void should_create_user_with_token_preserving_all_fields() {
    UserData userData = new UserData("id", "test@example.com", "testuser", "my bio", "http://img.png");

    UserWithToken uwt = new UserWithToken(userData, "token-xyz");

    assertEquals("test@example.com", uwt.getEmail());
    assertEquals("testuser", uwt.getUsername());
    assertEquals("my bio", uwt.getBio());
    assertEquals("http://img.png", uwt.getImage());
    assertEquals("token-xyz", uwt.getToken());
  }

  // === ArticleDataList tests ===

  @Test
  void should_create_article_data_list() {
    ArticleData article1 = new ArticleData();
    article1.setId("id1");
    ArticleData article2 = new ArticleData();
    article2.setId("id2");

    List<ArticleData> articles = Arrays.asList(article1, article2);
    ArticleDataList list = new ArticleDataList(articles, 10);

    assertEquals(2, list.getArticleDatas().size());
    assertEquals(10, list.getCount());
  }

  @Test
  void should_create_empty_article_data_list() {
    ArticleDataList list = new ArticleDataList(Collections.emptyList(), 0);

    assertTrue(list.getArticleDatas().isEmpty());
    assertEquals(0, list.getCount());
  }

  // === ArticleFavoriteCount tests ===

  @Test
  void should_create_article_favorite_count() {
    ArticleFavoriteCount count = new ArticleFavoriteCount("article-id", 42);

    assertEquals("article-id", count.getId());
    assertEquals(42, count.getCount());
  }

  @Test
  void should_test_article_favorite_count_equals() {
    ArticleFavoriteCount count1 = new ArticleFavoriteCount("id", 5);
    ArticleFavoriteCount count2 = new ArticleFavoriteCount("id", 5);

    assertEquals(count1, count2);
    assertEquals(count1.hashCode(), count2.hashCode());
  }

  @Test
  void should_test_article_favorite_count_to_string() {
    ArticleFavoriteCount count = new ArticleFavoriteCount("id", 5);

    String str = count.toString();
    assertNotNull(str);
    assertTrue(str.contains("id"));
  }
}
