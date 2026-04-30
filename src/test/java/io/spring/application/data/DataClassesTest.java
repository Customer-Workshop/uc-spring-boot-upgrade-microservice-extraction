package io.spring.application.data;

import static org.junit.jupiter.api.Assertions.*;

import io.spring.application.DateTimeCursor;
import java.util.Arrays;
import java.util.Collections;
import org.joda.time.DateTime;
import org.junit.jupiter.api.Test;

class DataClassesTest {

  @Test
  void should_create_article_data_with_all_fields() {
    DateTime now = new DateTime();
    ProfileData profile = new ProfileData("uid", "user", "bio", "img", false);
    ArticleData data =
        new ArticleData(
            "id", "slug", "title", "desc", "body", true, 5,
            now, now, Arrays.asList("java"), profile);

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
  }

  @Test
  void should_article_data_get_cursor() {
    DateTime now = new DateTime();
    ProfileData profile = new ProfileData("uid", "user", "bio", "img", false);
    ArticleData data =
        new ArticleData(
            "id", "slug", "title", "desc", "body", false, 0,
            now, now, Collections.emptyList(), profile);

    DateTimeCursor cursor = data.getCursor();
    assertNotNull(cursor);
  }

  @Test
  void should_article_data_set_fields() {
    ArticleData data = new ArticleData();
    data.setId("newId");
    data.setSlug("new-slug");
    data.setTitle("new title");
    data.setDescription("new desc");
    data.setBody("new body");
    data.setFavorited(true);
    data.setFavoritesCount(10);
    DateTime now = new DateTime();
    data.setCreatedAt(now);
    data.setUpdatedAt(now);
    data.setTagList(Arrays.asList("test"));
    ProfileData profile = new ProfileData("uid", "user", "bio", "img", false);
    data.setProfileData(profile);

    assertEquals("newId", data.getId());
    assertEquals("new-slug", data.getSlug());
    assertEquals("new title", data.getTitle());
    assertEquals("new desc", data.getDescription());
    assertEquals("new body", data.getBody());
    assertTrue(data.isFavorited());
    assertEquals(10, data.getFavoritesCount());
    assertEquals(now, data.getCreatedAt());
    assertEquals(now, data.getUpdatedAt());
    assertEquals(1, data.getTagList().size());
    assertEquals(profile, data.getProfileData());
  }

  @Test
  void should_article_data_equals_and_hashcode() {
    DateTime now = new DateTime();
    ProfileData profile = new ProfileData("uid", "user", "bio", "img", false);
    ArticleData data1 =
        new ArticleData("id", "slug", "title", "desc", "body", false, 0,
            now, now, Collections.emptyList(), profile);
    ArticleData data2 =
        new ArticleData("id", "slug", "title", "desc", "body", false, 0,
            now, now, Collections.emptyList(), profile);

    assertEquals(data1, data2);
    assertEquals(data1.hashCode(), data2.hashCode());
  }

  @Test
  void should_article_data_to_string() {
    ArticleData data = new ArticleData();
    data.setId("id");
    assertNotNull(data.toString());
  }

  @Test
  void should_create_article_data_list() {
    DateTime now = new DateTime();
    ProfileData profile = new ProfileData("uid", "user", "bio", "img", false);
    ArticleData data =
        new ArticleData("id", "slug", "title", "desc", "body", false, 0,
            now, now, Collections.emptyList(), profile);

    ArticleDataList list = new ArticleDataList(Arrays.asList(data), 1);

    assertEquals(1, list.getArticleDatas().size());
    assertEquals(1, list.getCount());
  }

  @Test
  void should_create_empty_article_data_list() {
    ArticleDataList list = new ArticleDataList(Collections.emptyList(), 0);

    assertEquals(0, list.getArticleDatas().size());
    assertEquals(0, list.getCount());
  }

  @Test
  void should_create_comment_data() {
    DateTime now = new DateTime();
    ProfileData profile = new ProfileData("uid", "user", "bio", "img", false);
    CommentData data = new CommentData("cid", "body", "articleId", now, now, profile);

    assertEquals("cid", data.getId());
    assertEquals("body", data.getBody());
    assertEquals("articleId", data.getArticleId());
    assertEquals(now, data.getCreatedAt());
    assertEquals(now, data.getUpdatedAt());
    assertEquals(profile, data.getProfileData());
  }

