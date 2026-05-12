package io.spring.application.data;

import static org.junit.jupiter.api.Assertions.*;

import io.spring.application.DateTimeCursor;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.joda.time.DateTime;
import org.junit.jupiter.api.Test;

public class ArticleDataTest {

  @Test
  void should_create_with_all_args() {
    DateTime now = new DateTime();
    ProfileData profile = new ProfileData("uid", "user", "bio", "img", false);
    List<String> tags = Arrays.asList("java", "spring");
    ArticleData data =
        new ArticleData("id1", "slug", "title", "desc", "body", true, 5, now, now, tags, profile);

    assertEquals("id1", data.getId());
    assertEquals("slug", data.getSlug());
    assertEquals("title", data.getTitle());
    assertEquals("desc", data.getDescription());
    assertEquals("body", data.getBody());
    assertTrue(data.isFavorited());
    assertEquals(5, data.getFavoritesCount());
    assertEquals(now, data.getCreatedAt());
    assertEquals(now, data.getUpdatedAt());
    assertEquals(2, data.getTagList().size());
    assertEquals(profile, data.getProfileData());
  }

  @Test
  void should_create_with_no_args() {
    ArticleData data = new ArticleData();

    assertNull(data.getId());
    assertNull(data.getSlug());
    assertNull(data.getTitle());
    assertFalse(data.isFavorited());
    assertEquals(0, data.getFavoritesCount());
  }

  @Test
  void should_get_cursor_from_updated_at() {
    DateTime now = new DateTime();
    ArticleData data =
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
            Collections.emptyList(),
            new ProfileData("uid", "user", "", "", false));

    DateTimeCursor cursor = data.getCursor();
    assertNotNull(cursor);
    assertEquals(now, cursor.getData());
  }

  @Test
  void should_support_setters() {
    ArticleData data = new ArticleData();
    data.setId("newId");
    data.setSlug("new-slug");
    data.setTitle("New Title");
    data.setDescription("New Desc");
    data.setBody("New Body");
    data.setFavorited(true);
    data.setFavoritesCount(10);
    DateTime now = new DateTime();
    data.setCreatedAt(now);
    data.setUpdatedAt(now);
    data.setTagList(Collections.singletonList("tag"));
    ProfileData profile = new ProfileData("uid", "user", "", "", false);
    data.setProfileData(profile);

    assertEquals("newId", data.getId());
    assertEquals("new-slug", data.getSlug());
    assertEquals("New Title", data.getTitle());
    assertEquals("New Desc", data.getDescription());
    assertEquals("New Body", data.getBody());
    assertTrue(data.isFavorited());
    assertEquals(10, data.getFavoritesCount());
    assertEquals(now, data.getCreatedAt());
    assertEquals(now, data.getUpdatedAt());
    assertEquals(1, data.getTagList().size());
    assertEquals(profile, data.getProfileData());
  }

  @Test
  void should_implement_equals_and_hashcode() {
    DateTime now = new DateTime();
    ProfileData profile = new ProfileData("uid", "user", "", "", false);
    ArticleData data1 =
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
            Collections.emptyList(),
            profile);
    ArticleData data2 =
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
            Collections.emptyList(),
            profile);

    assertEquals(data1, data2);
    assertEquals(data1.hashCode(), data2.hashCode());
  }

  @Test
  void should_implement_toString() {
    ArticleData data = new ArticleData();
    data.setId("test-id");
    assertNotNull(data.toString());
    assertTrue(data.toString().contains("test-id"));
  }
}
