package io.spring;

import static org.junit.jupiter.api.Assertions.*;

import io.spring.application.CursorPageParameter;
import io.spring.application.CursorPager;
import io.spring.application.CursorPager.Direction;
import io.spring.application.Page;
import io.spring.application.data.CommentData;
import io.spring.application.data.ProfileData;
import io.spring.core.article.Article;
import io.spring.core.user.User;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;
import org.joda.time.DateTime;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

public class ParameterizedTests {

  // --- Property-based: Util.isEmpty invariants ---

  @ParameterizedTest
  @NullAndEmptySource
  public void isEmpty_should_return_true_for_null_and_empty(String value) {
    assertTrue(Util.isEmpty(value));
  }

  @ParameterizedTest
  @ValueSource(strings = {"a", "hello", " ", "  x  ", "1234567890"})
  public void isEmpty_should_return_false_for_non_empty(String value) {
    assertFalse(Util.isEmpty(value));
  }

  // --- Property-based: CursorPageParameter limit invariants ---

  @ParameterizedTest
  @ValueSource(ints = {-100, -1, 0})
  public void cursorPageParam_non_positive_limit_uses_default(int limit) {
    CursorPageParameter<DateTime> p = new CursorPageParameter<>(null, limit, Direction.NEXT);
    assertEquals(20, p.getLimit(), "Non-positive limits should default to 20");
  }

  @ParameterizedTest
  @ValueSource(ints = {1, 10, 500, 999, 1000})
  public void cursorPageParam_valid_limits_are_preserved(int limit) {
    CursorPageParameter<DateTime> p = new CursorPageParameter<>(null, limit, Direction.NEXT);
    assertEquals(limit, p.getLimit());
  }

  @ParameterizedTest
  @ValueSource(ints = {1001, 2000, 5000, Integer.MAX_VALUE})
  public void cursorPageParam_over_max_is_capped(int limit) {
    CursorPageParameter<DateTime> p = new CursorPageParameter<>(null, limit, Direction.NEXT);
    assertEquals(1000, p.getLimit(), "Limits > 1000 should be capped");
  }

  @ParameterizedTest
  @ValueSource(ints = {1, 5, 10, 100})
  public void cursorPageParam_queryLimit_is_always_limit_plus_one(int limit) {
    CursorPageParameter<DateTime> p = new CursorPageParameter<>(null, limit, Direction.NEXT);
    assertEquals(p.getLimit() + 1, p.getQueryLimit());
  }

  // --- Property-based: CursorPager direction invariants ---

  @Test
  public void cursorPager_next_direction_never_has_previous() {
    for (boolean hasExtra : new boolean[] {true, false}) {
      CursorPager<CommentData> pager =
          new CursorPager<>(Collections.emptyList(), Direction.NEXT, hasExtra);
      assertFalse(pager.hasPrevious(), "NEXT direction should never have previous");
      assertEquals(hasExtra, pager.hasNext());
    }
  }

  @Test
  public void cursorPager_prev_direction_never_has_next() {
    for (boolean hasExtra : new boolean[] {true, false}) {
      CursorPager<CommentData> pager =
          new CursorPager<>(Collections.emptyList(), Direction.PREV, hasExtra);
      assertFalse(pager.hasNext(), "PREV direction should never have next");
      assertEquals(hasExtra, pager.hasPrevious());
    }
  }

  // --- Data-driven: Article slug generation ---

  @ParameterizedTest
  @CsvSource({
    "Hello World, hello-world",
    "Test Article, test-article",
    "Java Spring Boot, java-spring-boot"
  })
  public void article_slug_should_contain_slugified_title(String title, String expectedPrefix) {
    Article article = new Article(title, "desc", "body", Arrays.asList("java"), "u1");
    assertTrue(
        article.getSlug().contains(expectedPrefix),
        "Slug '" + article.getSlug() + "' should contain '" + expectedPrefix + "'");
  }

  @ParameterizedTest
  @ValueSource(strings = {"Title A", "Title B", "Title C"})
  public void article_slug_is_never_null_or_empty(String title) {
    Article article = new Article(title, "desc", "body", Arrays.asList("java"), "u1");
    assertNotNull(article.getSlug());
    assertFalse(article.getSlug().isEmpty());
  }

  // --- Data-driven: User construction ---

  @ParameterizedTest
  @CsvSource({
    "a@b.com, user1, pass1, bio1, img1",
    "x@y.org, admin, secret, admin bio, admin.png",
    "test@test.com, tester, test123, , "
  })
  public void user_should_preserve_all_fields(
      String email, String username, String password, String bio, String image) {
    User user = new User(email, username, password, bio != null ? bio : "", image != null ? image : "");
    assertEquals(email, user.getEmail());
    assertEquals(username, user.getUsername());
    assertNotNull(user.getId());
  }

  // --- Data-driven: Page offset/limit ---

  static Stream<int[]> pageParams() {
    return Stream.of(new int[] {0, 20}, new int[] {1, 10}, new int[] {5, 100}, new int[] {0, 1});
  }

  @ParameterizedTest
  @MethodSource("pageParams")
  public void page_should_preserve_offset_and_limit(int[] params) {
    Page page = new Page(params[0], params[1]);
    assertEquals(params[0], page.getOffset());
    assertEquals(params[1], page.getLimit());
  }

  // --- Property-based: CommentData cursor is derived from createdAt ---

  static Stream<DateTime> dateTimes() {
    return Stream.of(
        new DateTime(2020, 1, 1, 0, 0),
        new DateTime(2023, 6, 15, 12, 30),
        new DateTime(2025, 12, 31, 23, 59));
  }

  @ParameterizedTest
  @MethodSource("dateTimes")
  public void commentData_cursor_is_based_on_createdAt(DateTime createdAt) {
    ProfileData profile = new ProfileData("id", "user", "bio", "img", false);
    CommentData cd = new CommentData("c1", "body", "a1", createdAt, new DateTime(), profile);
    assertNotNull(cd.getCursor());
    assertTrue(cd.getCursor().toString().length() > 0);
  }
}
