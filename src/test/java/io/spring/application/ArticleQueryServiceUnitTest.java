package io.spring.application;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import io.spring.application.CursorPager.Direction;
import io.spring.application.data.ArticleData;
import io.spring.application.data.ArticleFavoriteCount;
import io.spring.application.data.ProfileData;
import io.spring.core.user.User;
import io.spring.infrastructure.mybatis.readservice.ArticleFavoritesReadService;
import io.spring.infrastructure.mybatis.readservice.ArticleReadService;
import io.spring.infrastructure.mybatis.readservice.UserRelationshipQueryService;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import org.joda.time.DateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ArticleQueryServiceUnitTest {

  private ArticleReadService articleReadService;
  private UserRelationshipQueryService userRelationshipQueryService;
  private ArticleFavoritesReadService articleFavoritesReadService;
  private ArticleQueryService articleQueryService;
  private User user;
  private ProfileData profileData;

  @BeforeEach
  public void setUp() {
    articleReadService = mock(ArticleReadService.class);
    userRelationshipQueryService = mock(UserRelationshipQueryService.class);
    articleFavoritesReadService = mock(ArticleFavoritesReadService.class);
    articleQueryService =
        new ArticleQueryService(
            articleReadService, userRelationshipQueryService, articleFavoritesReadService);
    user = new User("test@test.com", "testuser", "pass", "", "");
    profileData = new ProfileData(user.getId(), "testuser", "bio", "img", false);
  }

  private ArticleData createArticleData(String id) {
    return new ArticleData(
        id,
        "slug-" + id,
        "Title " + id,
        "Desc",
        "Body",
        false,
        0,
        new DateTime(),
        new DateTime(),
        new ArrayList<String>(),
        profileData);
  }

  private void stubFillExtraInfoForList(String... articleIds) {
    List<ArticleFavoriteCount> favCounts = new ArrayList<>();
    for (String id : articleIds) {
      favCounts.add(new ArticleFavoriteCount(id, 0));
    }
    when(articleFavoritesReadService.articlesFavoriteCount(anyList())).thenReturn(favCounts);
    when(articleFavoritesReadService.userFavorites(anyList(), any(User.class)))
        .thenReturn(new HashSet<>());
    when(userRelationshipQueryService.followingAuthors(anyString(), anyList()))
        .thenReturn(new HashSet<>());
  }

  private void stubFillExtraInfoForSingle() {
    when(articleFavoritesReadService.isUserFavorite(anyString(), anyString())).thenReturn(false);
    when(articleFavoritesReadService.articleFavoriteCount(anyString())).thenReturn(0);
    when(userRelationshipQueryService.isUserFollowing(anyString(), anyString())).thenReturn(false);
  }

  @Test
  public void should_find_by_id_return_empty_when_null() {
    when(articleReadService.findById("nonexistent")).thenReturn(null);
    Optional<ArticleData> result = articleQueryService.findById("nonexistent", user);
    assertFalse(result.isPresent());
  }

  @Test
  public void should_find_by_id_with_user() {
    ArticleData data = createArticleData("a1");
    when(articleReadService.findById("a1")).thenReturn(data);
    stubFillExtraInfoForSingle();

    Optional<ArticleData> result = articleQueryService.findById("a1", user);
    assertTrue(result.isPresent());
  }

  @Test
  public void should_find_by_id_without_user() {
    ArticleData data = createArticleData("a1");
    when(articleReadService.findById("a1")).thenReturn(data);

    Optional<ArticleData> result = articleQueryService.findById("a1", null);
    assertTrue(result.isPresent());
  }

  @Test
  public void should_find_by_slug_return_empty_when_null() {
    when(articleReadService.findBySlug("nonexistent")).thenReturn(null);
    Optional<ArticleData> result = articleQueryService.findBySlug("nonexistent", user);
    assertFalse(result.isPresent());
  }

  @Test
  public void should_find_by_slug_success() {
    ArticleData data = createArticleData("a1");
    when(articleReadService.findBySlug("slug-a1")).thenReturn(data);
    stubFillExtraInfoForSingle();

    Optional<ArticleData> result = articleQueryService.findBySlug("slug-a1", user);
    assertTrue(result.isPresent());
  }

  @Test
  public void should_find_by_slug_without_user() {
    ArticleData data = createArticleData("a1");
    when(articleReadService.findBySlug("slug-a1")).thenReturn(data);

    Optional<ArticleData> result = articleQueryService.findBySlug("slug-a1", null);
    assertTrue(result.isPresent());
  }

  @Test
  public void should_find_recent_articles_with_cursor_empty() {
    when(articleReadService.findArticlesWithCursor(any(), any(), any(), any()))
        .thenReturn(new ArrayList<>());

    CursorPageParameter<DateTime> page = new CursorPageParameter<>(null, 10, Direction.NEXT);
    CursorPager<ArticleData> result =
        articleQueryService.findRecentArticlesWithCursor(null, null, null, page, user);
    assertTrue(result.getData().isEmpty());
  }

  @Test
  public void should_find_recent_articles_with_cursor_has_extra() {
    when(articleReadService.findArticlesWithCursor(any(), any(), any(), any()))
        .thenReturn(new ArrayList<>(Arrays.asList("a1", "a2")));
    ArticleData data1 = createArticleData("a1");
    when(articleReadService.findArticles(anyList()))
        .thenReturn(Collections.singletonList(data1));
    stubFillExtraInfoForList("a1");

    CursorPageParameter<DateTime> page = new CursorPageParameter<>(null, 1, Direction.NEXT);
    CursorPager<ArticleData> result =
        articleQueryService.findRecentArticlesWithCursor(null, null, null, page, user);
    assertTrue(result.hasNext());
  }

  @Test
  public void should_find_recent_articles_with_cursor_prev_direction() {
    when(articleReadService.findArticlesWithCursor(any(), any(), any(), any()))
        .thenReturn(new ArrayList<>(Collections.singletonList("a1")));
    ArticleData data1 = createArticleData("a1");
    when(articleReadService.findArticles(anyList()))
        .thenReturn(Collections.singletonList(data1));
    List<ArticleFavoriteCount> favCounts = new ArrayList<>();
    favCounts.add(new ArticleFavoriteCount("a1", 0));
    when(articleFavoritesReadService.articlesFavoriteCount(anyList())).thenReturn(favCounts);

    CursorPageParameter<DateTime> page = new CursorPageParameter<>(null, 10, Direction.PREV);
    CursorPager<ArticleData> result =
        articleQueryService.findRecentArticlesWithCursor(null, null, null, page, null);
    assertEquals(1, result.getData().size());
  }

  @Test
  public void should_find_user_feed_with_cursor_empty_followed_users() {
    when(userRelationshipQueryService.followedUsers(user.getId()))
        .thenReturn(Collections.emptyList());

    CursorPageParameter<DateTime> page = new CursorPageParameter<>(null, 10, Direction.NEXT);
    CursorPager<ArticleData> result = articleQueryService.findUserFeedWithCursor(user, page);
    assertTrue(result.getData().isEmpty());
  }

  @Test
  public void should_find_user_feed_with_cursor_success() {
    when(userRelationshipQueryService.followedUsers(user.getId()))
        .thenReturn(Collections.singletonList("followed-user-id"));
    ArticleData data1 = createArticleData("a1");
    when(articleReadService.findArticlesOfAuthorsWithCursor(anyList(), any()))
        .thenReturn(new ArrayList<>(Collections.singletonList(data1)));
    stubFillExtraInfoForList("a1");

    CursorPageParameter<DateTime> page = new CursorPageParameter<>(null, 10, Direction.NEXT);
    CursorPager<ArticleData> result = articleQueryService.findUserFeedWithCursor(user, page);
    assertEquals(1, result.getData().size());
  }

  @Test
  public void should_find_user_feed_with_cursor_has_extra_and_prev() {
    when(userRelationshipQueryService.followedUsers(user.getId()))
        .thenReturn(Collections.singletonList("followed-user-id"));
    ArticleData data1 = createArticleData("a1");
    ArticleData data2 = createArticleData("a2");
    when(articleReadService.findArticlesOfAuthorsWithCursor(anyList(), any()))
        .thenReturn(new ArrayList<>(Arrays.asList(data1, data2)));
    stubFillExtraInfoForList("a1", "a2");

    CursorPageParameter<DateTime> page = new CursorPageParameter<>(null, 1, Direction.PREV);
    CursorPager<ArticleData> result = articleQueryService.findUserFeedWithCursor(user, page);
    assertTrue(result.hasPrevious());
    assertEquals(1, result.getData().size());
  }
}
