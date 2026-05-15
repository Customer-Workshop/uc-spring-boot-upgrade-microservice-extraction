package io.spring.application.data;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import org.joda.time.DateTime;
import org.junit.jupiter.api.Test;

public class ArticleDataTest {

  @Test
  public void should_create_article_data() {
    DateTime now = new DateTime();
    ProfileData profile = new ProfileData("authorId", "author", "bio", "image", false);
    ArticleData articleData =
        new ArticleData(
            "id",
            "slug",
            "title",
            "description",
            "body",
            false,
            0,
            now,
            now,
            Arrays.asList("java"),
            profile);
    assertEquals("id", articleData.getId());
    assertEquals("slug", articleData.getSlug());
    assertEquals("title", articleData.getTitle());
    assertEquals("description", articleData.getDescription());
    assertEquals("body", articleData.getBody());
    assertFalse(articleData.isFavorited());
    assertEquals(0, articleData.getFavoritesCount());
    assertEquals(now, articleData.getCreatedAt());
    assertEquals(now, articleData.getUpdatedAt());
    assertEquals(1, articleData.getTagList().size());
    assertEquals(profile, articleData.getProfileData());
  }

  @Test
  public void should_return_cursor_from_updated_at() {
    DateTime now = new DateTime();
    ArticleData articleData = new ArticleData();
    articleData.setUpdatedAt(now);
    assertNotNull(articleData.getCursor());
    assertEquals(now, articleData.getCursor().getData());
  }

  @Test
  public void should_set_favorited() {
    ArticleData articleData = new ArticleData();
    articleData.setFavorited(true);
    assertTrue(articleData.isFavorited());
  }

  @Test
  public void should_set_favorites_count() {
    ArticleData articleData = new ArticleData();
    articleData.setFavoritesCount(5);
    assertEquals(5, articleData.getFavoritesCount());
  }

  @Test
  public void should_set_all_fields() {
    DateTime now = new DateTime();
    ProfileData profile = new ProfileData("id", "user", "bio", "img", false);
    ArticleData articleData = new ArticleData();
    articleData.setId("id");
    articleData.setSlug("slug");
    articleData.setTitle("title");
    articleData.setDescription("desc");
    articleData.setBody("body");
    articleData.setFavorited(true);
    articleData.setFavoritesCount(10);
    articleData.setCreatedAt(now);
    articleData.setUpdatedAt(now);
    articleData.setTagList(Arrays.asList("java", "spring"));
    articleData.setProfileData(profile);
    assertEquals("id", articleData.getId());
    assertEquals("slug", articleData.getSlug());
    assertEquals("title", articleData.getTitle());
    assertEquals("desc", articleData.getDescription());
    assertEquals("body", articleData.getBody());
    assertTrue(articleData.isFavorited());
    assertEquals(10, articleData.getFavoritesCount());
    assertEquals(2, articleData.getTagList().size());
    assertEquals(profile, articleData.getProfileData());
  }

  @Test
  public void should_implement_equals() {
    DateTime now = new DateTime();
    ArticleData a1 =
        new ArticleData("id", "slug", "title", "desc", "body", false, 0, now, now, null, null);
    ArticleData a2 =
        new ArticleData("id", "slug", "title", "desc", "body", false, 0, now, now, null, null);
    assertEquals(a1, a2);
  }

  @Test
  public void should_not_equal_different_article() {
    DateTime now = new DateTime();
    ArticleData a1 =
        new ArticleData("id1", "slug1", "t1", "d1", "b1", false, 0, now, now, null, null);
    ArticleData a2 =
        new ArticleData("id2", "slug2", "t2", "d2", "b2", true, 5, now, now, null, null);
    assertNotEquals(a1, a2);
  }

  @Test
  public void should_implement_hashcode() {
    DateTime now = new DateTime();
    ArticleData a1 =
        new ArticleData("id", "slug", "title", "desc", "body", false, 0, now, now, null, null);
    ArticleData a2 =
        new ArticleData("id", "slug", "title", "desc", "body", false, 0, now, now, null, null);
    assertEquals(a1.hashCode(), a2.hashCode());
  }

  @Test
  public void should_implement_tostring() {
    ArticleData articleData = new ArticleData();
    articleData.setTitle("Test Title");
    String str = articleData.toString();
    assertNotNull(str);
    assertTrue(str.contains("Test Title"));
  }

  @Test
  public void should_not_equal_null() {
    ArticleData articleData = new ArticleData();
    assertNotEquals(null, articleData);
  }

  @Test
  public void should_equal_self() {
    ArticleData articleData = new ArticleData();
    assertEquals(articleData, articleData);
  }
}
