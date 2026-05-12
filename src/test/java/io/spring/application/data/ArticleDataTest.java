package io.spring.application.data;

import static org.junit.jupiter.api.Assertions.*;

import io.spring.application.DateTimeCursor;
import java.util.Arrays;
import java.util.Collections;
import org.joda.time.DateTime;
import org.junit.jupiter.api.Test;

public class ArticleDataTest {

  @Test
  public void should_create_with_all_args_constructor() {
    DateTime now = new DateTime();
    ProfileData profile = new ProfileData("id", "user", "bio", "image", false);
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
            Arrays.asList("tag1"),
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
  public void should_create_with_no_args_and_setters() {
    ArticleData data = new ArticleData();
    DateTime now = new DateTime();
    data.setId("id");
    data.setSlug("slug");
    data.setTitle("title");
    data.setDescription("desc");
    data.setBody("body");
    data.setFavorited(false);
    data.setFavoritesCount(0);
    data.setCreatedAt(now);
    data.setUpdatedAt(now);
    data.setTagList(Collections.emptyList());
    data.setProfileData(null);

    assertEquals("id", data.getId());
    assertEquals("slug", data.getSlug());
    assertFalse(data.isFavorited());
    assertNull(data.getProfileData());
  }

  @Test
  public void should_return_cursor_from_updated_at() {
    DateTime now = new DateTime();
    ArticleData data =
        new ArticleData("id", "slug", "title", "desc", "body", false, 0, now, now, null, null);

    DateTimeCursor cursor = data.getCursor();

    assertNotNull(cursor);
    assertEquals(now, cursor.getData());
  }

  @Test
  public void should_implement_equals_and_hashcode() {
    DateTime now = new DateTime();
    ArticleData data1 =
        new ArticleData("id", "slug", "title", "desc", "body", false, 0, now, now, null, null);
    ArticleData data2 =
        new ArticleData("id", "slug", "title", "desc", "body", false, 0, now, now, null, null);

    assertEquals(data1, data2);
    assertEquals(data1.hashCode(), data2.hashCode());
  }

  @Test
  public void should_not_equal_different_data() {
    DateTime now = new DateTime();
    ArticleData data1 =
        new ArticleData("id1", "slug1", "title", "desc", "body", false, 0, now, now, null, null);
    ArticleData data2 =
        new ArticleData("id2", "slug2", "title", "desc", "body", false, 0, now, now, null, null);

    assertNotEquals(data1, data2);
  }

  @Test
  public void should_implement_toString() {
    DateTime now = new DateTime();
    ArticleData data =
        new ArticleData("id", "slug", "title", "desc", "body", false, 0, now, now, null, null);

    String str = data.toString();

    assertNotNull(str);
    assertTrue(str.contains("id"));
    assertTrue(str.contains("slug"));
  }
}
