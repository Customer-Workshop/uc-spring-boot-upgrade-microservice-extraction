package io.spring.application;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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
public class ArticleQueryServiceAdditionalTest {

  @Mock private ArticleReadService articleReadService;
  @Mock private UserRelationshipQueryService userRelationshipQueryService;
  @Mock private ArticleFavoritesReadService articleFavoritesReadService;

  @InjectMocks private ArticleQueryService articleQueryService;

  private User user;
  private ArticleData articleData;

  @BeforeEach
  void setUp() {
    user = new User("test@test.com", "testuser", "password", "bio", "image");
    ProfileData profile = new ProfileData("author-id", "author", "bio", "img", false);
    articleData = new ArticleData();
    articleData.setId("a1");
    articleData.setSlug("test-slug");
    articleData.setTitle("title");
    articleData.setDescription("desc");
    articleData.setBody("body");
    articleData.setCreatedAt(new DateTime());
    articleData.setUpdatedAt(new DateTime());
    articleData.setTagList(Arrays.asList("java"));
    articleData.setProfileData(profile);
  }

  @Test
  void should_find_by_id_with_user() {
    when(articleReadService.findById("a1")).thenReturn(articleData);
    when(articleFavoritesReadService.isUserFavorite(user.getId(), "a1")).thenReturn(true);
    when(articleFavoritesReadService.articleFavoriteCount("a1")).thenReturn(5);
    when(userRelationshipQueryService.isUserFollowing(user.getId(), "author-id")).thenReturn(true);

    Optional<ArticleData> result = articleQueryService.findById("a1", user);

    assertTrue(result.isPresent());
    assertTrue(result.get().isFavorited());
    assertEquals(5, result.get().getFavoritesCount());
  }

  @Test
  void should_find_by_id_without_user() {
    when(articleReadService.findById("a1")).thenReturn(articleData);

    Optional<ArticleData> result = articleQueryService.findById("a1", null);

    assertTrue(result.isPresent());
  }

  @Test
  void should_return_empty_when_not_found_by_id() {
    when(articleReadService.findById("missing")).thenReturn(null);

    Optional<ArticleData> result = articleQueryService.findById("missing", user);

    assertFalse(result.isPresent());
  }

  @Test
  void should_find_by_slug_with_user() {
    when(articleReadService.findBySlug("test-slug")).thenReturn(articleData);
    when(articleFavoritesReadService.isUserFavorite(user.getId(), "a1")).thenReturn(false);
    when(articleFavoritesReadService.articleFavoriteCount("a1")).thenReturn(0);
    when(userRelationshipQueryService.isUserFollowing(user.getId(), "author-id")).thenReturn(false);

    Optional<ArticleData> result = articleQueryService.findBySlug("test-slug", user);

    assertTrue(result.isPresent());
  }

  @Test
  void should_return_empty_when_not_found_by_slug() {
    when(articleReadService.findBySlug("missing")).thenReturn(null);

    Optional<ArticleData> result = articleQueryService.findBySlug("missing", user);

    assertFalse(result.isPresent());
  }

  @Test
  void should_find_recent_articles_empty() {
    when(articleReadService.queryArticles(any(), any(), any(), any()))
        .thenReturn(Collections.emptyList());
    when(articleReadService.countArticle(any(), any(), any())).thenReturn(0);

    ArticleDataList result =
        articleQueryService.findRecentArticles(null, null, null, new Page(), null);

    assertTrue(result.getArticleDatas().isEmpty());
    assertEquals(0, result.getCount());
  }

  @Test
  void should_find_recent_articles_with_user() {
    when(articleReadService.queryArticles(any(), any(), any(), any()))
        .thenReturn(Arrays.asList("a1"));
    when(articleReadService.countArticle(any(), any(), any())).thenReturn(1);
    when(articleReadService.findArticles(Arrays.asList("a1")))
        .thenReturn(Arrays.asList(articleData));
    when(articleFavoritesReadService.articlesFavoriteCount(any()))
        .thenReturn(Arrays.asList(new ArticleFavoriteCount("a1", 3)));
    when(articleFavoritesReadService.userFavorites(any(), eq(user)))
        .thenReturn(new HashSet<>(Arrays.asList("a1")));
    when(userRelationshipQueryService.followingAuthors(eq(user.getId()), any()))
        .thenReturn(new HashSet<>(Arrays.asList("author-id")));

    ArticleDataList result =
        articleQueryService.findRecentArticles(null, null, null, new Page(), user);

    assertEquals(1, result.getArticleDatas().size());
    assertTrue(result.getArticleDatas().get(0).isFavorited());
  }

  @Test
  void should_find_user_feed_empty() {
    when(userRelationshipQueryService.followedUsers(user.getId()))
        .thenReturn(Collections.emptyList());

    ArticleDataList result = articleQueryService.findUserFeed(user, new Page());

    assertTrue(result.getArticleDatas().isEmpty());
    assertEquals(0, result.getCount());
  }

