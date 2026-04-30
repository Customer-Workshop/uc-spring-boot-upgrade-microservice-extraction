package io.spring.application.data;

import static org.junit.jupiter.api.Assertions.*;

import io.spring.application.CursorPageParameter;
import io.spring.application.CursorPager.Direction;
import java.util.List;
import org.joda.time.DateTime;
import org.junit.jupiter.api.Test;

public class DataClassesTest {

  @Test
  void should_test_article_data_lombok_methods() {
    ArticleData a1 = new ArticleData();
    a1.setId("1");
    a1.setSlug("slug");
    a1.setTitle("title");
    a1.setDescription("desc");
    a1.setBody("body");
    a1.setFavorited(true);
    a1.setFavoritesCount(5);
    a1.setCreatedAt(new DateTime());
    a1.setUpdatedAt(new DateTime());
    a1.setTagList(List.of("java"));
    a1.setProfileData(new ProfileData("id", "user", "bio", "img", false));

    assertEquals("1", a1.getId());
    assertEquals("slug", a1.getSlug());
    assertEquals("title", a1.getTitle());
    assertEquals("desc", a1.getDescription());
    assertEquals("body", a1.getBody());
    assertTrue(a1.isFavorited());
    assertEquals(5, a1.getFavoritesCount());
    assertNotNull(a1.getCreatedAt());
    assertNotNull(a1.getUpdatedAt());
    assertEquals(1, a1.getTagList().size());
    assertNotNull(a1.getProfileData());
    assertNotNull(a1.getCursor());
    assertNotNull(a1.toString());

    ArticleData a2 =
        new ArticleData(
            "1",
            "slug",
            "title",
            "desc",
            "body",
            true,
            5,
            new DateTime(),
            new DateTime(),
            List.of("java"),
            new ProfileData("id", "user", "bio", "img", false));
    assertEquals(a1.getId(), a2.getId());
    assertEquals(a1.hashCode(), a1.hashCode());
    assertEquals(a1, a1);
    assertNotEquals(a1, null);
    assertNotEquals(a1, "string");
  }

  @Test
  void should_test_comment_data_lombok_methods() {
    CommentData c1 = new CommentData();
    c1.setId("c1");
    c1.setBody("body");
    c1.setArticleId("article1");
    c1.setCreatedAt(new DateTime());
    c1.setUpdatedAt(new DateTime());
    c1.setProfileData(new ProfileData("id", "user", "bio", "img", false));

    assertEquals("c1", c1.getId());
    assertEquals("body", c1.getBody());
    assertEquals("article1", c1.getArticleId());
    assertNotNull(c1.getCreatedAt());
    assertNotNull(c1.getUpdatedAt());
    assertNotNull(c1.getProfileData());
    assertNotNull(c1.getCursor());
    assertNotNull(c1.toString());

    CommentData c2 =
        new CommentData("c1", "body", "article1", new DateTime(), new DateTime(), null);
    assertEquals(c1.getId(), c2.getId());
    assertEquals(c1, c1);
    assertEquals(c1.hashCode(), c1.hashCode());
    assertNotEquals(c1, null);
  }

  @Test
  void should_test_user_data_lombok_methods() {
    UserData u1 = new UserData();
    u1.setId("u1");
    u1.setEmail("test@test.com");
    u1.setUsername("testuser");
    u1.setBio("bio");
    u1.setImage("img");

    assertEquals("u1", u1.getId());
    assertEquals("test@test.com", u1.getEmail());
    assertEquals("testuser", u1.getUsername());
    assertEquals("bio", u1.getBio());
    assertEquals("img", u1.getImage());
    assertNotNull(u1.toString());

    UserData u2 = new UserData("u1", "test@test.com", "testuser", "bio", "img");
    assertEquals(u1, u2);
    assertEquals(u1.hashCode(), u2.hashCode());
    assertNotEquals(u1, null);
    assertNotEquals(u1, "string");
  }

  @Test
  void should_test_profile_data_lombok_methods() {
    ProfileData p1 = new ProfileData();
    p1.setId("p1");
    p1.setUsername("user");
    p1.setBio("bio");
    p1.setImage("img");
    p1.setFollowing(true);

    assertEquals("p1", p1.getId());
    assertEquals("user", p1.getUsername());
    assertEquals("bio", p1.getBio());
    assertEquals("img", p1.getImage());
    assertTrue(p1.isFollowing());
    assertNotNull(p1.toString());

    ProfileData p2 = new ProfileData("p1", "user", "bio", "img", true);
    assertEquals(p1, p2);
    assertEquals(p1.hashCode(), p2.hashCode());
    assertNotEquals(p1, null);
  }

  @Test
  void should_test_article_favorite_count() {
    ArticleFavoriteCount afc = new ArticleFavoriteCount("a1", 5);
    assertEquals("a1", afc.getId());
    assertEquals(5, afc.getCount());
    assertNotNull(afc.toString());

    ArticleFavoriteCount afc2 = new ArticleFavoriteCount("a1", 5);
    assertEquals(afc, afc2);
    assertEquals(afc.hashCode(), afc2.hashCode());
  }

  @Test
  void should_test_cursor_page_parameter() {
    CursorPageParameter<DateTime> param = new CursorPageParameter<>();
    assertEquals(20, param.getLimit());

    CursorPageParameter<DateTime> param2 =
        new CursorPageParameter<>(new DateTime(), 50, Direction.NEXT);
    assertEquals(50, param2.getLimit());
    assertNotNull(param2.getCursor());
    assertTrue(param2.isNext());
    assertEquals(51, param2.getQueryLimit());
    assertNotNull(param2.toString());

    CursorPageParameter<DateTime> param3 = new CursorPageParameter<>(null, 2000, Direction.PREV);
    assertEquals(1000, param3.getLimit());
    assertFalse(param3.isNext());

    CursorPageParameter<DateTime> param4 = new CursorPageParameter<>(null, -5, Direction.NEXT);
    assertEquals(20, param4.getLimit());

    assertEquals(param2, param2);
    assertEquals(param2.hashCode(), param2.hashCode());
    assertNotEquals(param2, null);
  }

  @Test
  void should_test_article_data_list() {
    ArticleDataList list = new ArticleDataList(List.of(), 0);
    assertEquals(0, list.getCount());
    assertTrue(list.getArticleDatas().isEmpty());
  }

  @Test
  void should_test_user_with_token() {
    UserData userData = new UserData("u1", "test@test.com", "testuser", "bio", "img");
    UserWithToken uwt = new UserWithToken(userData, "token123");

    assertEquals("test@test.com", uwt.getEmail());
    assertEquals("testuser", uwt.getUsername());
    assertEquals("bio", uwt.getBio());
    assertEquals("img", uwt.getImage());
    assertEquals("token123", uwt.getToken());
  }
}
