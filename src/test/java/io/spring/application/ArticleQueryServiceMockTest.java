package io.spring.application;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import io.spring.application.data.ArticleData;
import io.spring.application.data.ArticleDataList;
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
import java.util.Set;
import org.joda.time.DateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class ArticleQueryServiceMockTest {

  @Mock private ArticleReadService articleReadService;
  @Mock private UserRelationshipQueryService userRelationshipQueryService;
  @Mock private ArticleFavoritesReadService articleFavoritesReadService;

  @InjectMocks private ArticleQueryService articleQueryService;

  private User user;
  private ArticleData articleData;

  @BeforeEach
  void setUp() {
    user = new User("test@test.com", "testuser", "password", "bio", "image");
    articleData =
        new ArticleData(
            "article-id",
            "test-slug",
            "Test Title",
            "desc",
            "body",
            false,
            0,
            new DateTime(),
            new DateTime(),
            Arrays.asList("java"),
            new ProfileData(user.getId(), user.getUsername(), "", "", false));
  }

  @Test
  void should_find_by_id_with_user() {
    when(articleReadService.findById(eq("article-id"))).thenReturn(articleData);
    when(articleFavoritesReadService.isUserFavorite(eq(user.getId()), eq("article-id")))
        .thenReturn(true);
    when(articleFavoritesReadService.articleFavoriteCount(eq("article-id"))).thenReturn(5);
    when(userRelationshipQueryService.isUserFollowing(any(), any())).thenReturn(false);

    Optional<ArticleData> result = articleQueryService.findById("article-id", user);

    assertTrue(result.isPresent());
    assertTrue(result.get().isFavorited());
    assertEquals(5, result.get().getFavoritesCount());
  }

  @Test
  void should_find_by_id_without_user() {
    when(articleReadService.findById(eq("article-id"))).thenReturn(articleData);

    Optional<ArticleData> result = articleQueryService.findById("article-id", null);

    assertTrue(result.isPresent());
  }

  @Test
  void should_return_empty_when_id_not_found() {
    when(articleReadService.findById(eq("nonexistent"))).thenReturn(null);

    Optional<ArticleData> result = articleQueryService.findById("nonexistent", user);

    assertFalse(result.isPresent());
  }

  @Test
  void should_find_by_slug_with_user() {
    when(articleReadService.findBySlug(eq("test-slug"))).thenReturn(articleData);
    when(articleFavoritesReadService.isUserFavorite(any(), any())).thenReturn(false);
    when(articleFavoritesReadService.articleFavoriteCount(any())).thenReturn(0);
    when(userRelationshipQueryService.isUserFollowing(any(), any())).thenReturn(true);

    Optional<ArticleData> result = articleQueryService.findBySlug("test-slug", user);

    assertTrue(result.isPresent());
    assertTrue(result.get().getProfileData().isFollowing());
  }

  @Test
  void should_find_by_slug_without_user() {
    when(articleReadService.findBySlug(eq("test-slug"))).thenReturn(articleData);

    Optional<ArticleData> result = articleQueryService.findBySlug("test-slug", null);

    assertTrue(result.isPresent());
  }

  @Test
  void should_return_empty_when_slug_not_found() {
    when(articleReadService.findBySlug(eq("nonexistent"))).thenReturn(null);

    Optional<ArticleData> result = articleQueryService.findBySlug("nonexistent", user);

    assertFalse(result.isPresent());
  }

  @Test
  void should_find_recent_articles_with_cursor_empty() {
    CursorPageParameter<DateTime> page =
        new CursorPageParameter<>(null, 10, CursorPager.Direction.NEXT);
    when(articleReadService.findArticlesWithCursor(any(), any(), any(), any()))
        .thenReturn(Collections.emptyList());

    CursorPager<ArticleData> result =
        articleQueryService.findRecentArticlesWithCursor(null, null, null, page, user);

    assertTrue(result.getData().isEmpty());
  }

  @Test
  void should_find_recent_articles_with_cursor_has_extra() {
    CursorPageParameter<DateTime> page =
        new CursorPageParameter<>(null, 1, CursorPager.Direction.NEXT);
    List<String> ids = new ArrayList<>(Arrays.asList("id1", "id2"));
    when(articleReadService.findArticlesWithCursor(any(), any(), any(), any())).thenReturn(ids);
    when(articleReadService.findArticles(any())).thenReturn(Arrays.asList(articleData));
    when(articleFavoritesReadService.articlesFavoriteCount(any()))
        .thenReturn(Arrays.asList(new ArticleFavoriteCount("article-id", 0)));

    CursorPager<ArticleData> result =
        articleQueryService.findRecentArticlesWithCursor(null, null, null, page, user);

    assertTrue(result.hasNext());
    assertEquals(1, result.getData().size());
  }

  @Test
  void should_find_recent_articles_with_cursor_prev_direction() {
    CursorPageParameter<DateTime> page =
        new CursorPageParameter<>(null, 10, CursorPager.Direction.PREV);
    List<String> ids = new ArrayList<>(Arrays.asList("id1"));
    when(articleReadService.findArticlesWithCursor(any(), any(), any(), any())).thenReturn(ids);
    when(articleReadService.findArticles(any())).thenReturn(Arrays.asList(articleData));
    when(articleFavoritesReadService.articlesFavoriteCount(any()))
        .thenReturn(Arrays.asList(new ArticleFavoriteCount("article-id", 0)));

    CursorPager<ArticleData> result =
        articleQueryService.findRecentArticlesWithCursor(null, null, null, page, user);

    assertEquals(1, result.getData().size());
  }

  @Test
  void should_find_recent_articles_with_cursor_with_user_favorites() {
    CursorPageParameter<DateTime> page =
        new CursorPageParameter<>(null, 10, CursorPager.Direction.NEXT);
    List<String> ids = new ArrayList<>(Arrays.asList("article-id"));
    when(articleReadService.findArticlesWithCursor(any(), any(), any(), any())).thenReturn(ids);
    when(articleReadService.findArticles(any())).thenReturn(Arrays.asList(articleData));
    when(articleFavoritesReadService.articlesFavoriteCount(any()))
        .thenReturn(Arrays.asList(new ArticleFavoriteCount("article-id", 3)));
    Set<String> favSet = new HashSet<>(Arrays.asList("article-id"));
    when(articleFavoritesReadService.userFavorites(any(), eq(user))).thenReturn(favSet);
    Set<String> followSet = new HashSet<>(Arrays.asList(user.getId()));
    when(userRelationshipQueryService.followingAuthors(eq(user.getId()), any()))
        .thenReturn(followSet);

    CursorPager<ArticleData> result =
        articleQueryService.findRecentArticlesWithCursor(null, null, null, page, user);

    assertEquals(1, result.getData().size());
  }

  @Test
  void should_find_recent_articles_without_user() {
    CursorPageParameter<DateTime> page =
        new CursorPageParameter<>(null, 10, CursorPager.Direction.NEXT);
    List<String> ids = new ArrayList<>(Arrays.asList("article-id"));
    when(articleReadService.findArticlesWithCursor(any(), any(), any(), any())).thenReturn(ids);
    when(articleReadService.findArticles(any())).thenReturn(Arrays.asList(articleData));
    when(articleFavoritesReadService.articlesFavoriteCount(any()))
        .thenReturn(Arrays.asList(new ArticleFavoriteCount("article-id", 0)));

    CursorPager<ArticleData> result =
        articleQueryService.findRecentArticlesWithCursor(null, null, null, page, null);

    assertEquals(1, result.getData().size());
  }

  @Test
  void should_find_user_feed_with_cursor_no_follows() {
    CursorPageParameter<DateTime> page =
        new CursorPageParameter<>(null, 10, CursorPager.Direction.NEXT);
    when(userRelationshipQueryService.followedUsers(eq(user.getId())))
        .thenReturn(Collections.emptyList());

    CursorPager<ArticleData> result = articleQueryService.findUserFeedWithCursor(user, page);

    assertTrue(result.getData().isEmpty());
  }

  @Test
  void should_find_user_feed_with_cursor_has_follows() {
    CursorPageParameter<DateTime> page =
        new CursorPageParameter<>(null, 10, CursorPager.Direction.NEXT);
    when(userRelationshipQueryService.followedUsers(eq(user.getId())))
        .thenReturn(Arrays.asList("author1"));
    when(articleReadService.findArticlesOfAuthorsWithCursor(any(), any()))
        .thenReturn(new ArrayList<>(Arrays.asList(articleData)));
    when(articleFavoritesReadService.articlesFavoriteCount(any()))
        .thenReturn(Arrays.asList(new ArticleFavoriteCount("article-id", 0)));

    CursorPager<ArticleData> result = articleQueryService.findUserFeedWithCursor(user, page);

    assertEquals(1, result.getData().size());
  }

  @Test
  void should_find_user_feed_with_cursor_has_extra() {
    CursorPageParameter<DateTime> page =
        new CursorPageParameter<>(null, 1, CursorPager.Direction.NEXT);
    when(userRelationshipQueryService.followedUsers(eq(user.getId())))
        .thenReturn(Arrays.asList("author1"));
    ArticleData article2 =
        new ArticleData(
            "id2",
            "slug2",
            "Title2",
            "desc",
            "body",
            false,
            0,
            new DateTime(),
            new DateTime(),
            Arrays.asList("java"),
            new ProfileData("other-id", "other", "", "", false));
    when(articleReadService.findArticlesOfAuthorsWithCursor(any(), any()))
        .thenReturn(new ArrayList<>(Arrays.asList(articleData, article2)));
    when(articleFavoritesReadService.articlesFavoriteCount(any()))
        .thenReturn(
            Arrays.asList(
                new ArticleFavoriteCount("article-id", 0), new ArticleFavoriteCount("id2", 0)));

    CursorPager<ArticleData> result = articleQueryService.findUserFeedWithCursor(user, page);

    assertTrue(result.hasNext());
    assertEquals(1, result.getData().size());
  }

  @Test
  void should_find_recent_articles_page_empty() {
    Page page = new Page(0, 10);
    when(articleReadService.queryArticles(any(), any(), any(), any()))
        .thenReturn(Collections.emptyList());
    when(articleReadService.countArticle(any(), any(), any())).thenReturn(0);

    ArticleDataList result = articleQueryService.findRecentArticles(null, null, null, page, user);

    assertTrue(result.getArticleDatas().isEmpty());
    assertEquals(0, result.getCount());
  }

  @Test
  void should_find_recent_articles_page_with_results() {
    Page page = new Page(0, 10);
    when(articleReadService.queryArticles(any(), any(), any(), any()))
        .thenReturn(Arrays.asList("article-id"));
    when(articleReadService.countArticle(any(), any(), any())).thenReturn(1);
    when(articleReadService.findArticles(any())).thenReturn(Arrays.asList(articleData));
    when(articleFavoritesReadService.articlesFavoriteCount(any()))
        .thenReturn(Arrays.asList(new ArticleFavoriteCount("article-id", 0)));

    ArticleDataList result = articleQueryService.findRecentArticles(null, null, null, page, user);

    assertEquals(1, result.getArticleDatas().size());
    assertEquals(1, result.getCount());
  }

  @Test
  void should_find_user_feed_page_no_follows() {
    Page page = new Page(0, 10);
    when(userRelationshipQueryService.followedUsers(eq(user.getId())))
        .thenReturn(Collections.emptyList());

    ArticleDataList result = articleQueryService.findUserFeed(user, page);

    assertTrue(result.getArticleDatas().isEmpty());
    assertEquals(0, result.getCount());
  }

  @Test
  void should_find_user_feed_page_with_follows() {
    Page page = new Page(0, 10);
    when(userRelationshipQueryService.followedUsers(eq(user.getId())))
        .thenReturn(Arrays.asList("author1"));
    when(articleReadService.findArticlesOfAuthors(any(), any()))
        .thenReturn(Arrays.asList(articleData));
    when(articleReadService.countFeedSize(any())).thenReturn(1);
    when(articleFavoritesReadService.articlesFavoriteCount(any()))
        .thenReturn(Arrays.asList(new ArticleFavoriteCount("article-id", 0)));

    ArticleDataList result = articleQueryService.findUserFeed(user, page);

    assertEquals(1, result.getArticleDatas().size());
    assertEquals(1, result.getCount());
  }
}
