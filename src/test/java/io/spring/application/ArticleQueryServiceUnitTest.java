package io.spring.application;

import static java.util.Collections.emptyList;
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
import java.util.*;
import org.joda.time.DateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class ArticleQueryServiceUnitTest {

  @Mock private ArticleReadService articleReadService;
  @Mock private UserRelationshipQueryService userRelationshipQueryService;
  @Mock private ArticleFavoritesReadService articleFavoritesReadService;

  private ArticleQueryService service;
  private User user;

  @BeforeEach
  void setUp() {
    service =
        new ArticleQueryService(
            articleReadService, userRelationshipQueryService, articleFavoritesReadService);
    user = new User("test@test.com", "testuser", "password", "bio", "image");
  }

  @Test
  void should_find_by_slug_with_user() {
    ArticleData articleData = createArticleData("a1", "test-slug");
    when(articleReadService.findBySlug("test-slug")).thenReturn(articleData);
    when(articleFavoritesReadService.isUserFavorite(user.getId(), "a1")).thenReturn(true);
    when(articleFavoritesReadService.articleFavoriteCount("a1")).thenReturn(5);
    when(userRelationshipQueryService.isUserFollowing(user.getId(), "userId")).thenReturn(false);

    Optional<ArticleData> result = service.findBySlug("test-slug", user);

    assertTrue(result.isPresent());
    assertTrue(result.get().isFavorited());
    assertEquals(5, result.get().getFavoritesCount());
  }

  @Test
  void should_find_by_slug_without_user() {
    ArticleData articleData = createArticleData("a1", "test-slug");
    when(articleReadService.findBySlug("test-slug")).thenReturn(articleData);

    Optional<ArticleData> result = service.findBySlug("test-slug", null);

    assertTrue(result.isPresent());
  }

  @Test
  void should_return_empty_when_slug_not_found() {
    when(articleReadService.findBySlug("missing")).thenReturn(null);

    Optional<ArticleData> result = service.findBySlug("missing", user);

    assertTrue(result.isEmpty());
  }

  @Test
  void should_find_user_feed_with_cursor_empty_followers() {
    when(userRelationshipQueryService.followedUsers(user.getId())).thenReturn(emptyList());

    CursorPager<ArticleData> result =
        service.findUserFeedWithCursor(user, new CursorPageParameter<>(null, 10, Direction.NEXT));

    assertTrue(result.getData().isEmpty());
  }

  @Test
  void should_find_user_feed_with_cursor_with_followers() {
    when(userRelationshipQueryService.followedUsers(user.getId()))
        .thenReturn(List.of("followedUser1"));
    ArticleData a1 = createArticleData("a1", "slug-1");
    when(articleReadService.findArticlesOfAuthorsWithCursor(eq(List.of("followedUser1")), any()))
        .thenReturn(new ArrayList<>(List.of(a1)));
    when(articleFavoritesReadService.articlesFavoriteCount(any()))
        .thenReturn(List.of(new ArticleFavoriteCount("a1", 3)));
    when(articleFavoritesReadService.userFavorites(any(), eq(user))).thenReturn(Set.of("a1"));
    when(userRelationshipQueryService.followingAuthors(eq(user.getId()), any()))
        .thenReturn(Set.of("userId"));

    CursorPager<ArticleData> result =
        service.findUserFeedWithCursor(user, new CursorPageParameter<>(null, 10, Direction.NEXT));

    assertEquals(1, result.getData().size());
    assertTrue(result.getData().get(0).isFavorited());
    assertTrue(result.getData().get(0).getProfileData().isFollowing());
  }

  @Test
  void should_find_user_feed_with_cursor_has_extra_prev() {
    when(userRelationshipQueryService.followedUsers(user.getId()))
        .thenReturn(List.of("followedUser1"));
    ArticleData a1 = createArticleData("a1", "slug-1");
    ArticleData a2 = createArticleData("a2", "slug-2");
    when(articleReadService.findArticlesOfAuthorsWithCursor(any(), any()))
        .thenReturn(new ArrayList<>(List.of(a1, a2)));
    when(articleFavoritesReadService.articlesFavoriteCount(any()))
        .thenReturn(List.of(new ArticleFavoriteCount("a1", 0), new ArticleFavoriteCount("a2", 0)));
    when(articleFavoritesReadService.userFavorites(any(), eq(user))).thenReturn(Set.of());
    when(userRelationshipQueryService.followingAuthors(eq(user.getId()), any()))
        .thenReturn(Set.of());

    CursorPager<ArticleData> result =
        service.findUserFeedWithCursor(user, new CursorPageParameter<>(null, 1, Direction.PREV));

    assertEquals(1, result.getData().size());
    assertTrue(result.hasPrevious());
  }

  @Test
  void should_find_recent_articles_empty() {
    when(articleReadService.queryArticles(any(), any(), any(), any())).thenReturn(emptyList());
    when(articleReadService.countArticle(any(), any(), any())).thenReturn(0);

    ArticleDataList result = service.findRecentArticles(null, null, null, new Page(), user);

    assertEquals(0, result.getCount());
    assertTrue(result.getArticleDatas().isEmpty());
  }

  @Test
  void should_find_user_feed_empty_followers() {
    when(userRelationshipQueryService.followedUsers(user.getId())).thenReturn(emptyList());

    ArticleDataList result = service.findUserFeed(user, new Page());

    assertEquals(0, result.getCount());
    assertTrue(result.getArticleDatas().isEmpty());
  }

  @Test
  void should_find_user_feed_with_followers() {
    when(userRelationshipQueryService.followedUsers(user.getId()))
        .thenReturn(List.of("followedUser1"));
    ArticleData a1 = createArticleData("a1", "slug-1");
    when(articleReadService.findArticlesOfAuthors(eq(List.of("followedUser1")), any()))
        .thenReturn(List.of(a1));
    when(articleReadService.countFeedSize(List.of("followedUser1"))).thenReturn(1);
    when(articleFavoritesReadService.articlesFavoriteCount(any()))
        .thenReturn(List.of(new ArticleFavoriteCount("a1", 2)));
    when(articleFavoritesReadService.userFavorites(any(), eq(user))).thenReturn(Set.of("a1"));
    when(userRelationshipQueryService.followingAuthors(eq(user.getId()), any()))
        .thenReturn(Set.of("userId"));

    ArticleDataList result = service.findUserFeed(user, new Page());

    assertEquals(1, result.getCount());
    assertEquals(1, result.getArticleDatas().size());
    assertTrue(result.getArticleDatas().get(0).isFavorited());
  }

  @Test
  void should_find_recent_articles_with_cursor_empty() {
    when(articleReadService.findArticlesWithCursor(any(), any(), any(), any()))
        .thenReturn(emptyList());

    CursorPager<ArticleData> result =
        service.findRecentArticlesWithCursor(
            null, null, null, new CursorPageParameter<>(null, 10, Direction.NEXT), user);

    assertTrue(result.getData().isEmpty());
  }

  @Test
  void should_find_recent_articles_with_cursor_has_extra() {
    when(articleReadService.findArticlesWithCursor(any(), any(), any(), any()))
        .thenReturn(new ArrayList<>(List.of("a1", "a2", "a3")));
    ArticleData ad1 = createArticleData("a1", "slug-1");
    ArticleData ad2 = createArticleData("a2", "slug-2");
    when(articleReadService.findArticles(List.of("a1", "a2"))).thenReturn(List.of(ad1, ad2));
    when(articleFavoritesReadService.articlesFavoriteCount(any()))
        .thenReturn(List.of(new ArticleFavoriteCount("a1", 0), new ArticleFavoriteCount("a2", 0)));

    CursorPager<ArticleData> result =
        service.findRecentArticlesWithCursor(
            "tag", "author", "fav", new CursorPageParameter<>(null, 2, Direction.NEXT), null);

    assertEquals(2, result.getData().size());
    assertTrue(result.hasNext());
  }

  @Test
  void should_find_recent_articles_with_cursor_prev_direction() {
    when(articleReadService.findArticlesWithCursor(any(), any(), any(), any()))
        .thenReturn(new ArrayList<>(List.of("a1")));
    ArticleData ad1 = createArticleData("a1", "slug-1");
    when(articleReadService.findArticles(List.of("a1"))).thenReturn(List.of(ad1));
    when(articleFavoritesReadService.articlesFavoriteCount(any()))
        .thenReturn(List.of(new ArticleFavoriteCount("a1", 0)));

    CursorPager<ArticleData> result =
        service.findRecentArticlesWithCursor(
            null, null, null, new CursorPageParameter<>(null, 10, Direction.PREV), null);

    assertEquals(1, result.getData().size());
  }

  private ArticleData createArticleData(String id, String slug) {
    return new ArticleData(
        id,
        slug,
        "title",
        "desc",
        "body",
        false,
        0,
        new DateTime(),
        new DateTime(),
        List.of("tag"),
        new ProfileData("userId", "testuser", "bio", "image", false));
  }
}