  @Test
  void should_find_user_feed_with_articles() {
    when(userRelationshipQueryService.followedUsers(user.getId()))
        .thenReturn(Arrays.asList("followed-id"));
    when(articleReadService.findArticlesOfAuthors(any(), any()))
        .thenReturn(Arrays.asList(articleData));
    when(articleFavoritesReadService.articlesFavoriteCount(any()))
        .thenReturn(Arrays.asList(new ArticleFavoriteCount("a1", 1)));
    when(articleFavoritesReadService.userFavorites(any(), eq(user))).thenReturn(new HashSet<>());
    when(userRelationshipQueryService.followingAuthors(eq(user.getId()), any()))
        .thenReturn(new HashSet<>());
    when(articleReadService.countFeedSize(any())).thenReturn(1);

    ArticleDataList result = articleQueryService.findUserFeed(user, new Page());

    assertEquals(1, result.getArticleDatas().size());
    assertEquals(1, result.getCount());
  }

  @Test
  void should_find_recent_articles_with_cursor_empty() {
    CursorPageParameter<DateTime> page = new CursorPageParameter<>(null, 10, Direction.NEXT);
    when(articleReadService.findArticlesWithCursor(any(), any(), any(), any()))
        .thenReturn(Collections.emptyList());

    CursorPager<ArticleData> result =
        articleQueryService.findRecentArticlesWithCursor(null, null, null, page, null);

    assertTrue(result.getData().isEmpty());
  }

  @Test
  void should_find_recent_articles_with_cursor_has_extra() {
    CursorPageParameter<DateTime> page = new CursorPageParameter<>(null, 1, Direction.NEXT);
    when(articleReadService.findArticlesWithCursor(any(), any(), any(), any()))
        .thenReturn(new ArrayList<>(Arrays.asList("a1", "a2")));
    when(articleReadService.findArticles(any())).thenReturn(Arrays.asList(articleData));
    when(articleFavoritesReadService.articlesFavoriteCount(any()))
        .thenReturn(Arrays.asList(new ArticleFavoriteCount("a1", 0)));

    CursorPager<ArticleData> result =
        articleQueryService.findRecentArticlesWithCursor(null, null, null, page, null);

    assertTrue(result.hasNext());
  }

  @Test
  void should_find_recent_articles_with_cursor_prev_direction() {
    CursorPageParameter<DateTime> page = new CursorPageParameter<>(null, 10, Direction.PREV);
    when(articleReadService.findArticlesWithCursor(any(), any(), any(), any()))
        .thenReturn(new ArrayList<>(Arrays.asList("a1")));
    when(articleReadService.findArticles(any())).thenReturn(Arrays.asList(articleData));
    when(articleFavoritesReadService.articlesFavoriteCount(any()))
        .thenReturn(Arrays.asList(new ArticleFavoriteCount("a1", 0)));

    CursorPager<ArticleData> result =
        articleQueryService.findRecentArticlesWithCursor(null, null, null, page, null);

    assertFalse(result.hasNext());
    assertFalse(result.hasPrevious());
  }

  @Test
  void should_find_user_feed_with_cursor_empty_followed() {
    CursorPageParameter<DateTime> page = new CursorPageParameter<>(null, 10, Direction.NEXT);
    when(userRelationshipQueryService.followedUsers(user.getId()))
        .thenReturn(Collections.emptyList());

    CursorPager<ArticleData> result = articleQueryService.findUserFeedWithCursor(user, page);

    assertTrue(result.getData().isEmpty());
  }

  @Test
  void should_find_user_feed_with_cursor_has_articles() {
    CursorPageParameter<DateTime> page = new CursorPageParameter<>(null, 10, Direction.NEXT);
    when(userRelationshipQueryService.followedUsers(user.getId()))
        .thenReturn(Arrays.asList("followed-id"));
    when(articleReadService.findArticlesOfAuthorsWithCursor(any(), any()))
        .thenReturn(new ArrayList<>(Arrays.asList(articleData)));
    when(articleFavoritesReadService.articlesFavoriteCount(any()))
        .thenReturn(Arrays.asList(new ArticleFavoriteCount("a1", 0)));
    when(articleFavoritesReadService.userFavorites(any(), eq(user))).thenReturn(new HashSet<>());
    when(userRelationshipQueryService.followingAuthors(eq(user.getId()), any()))
        .thenReturn(new HashSet<>());

    CursorPager<ArticleData> result = articleQueryService.findUserFeedWithCursor(user, page);

    assertEquals(1, result.getData().size());
  }

  @Test
  void should_find_user_feed_with_cursor_prev_direction() {
    CursorPageParameter<DateTime> page = new CursorPageParameter<>(null, 10, Direction.PREV);
    when(userRelationshipQueryService.followedUsers(user.getId()))
        .thenReturn(Arrays.asList("followed-id"));
    when(articleReadService.findArticlesOfAuthorsWithCursor(any(), any()))
        .thenReturn(new ArrayList<>(Arrays.asList(articleData)));
    when(articleFavoritesReadService.articlesFavoriteCount(any()))
        .thenReturn(Arrays.asList(new ArticleFavoriteCount("a1", 0)));
    when(articleFavoritesReadService.userFavorites(any(), eq(user))).thenReturn(new HashSet<>());
    when(userRelationshipQueryService.followingAuthors(eq(user.getId()), any()))
        .thenReturn(new HashSet<>());

    CursorPager<ArticleData> result = articleQueryService.findUserFeedWithCursor(user, page);

    assertEquals(1, result.getData().size());
  }
}
