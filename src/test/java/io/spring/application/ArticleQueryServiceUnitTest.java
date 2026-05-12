package io.spring.application;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import io.spring.application.CursorPager.Direction;
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
import java.util.Optional;
import org.joda.time.DateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class ArticleQueryServiceUnitTest {

  @Mock private ArticleReadService articleReadService;
  @Mock private UserRelationshipQueryService userRelationshipQueryService;
  @Mock private ArticleFavoritesReadService articleFavoritesReadService;

  @InjectMocks private ArticleQueryService articleQueryService;

  private User user;
  private ArticleData articleData;
  private ProfileData profileData;

  @BeforeEach
  void setUp() {
    user = new User("test@test.com", "testuser", "pass", "bio", "image");
    profileData = new ProfileData("authorId", "author", "bio", "image", false);
    DateTime now = new DateTime();
    articleData =
        new ArticleData(
            "a1",
            "test-slug",
            "Test Title",
            "desc",
            "body",
            false,
            0,
            now,
            now,
            Arrays.asList("java"),
            profileData);
  }

  @Test
  void should_find_by_id_with_user() {
    when(articleReadService.findById("a1")).thenReturn(articleData);
    when(articleFavoritesReadService.isUserFavorite(user.getId(), "a1")).thenReturn(true);
    when(articleFavoritesReadService.articleFavoriteCount("a1")).thenReturn(5);
    when(userRelationshipQueryService.isUserFollowing(user.getId(), "authorId")).thenReturn(true);

    Optional<ArticleData> result = articleQueryService.findById("a1", user);

    assertTrue(result.isPresent());
    assertTrue(result.get().isFavorited());
    assertEquals(5, result.get().getFavoritesCount());
    assertTrue(result.get().getProfileData().isFollowing());
  }

  @Test
  void should_find_by_id_without_user() {
    when(articleReadService.findById("a1")).thenReturn(articleData);

    Optional<ArticleData> result = articleQueryService.findById("a1", null);

    assertTrue(result.isPresent());
  }

  @Test
  void should_return_empty_when_article_not_found_by_id() {
    when(articleReadService.findById("nonexistent")).thenReturn(null);

    Optional<ArticleData> result = articleQueryService.findById("nonexistent", user);

    assertTrue(result.isEmpty());
  }

  @Test
  void should_find_by_slug_with_user() {
    when(articleReadService.findBySlug("test-slug")).thenReturn(articleData);
    when(articleFavoritesReadService.isUserFavorite(user.getId(), "a1")).thenReturn(false);
    when(articleFavoritesReadService.articleFavoriteCount("a1")).thenReturn(0);
    when(userRelationshipQueryService.isUserFollowing(user.getId(), "authorId")).thenReturn(false);

    Optional<ArticleData> result = articleQueryService.findBySlug("test-slug", user);

    assertTrue(result.isPresent());
    assertFalse(result.get().isFavorited());
  }

  @Test
  void should_return_empty_when_slug_not_found() {
    when(articleReadService.findBySlug("nonexistent")).thenReturn(null);

    Optional<ArticleData> result = articleQueryService.findBySlug("nonexistent", user);

    assertTrue(result.isEmpty());
  }

  @Test
  void should_find_recent_articles_with_cursor_empty() {
    CursorPageParameter<DateTime> page = new CursorPageParameter<>(null, 10, Direction.NEXT);
    when(articleReadService.findArticlesWithCursor(any(), any(), any(), any()))
        .thenReturn(Collections.emptyList());

    CursorPager<ArticleData> result =
        articleQueryService.findRecentArticlesWithCursor(null, null, null, page, user);

    assertTrue(result.getData().isEmpty());
  }

  @Test
  void should_find_recent_articles_with_cursor_has_results() {
    CursorPageParameter<DateTime> page = new CursorPageParameter<>(null, 10, Direction.NEXT);
    when(articleReadService.findArticlesWithCursor(any(), any(), any(), any()))
        .thenReturn(new ArrayList<>(Arrays.asList("a1")));
    when(articleReadService.findArticles(anyList())).thenReturn(Arrays.asList(articleData));
    when(articleFavoritesReadService.articlesFavoriteCount(anyList()))
        .thenReturn(Arrays.asList(new ArticleFavoriteCount("a1", 3)));
    when(articleFavoritesReadService.userFavorites(anyList(), eq(user)))
        .thenReturn(new HashSet<>(Arrays.asList("a1")));
    when(userRelationshipQueryService.followingAuthors(eq(user.getId()), anyList()))
        .thenReturn(new HashSet<>());

    CursorPager<ArticleData> result =
        articleQueryService.findRecentArticlesWithCursor(null, null, null, page, user);

    assertFalse(result.getData().isEmpty());
  }

  @Test
  void should_find_recent_articles_with_cursor_prev_direction() {
    CursorPageParameter<DateTime> page = new CursorPageParameter<>(null, 10, Direction.PREV);
    when(articleReadService.findArticlesWithCursor(any(), any(), any(), any()))
        .thenReturn(new ArrayList<>(Arrays.asList("a1")));
    when(articleReadService.findArticles(anyList())).thenReturn(Arrays.asList(articleData));
    when(articleFavoritesReadService.articlesFavoriteCount(anyList()))
        .thenReturn(Arrays.asList(new ArticleFavoriteCount("a1", 0)));

    CursorPager<ArticleData> result =
        articleQueryService.findRecentArticlesWithCursor(null, null, null, page, null);

    assertFalse(result.getData().isEmpty());
  }

  @Test
  void should_find_user_feed_with_cursor_no_followed_users() {
    CursorPageParameter<DateTime> page = new CursorPageParameter<>(null, 10, Direction.NEXT);
    when(userRelationshipQueryService.followedUsers(user.getId()))
        .thenReturn(Collections.emptyList());

    CursorPager<ArticleData> result = articleQueryService.findUserFeedWithCursor(user, page);

    assertTrue(result.getData().isEmpty());
  }

  @Test
  void should_find_user_feed_with_cursor_has_followed_users() {
    CursorPageParameter<DateTime> page = new CursorPageParameter<>(null, 10, Direction.NEXT);
    when(userRelationshipQueryService.followedUsers(user.getId()))
        .thenReturn(Arrays.asList("followedUser1"));
    when(articleReadService.findArticlesOfAuthorsWithCursor(anyList(), any()))
        .thenReturn(new ArrayList<>(Arrays.asList(articleData)));
    when(articleFavoritesReadService.articlesFavoriteCount(anyList()))
        .thenReturn(Arrays.asList(new ArticleFavoriteCount("a1", 0)));
    when(articleFavoritesReadService.userFavorites(anyList(), eq(user)))
        .thenReturn(new HashSet<>());
    when(userRelationshipQueryService.followingAuthors(eq(user.getId()), anyList()))
        .thenReturn(new HashSet<>());

    CursorPager<ArticleData> result = articleQueryService.findUserFeedWithCursor(user, page);

    assertFalse(result.getData().isEmpty());
  }

  @Test
  void should_find_recent_articles_with_page() {
    Page page = new Page(0, 10);
    when(articleReadService.queryArticles(any(), any(), any(), eq(page)))
        .thenReturn(Arrays.asList("a1"));
    when(articleReadService.countArticle(any(), any(), any())).thenReturn(1);
    when(articleReadService.findArticles(anyList())).thenReturn(Arrays.asList(articleData));
    when(articleFavoritesReadService.articlesFavoriteCount(anyList()))
        .thenReturn(Arrays.asList(new ArticleFavoriteCount("a1", 0)));

    ArticleDataList result = articleQueryService.findRecentArticles(null, null, null, page, null);

    assertEquals(1, result.getArticleDatas().size());
    assertEquals(1, result.getCount());
  }

  @Test
  void should_find_recent_articles_with_page_empty() {
    Page page = new Page(0, 10);
    when(articleReadService.queryArticles(any(), any(), any(), eq(page)))
        .thenReturn(Collections.emptyList());
    when(articleReadService.countArticle(any(), any(), any())).thenReturn(0);

    ArticleDataList result = articleQueryService.findRecentArticles(null, null, null, page, null);

    assertTrue(result.getArticleDatas().isEmpty());
    assertEquals(0, result.getCount());
  }

  @Test
  void should_find_user_feed_with_page_no_followed_users() {
    Page page = new Page(0, 10);
    when(userRelationshipQueryService.followedUsers(user.getId()))
        .thenReturn(Collections.emptyList());

    ArticleDataList result = articleQueryService.findUserFeed(user, page);

    assertTrue(result.getArticleDatas().isEmpty());
    assertEquals(0, result.getCount());
  }

  @Test
  void should_find_user_feed_with_page_has_followed_users() {
    Page page = new Page(0, 10);
    when(userRelationshipQueryService.followedUsers(user.getId()))
        .thenReturn(Arrays.asList("followedUser1"));
    when(articleReadService.findArticlesOfAuthors(anyList(), eq(page)))
        .thenReturn(Arrays.asList(articleData));
    when(articleReadService.countFeedSize(anyList())).thenReturn(1);
    when(articleFavoritesReadService.articlesFavoriteCount(anyList()))
        .thenReturn(Arrays.asList(new ArticleFavoriteCount("a1", 0)));
    when(articleFavoritesReadService.userFavorites(anyList(), eq(user)))
        .thenReturn(new HashSet<>());
    when(userRelationshipQueryService.followingAuthors(eq(user.getId()), anyList()))
        .thenReturn(new HashSet<>());

    ArticleDataList result = articleQueryService.findUserFeed(user, page);

    assertFalse(result.getArticleDatas().isEmpty());
    assertEquals(1, result.getCount());
  }
}
