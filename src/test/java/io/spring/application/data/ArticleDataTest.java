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
  void should_get_cursor() {
    DateTime now = new DateTime();
    ArticleData data = new ArticleData();
    data.setUpdatedAt(now);
    DateTimeCursor cursor = data.getCursor();
    assertNotNull(cursor);
    assertEquals(now, cursor.getData());
  }

  @Test
  void should_set_and_get_all_fields() {
    ArticleData data = new ArticleData();
    data.setId("newId");
    data.setSlug("new-slug");
    data.setTitle("New Title");
    data.setDescription("new desc");
    data.setBody("new body");
    data.setFavorited(true);
    data.setFavoritesCount(10);

    DateTime now = new DateTime();
    data.setCreatedAt(now);
    data.setUpdatedAt(now);
    data.setTagList(Arrays.asList("tag1", "tag2"));
    data.setProfileData(new ProfileData("id", "user", "", "", false));

    assertEquals("newId", data.getId());
    assertEquals("new-slug", data.getSlug());
    assertEquals("New Title", data.getTitle());
    assertEquals("new desc", data.getDescription());
    assertEquals("new body", data.getBody());
    assertTrue(data.isFavorited());
    assertEquals(10, data.getFavoritesCount());
    assertEquals(2, data.getTagList().size());
  }

  @Test
  void should_have_equals_and_hashcode() {
    DateTime now = new DateTime();
    ArticleData data1 =
        new ArticleData("id", "slug", "title", "desc", "body", false, 0, now, now, null, null);
    ArticleData data2 =
        new ArticleData("id", "slug", "title", "desc", "body", false, 0, now, now, null, null);

    assertEquals(data1, data2);
    assertEquals(data1.hashCode(), data2.hashCode());
  }

  @Test
  void should_have_toString() {
    ArticleData data = new ArticleData();
    data.setId("id");
    assertNotNull(data.toString());
    assertTrue(data.toString().contains("id"));
  }
}
