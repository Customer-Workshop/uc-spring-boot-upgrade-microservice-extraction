package io.spring.application.data;

import static org.junit.jupiter.api.Assertions.*;

import io.spring.application.DateTimeCursor;
import java.util.Arrays;
import org.joda.time.DateTime;
import org.junit.jupiter.api.Test;

public class ArticleDataTest {

  @Test
  void should_create_with_all_args() {
    DateTime now = new DateTime();
    ProfileData profile = new ProfileData("uid", "user", "bio", "img", false);
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
  }

  @Test
  void should_create_with_no_args() {
    ArticleData data = new ArticleData();
    assertNull(data.getId());
    assertNull(data.getSlug());
    assertFalse(data.isFavorited());
    assertEquals(0, data.getFavoritesCount());
  }

  @Test
  void should_get_cursor_from_updated_at() {
    DateTime now = new DateTime();
    ArticleData data = new ArticleData();
    data.setUpdatedAt(now);
    DateTimeCursor cursor = data.getCursor();
    assertNotNull(cursor);
  }

  @Test
  void should_support_setters() {
    ArticleData data = new ArticleData();
    data.setId("id");
    data.setSlug("slug");
    data.setTitle("title");
    data.setDescription("desc");
    data.setBody("body");
    data.setFavorited(true);
    data.setFavoritesCount(10);
    data.setTagList(Arrays.asList("tag1"));
    data.setProfileData(new ProfileData());

    assertEquals("id", data.getId());
    assertEquals("slug", data.getSlug());
    assertEquals("title", data.getTitle());
    assertEquals("desc", data.getDescription());
    assertEquals("body", data.getBody());
    assertTrue(data.isFavorited());
    assertEquals(10, data.getFavoritesCount());
    assertEquals(1, data.getTagList().size());
    assertNotNull(data.getProfileData());
  }

  @Test
  void should_support_equals_and_hashcode() {
    DateTime now = new DateTime();
    ProfileData profile = new ProfileData("uid", "user", "bio", "img", false);
    ArticleData data1 =
        new ArticleData("id", "slug", "title", "desc", "body", false, 0, now, now, null, profile);
    ArticleData data2 =
        new ArticleData("id", "slug", "title", "desc", "body", false, 0, now, now, null, profile);
    assertEquals(data1, data2);
    assertEquals(data1.hashCode(), data2.hashCode());
  }

  @Test
  void should_support_to_string() {
    ArticleData data = new ArticleData();
    data.setSlug("test");
    assertNotNull(data.toString());
    assertTrue(data.toString().contains("test"));
  }
}
