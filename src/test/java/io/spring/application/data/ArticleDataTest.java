package io.spring.application.data;

import static org.junit.jupiter.api.Assertions.*;

import io.spring.application.DateTimeCursor;
import java.util.Arrays;
import org.joda.time.DateTime;
import org.junit.jupiter.api.Test;

public class ArticleDataTest {

  @Test
  public void should_create_article_data_and_access_fields() {
    DateTime now = new DateTime();
    ProfileData profile = new ProfileData("uid", "user", "bio", "img", false);
    ArticleData ad =
        new ArticleData(
            "id1",
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

    assertEquals("id1", ad.getId());
    assertEquals("slug", ad.getSlug());
    assertEquals("title", ad.getTitle());
    assertEquals("desc", ad.getDescription());
    assertEquals("body", ad.getBody());
    assertTrue(ad.isFavorited());
    assertEquals(5, ad.getFavoritesCount());
    assertEquals(now, ad.getCreatedAt());
    assertEquals(now, ad.getUpdatedAt());
    assertEquals(1, ad.getTagList().size());
    assertEquals(profile, ad.getProfileData());
  }

  @Test
  public void should_get_cursor_based_on_updated_at() {
    DateTime now = new DateTime();
    ArticleData ad =
        new ArticleData(
            "id1", "slug", "title", "desc", "body", false, 0, now, now, Arrays.asList(), null);
    DateTimeCursor cursor = ad.getCursor();

    assertNotNull(cursor);
    assertEquals(now, cursor.getData());
  }
}
