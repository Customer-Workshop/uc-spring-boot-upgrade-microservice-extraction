package io.spring.application.data;

import static org.junit.jupiter.api.Assertions.*;

import io.spring.application.DateTimeCursor;
import java.util.Arrays;
import org.joda.time.DateTime;
import org.junit.jupiter.api.Test;

public class ArticleDataTest {

  @Test
  void should_create_article_data_with_all_args() {
    DateTime now = new DateTime();
    ProfileData profile = new ProfileData("id1", "user", "bio", "img", false);
    ArticleData data =
        new ArticleData(
            "id",
            "test-slug",
            "Test Title",
            "desc",
            "body",
            true,
            5,
            now,
            now,
            Arrays.asList("java"),
            profile);

    assertEquals("id", data.getId());
    assertEquals("test-slug", data.getSlug());
    assertEquals("Test Title", data.getTitle());
    assertEquals("desc", data.getDescription());
    assertEquals("body", data.getBody());
    assertTrue(data.isFavorited());
    assertEquals(5, data.getFavoritesCount());
    assertEquals(now, data.getCreatedAt());
    assertEquals(now, data.getUpdatedAt());
    assertEquals(Arrays.asList("java"), data.getTagList());
    assertEquals(profile, data.getProfileData());
  }

  @Test
  void should_return_cursor_based_on_updated_at() {
    DateTime updatedAt = new DateTime(2023, 1, 15, 10, 30, 0);
    ArticleData data = new ArticleData();
    data.setUpdatedAt(updatedAt);

    DateTimeCursor cursor = data.getCursor();

    assertNotNull(cursor);
    assertEquals(new DateTimeCursor(updatedAt).toString(), cursor.toString());
  }

  @Test
  void should_create_article_data_with_no_args() {
    ArticleData data = new ArticleData();

    assertNull(data.getId());
    assertNull(data.getSlug());
    assertNull(data.getTitle());
    assertFalse(data.isFavorited());
    assertEquals(0, data.getFavoritesCount());
  }

  @Test
  void should_support_setters() {
    ArticleData data = new ArticleData();
    data.setId("new-id");
    data.setSlug("new-slug");
    data.setTitle("New Title");
    data.setDescription("New Desc");
    data.setBody("New Body");
    data.setFavorited(true);
    data.setFavoritesCount(10);

    assertEquals("new-id", data.getId());
    assertEquals("new-slug", data.getSlug());
    assertEquals("New Title", data.getTitle());
    assertEquals("New Desc", data.getDescription());
    assertEquals("New Body", data.getBody());
    assertTrue(data.isFavorited());
    assertEquals(10, data.getFavoritesCount());
  }

  @Test
  void should_support_equals_and_hashcode() {
    DateTime now = new DateTime();
    ProfileData profile = new ProfileData("id", "user", "bio", "img", false);
    ArticleData data1 =
        new ArticleData(
            "id", "slug", "title", "desc", "body", false, 0, now, now, Arrays.asList(), profile);
    ArticleData data2 =
        new ArticleData(
            "id", "slug", "title", "desc", "body", false, 0, now, now, Arrays.asList(), profile);

    assertEquals(data1, data2);
    assertEquals(data1.hashCode(), data2.hashCode());
  }
}
