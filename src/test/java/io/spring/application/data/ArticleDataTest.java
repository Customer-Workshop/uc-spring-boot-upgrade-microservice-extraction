package io.spring.application.data;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.spring.application.DateTimeCursor;
import java.util.Arrays;
import java.util.List;
import org.joda.time.DateTime;
import org.junit.jupiter.api.Test;

public class ArticleDataTest {

  @Test
  public void should_create_with_all_args() {
    DateTime now = new DateTime();
    List<String> tags = Arrays.asList("java", "spring");
    ProfileData profile = new ProfileData("user-id", "testuser", "bio", "image", false);

    ArticleData data =
        new ArticleData("id", "slug", "title", "desc", "body", true, 5, now, now, tags, profile);

    assertEquals("id", data.getId());
    assertEquals("slug", data.getSlug());
    assertEquals("title", data.getTitle());
    assertEquals("desc", data.getDescription());
    assertEquals("body", data.getBody());
    assertTrue(data.isFavorited());
    assertEquals(5, data.getFavoritesCount());
    assertEquals(now, data.getCreatedAt());
    assertEquals(now, data.getUpdatedAt());
    assertEquals(tags, data.getTagList());
    assertEquals(profile, data.getProfileData());
  }

  @Test
  public void should_create_with_no_args() {
    ArticleData data = new ArticleData();
    assertNotNull(data);
    assertNull(data.getId());
    assertFalse(data.isFavorited());
    assertEquals(0, data.getFavoritesCount());
  }

  @Test
  public void should_get_cursor() {
    DateTime now = new DateTime();
    ArticleData data =
        new ArticleData("id", "slug", "title", "desc", "body", false, 0, now, now, null, null);

    DateTimeCursor cursor = data.getCursor();
    assertNotNull(cursor);
    assertEquals(now.getMillis(), cursor.getData().getMillis());
  }

  @Test
  public void should_set_fields() {
    ArticleData data = new ArticleData();
    data.setId("new-id");
    data.setSlug("new-slug");
    data.setTitle("new title");
    data.setDescription("new desc");
    data.setBody("new body");
    data.setFavorited(true);
    data.setFavoritesCount(10);
    DateTime now = new DateTime();
    data.setCreatedAt(now);
    data.setUpdatedAt(now);
    data.setTagList(Arrays.asList("tag"));
    data.setProfileData(new ProfileData("id", "user", "", "", false));

    assertEquals("new-id", data.getId());
    assertEquals("new-slug", data.getSlug());
    assertEquals("new title", data.getTitle());
    assertEquals("new desc", data.getDescription());
    assertEquals("new body", data.getBody());
    assertTrue(data.isFavorited());
    assertEquals(10, data.getFavoritesCount());
  }

  @Test
  public void should_have_equals_and_hashcode() {
    DateTime now = new DateTime();
    ArticleData data1 =
        new ArticleData("id", "slug", "title", "desc", "body", false, 0, now, now, null, null);
    ArticleData data2 =
        new ArticleData("id", "slug", "title", "desc", "body", false, 0, now, now, null, null);
    ArticleData data3 =
        new ArticleData("id2", "slug2", "title", "desc", "body", false, 0, now, now, null, null);

    assertEquals(data1, data2);
    assertNotEquals(data1, data3);
    assertEquals(data1.hashCode(), data2.hashCode());
  }

  @Test
  public void should_have_to_string() {
    ArticleData data = new ArticleData();
    data.setId("test-id");
    String str = data.toString();
    assertNotNull(str);
    assertTrue(str.contains("test-id"));
  }
}