  @Test
  void should_comment_data_get_cursor() {
    DateTime now = new DateTime();
    ProfileData profile = new ProfileData("uid", "user", "bio", "img", false);
    CommentData data = new CommentData("cid", "body", "articleId", now, now, profile);

    DateTimeCursor cursor = data.getCursor();
    assertNotNull(cursor);
  }

  @Test
  void should_comment_data_setters() {
    CommentData data = new CommentData();
    data.setId("newId");
    data.setBody("new body");
    data.setArticleId("aid");
    DateTime now = new DateTime();
    data.setCreatedAt(now);
    data.setUpdatedAt(now);
    ProfileData profile = new ProfileData("uid", "user", "bio", "img", false);
    data.setProfileData(profile);

    assertEquals("newId", data.getId());
    assertEquals("new body", data.getBody());
    assertEquals("aid", data.getArticleId());
    assertEquals(profile, data.getProfileData());
  }

  @Test
  void should_comment_data_equals_and_hashcode() {
    DateTime now = new DateTime();
    ProfileData profile = new ProfileData("uid", "user", "bio", "img", false);
    CommentData d1 = new CommentData("cid", "body", "aid", now, now, profile);
    CommentData d2 = new CommentData("cid", "body", "aid", now, now, profile);

    assertEquals(d1, d2);
    assertEquals(d1.hashCode(), d2.hashCode());
  }

  @Test
  void should_create_profile_data() {
    ProfileData data = new ProfileData("uid", "user", "bio", "img", true);

    assertEquals("uid", data.getId());
    assertEquals("user", data.getUsername());
    assertEquals("bio", data.getBio());
    assertEquals("img", data.getImage());
    assertTrue(data.isFollowing());
  }

  @Test
  void should_profile_data_setters() {
    ProfileData data = new ProfileData();
    data.setId("uid");
    data.setUsername("user");
    data.setBio("bio");
    data.setImage("img");
    data.setFollowing(true);

    assertEquals("uid", data.getId());
    assertEquals("user", data.getUsername());
    assertEquals("bio", data.getBio());
    assertEquals("img", data.getImage());
    assertTrue(data.isFollowing());
  }

  @Test
  void should_profile_data_equals_and_hashcode() {
    ProfileData d1 = new ProfileData("uid", "user", "bio", "img", false);
    ProfileData d2 = new ProfileData("uid", "user", "bio", "img", false);

    assertEquals(d1, d2);
    assertEquals(d1.hashCode(), d2.hashCode());
  }

  @Test
  void should_create_user_data() {
    UserData data = new UserData("uid", "test@test.com", "user", "bio", "img");

    assertEquals("uid", data.getId());
    assertEquals("test@test.com", data.getEmail());
    assertEquals("user", data.getUsername());
    assertEquals("bio", data.getBio());
    assertEquals("img", data.getImage());
  }

  @Test
  void should_user_data_setters() {
    UserData data = new UserData();
    data.setId("uid");
    data.setEmail("email");
    data.setUsername("user");
    data.setBio("bio");
    data.setImage("img");

    assertEquals("uid", data.getId());
    assertEquals("email", data.getEmail());
    assertEquals("user", data.getUsername());
    assertEquals("bio", data.getBio());
    assertEquals("img", data.getImage());
  }

  @Test
  void should_user_data_equals_and_hashcode() {
    UserData d1 = new UserData("uid", "email", "user", "bio", "img");
    UserData d2 = new UserData("uid", "email", "user", "bio", "img");

    assertEquals(d1, d2);
    assertEquals(d1.hashCode(), d2.hashCode());
  }

  @Test
  void should_create_user_with_token() {
    UserData userData = new UserData("uid", "test@test.com", "user", "bio", "img");
    UserWithToken uwt = new UserWithToken(userData, "my-token");

    assertEquals("test@test.com", uwt.getEmail());
    assertEquals("user", uwt.getUsername());
    assertEquals("bio", uwt.getBio());
    assertEquals("img", uwt.getImage());
    assertEquals("my-token", uwt.getToken());
  }

  @Test
  void should_create_article_favorite_count() {
    ArticleFavoriteCount count = new ArticleFavoriteCount("aid", 5);

    assertEquals("aid", count.getId());
    assertEquals(5, count.getCount());
  }

  @Test
  void should_article_favorite_count_equals_and_hashcode() {
    ArticleFavoriteCount c1 = new ArticleFavoriteCount("aid", 5);
    ArticleFavoriteCount c2 = new ArticleFavoriteCount("aid", 5);

    assertEquals(c1, c2);
    assertEquals(c1.hashCode(), c2.hashCode());
  }
}
