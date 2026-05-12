package io.spring.application;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

import io.spring.application.CursorPager.Direction;
import io.spring.application.data.ArticleData;
import io.spring.application.data.ArticleDataList;
import io.spring.application.data.ArticleFavoriteCount;
import io.spring.application.data.ProfileData;
import io.spring.core.user.User;
import io.spring.infrastructure.mybatis.readservice.ArticleFavoritesReadService;
import io.spring.infrastructure.mybatis.readservice.ArticleReadService;
import io.spring.infrastructure.mybatis.readservice.UserRelationshipQueryService;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import org.joda.time.DateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class ArticleQueryServiceTest {

  @Mock private ArticleReadService articleReadService;
  @Mock private UserRelationshipQueryService userRelationshipQueryService;
  @Mock private ArticleFavoritesReadService articleFavoritesReadService;

  private ArticleQueryService articleQueryService;
  private User user;

  @BeforeEach
  public void setUp() {
    articleQueryService =
        new ArticleQueryService(
            articleReadService, userRelationshipQueryService, articleFavoritesReadService);
    user = new User("test@test.com", "testuser", "password", "bio", "image");
  }

  private ArticleData createArticleData(String id) {
    return new ArticleData(
        id,
        "slug-" + id,
        "Title",
        "Desc",
        "Body",
        false,
        0,
        new DateTime(),
        new DateTime(),
        Collections.emptyList(),
        new ProfileData("author-id", "author", "bio", "image", false));
  }

  @Test
  public void should_find_by_id_with_user() {
    ArticleData articleData = createArticleData("article-1");
    when(articleReadService.findById(eq("article-1"))).thenReturn(articleData);
    when(articleFavoritesReadService.isUserFavorite(eq(user.getId()), eq("article-1")))
        .thenReturn(true);
    when(articleFavoritesReadService.articleFavoriteCount(eq("article-1"))).thenReturn(5);
    when(userRelationshipQueryService.isUserFollowing(eq(user.getId()), eq("author-id")))
        .thenReturn(false);

    Optional<ArticleData> result = articleQueryService.findById("article-1", user);

    assertTrue(result.isPresent());
    assertTrue(result.get().isFavorited());
    assertEquals(5, result.get().getFavoritesCount());
  }

  @Test
  public void should_find_by_id_without_user() {
    ArticleData articleData = createArticleData("article-1");
    when(articleReadService.findById(eq("article-1"))).thenReturn(articleData);

    Optional<ArticleData> result = articleQueryService.findById("article-1", null);

    assertTrue(result.isPresent());
  }

  @Test
  public void should_return_empty_when_article_not_found_by_id() {
    when(articleReadService.findById(eq("missing"))).thenReturn(null);

    Optional<ArticleData> result = articleQueryService.findById("missing", user);

    assertFalse(result.isPresent());
  }

  @Test
  public void should_find_by_slug_with_user() {
    ArticleData articleData = createArticleData("article-1");
    when(articleReadService.findBySlug(eq("test-slug"))).thenReturn(articleData);
    when(articleFavoritesReadService.isUserFavorite(eq(user.getId()), eq("article-1")))
        .thenReturn(false);
    when(articleFavoritesReadService.articleFavoriteCount(eq("article-1"))).thenReturn(0);
    when(userRelationshipQueryService.isUserFollowing(eq(user.getId()), eq("author-id")))
        .thenReturn(true);

    Optional<ArticleData> result = articleQueryService.findBySlug("test-slug", user);

    assertTrue(result.isPresent());
  }

  @Test
  public void should_return_empty_when_article_not_found_by_slug() {
    when(articleReadService.findBySlug(eq("missing"))).thenReturn(null);

    Optional<ArticleData> result = articleQueryService.findBySlug("missing", null);

    assertFalse(result.isPresent());
  }

  @Test
  public void should_find_recent_articles_with_cursor_empty() {
    CursorPageParameter<DateTime> page = new CursorPageParameter<>(null, 10, Direction.NEXT);
    when(articleReadService.findArticlesWithCursor(any(), any(), any(), any()))
        .thenReturn(Collections.emptyList());

    CursorPager<ArticleData> result =
        articleQueryService.findRecentArticlesWithCursor(null, null, null, page, user);

    assertTrue(result.getData().isEmpty());
  }

  @Test
  public void should_find_recent_articles_with_cursor_with_results() {
    CursorPageParameter<DateTime> page = new CursorPageParameter<>(null, 10, Direction.NEXT);
    when(articleReadService.findArticlesWithCursor(any(), any(), any(), any()))
        .thenReturn(Arrays.asList("id1"));
    ArticleData articleData = createArticleData("id1");
    when(articleReadService.findArticles(anyList())).thenReturn(Arrays.asList(articleData));
    when(articleFavoritesReadService.articlesFavoriteCount(anyList()))
        .thenReturn(Arrays.asList(new ArticleFavoriteCount("id1", 3)));
    when(articleFavoritesReadService.userFavorites(anyList(), eq(user)))
        .thenReturn(new HashSet<>(Arrays.asList("id1")));
    when(userRelationshipQueryService.followingAuthors(eq(user.getId()), anyList()))
        .thenReturn(Collections.emptySet());

    CursorPager<ArticleData> result =
        articleQueryService.findRecentArticlesWithCursor(null, null, null, page, user);

    assertFalse(result.getData().isEmpty());
  }

  @Test
  public void should_find_recent_articles_with_cursor_prev_direction() {
    CursorPageParameter<DateTime> page = new CursorPageParameter<>(null, 10, Direction.PREV);
    when(articleReadService.findArticlesWithCursor(any(), any(), any(), any()))
        .thenReturn(Arrays.asList("id1"));
    ArticleData articleData = createArticleData("id1");
    when(articleReadService.findArticles(anyList())).thenReturn(Arrays.asList(articleData));
    when(articleFavoritesReadService.articlesFavoriteCount(anyList()))
        .thenReturn(Arrays.asList(new ArticleFavoriteCount("id1", 0)));

    CursorPager<ArticleData> result =
        articleQueryService.findRecentArticlesWithCursor(null, null, null, page, null);

    assertFalse(result.getData().isEmpty());
  }

  @Test
  public void should_find_user_feed_with_cursor_empty_followers() {
    CursorPageParameter<DateTime> page = new CursorPageParameter<>(null, 10, Direction.NEXT);
    when(userRelationshipQueryService.followedUsers(eq(user.getId())))
        .thenReturn(Collections.emptyList());

    CursorPager<ArticleData> result = articleQueryService.findUserFeedWithCursor(user, page);

    assertTrue(result.getData().isEmpty());
  }

  @Test
  public void should_find_user_feed_with_cursor_with_results() {
    CursorPageParameter<DateTime> page = new CursorPageParameter<>(null, 10, Direction.NEXT);
    when(userRelationshipQueryService.followedUsers(eq(user.getId())))
        .thenReturn(Arrays.asList("followed-user-id"));
    ArticleData articleData = createArticleData("id1");
    when(articleReadService.findArticlesOfAuthorsWithCursor(anyList(), any()))
        .thenReturn(Arrays.asList(articleData));
    when(articleFavoritesReadService.articlesFavoriteCount(anyList()))
        .thenReturn(Arrays.asList(new ArticleFavoriteCount("id1", 0)));
    when(articleFavoritesReadService.userFavorites(anyList(), eq(user)))
        .thenReturn(Collections.emptySet());
    when(userRelationshipQueryService.followingAuthors(eq(user.getId()), anyList()))
        .thenReturn(Collections.emptySet());

    CursorPager<ArticleData> result = articleQueryService.findUserFeedWithCursor(user, page);

    assertFalse(result.getData().isEmpty());
  }

  @Test
  public void should_find_recent_articles_empty() {
    Page page = new Page();
    when(articleReadService.queryArticles(any(), any(), any(), any()))
        .thenReturn(Collections.emptyList());
    when(articleReadService.countArticle(any(), any(), any())).thenReturn(0);

    ArticleDataList result = articleQueryService.findRecentArticles(null, null, null, page, user);

    assertTrue(result.getArticleDatas().isEmpty());
    assertEquals(0, result.getCount());
  }

  @Test
  public void should_find_recent_articles_with_results() {
    Page page = new Page();
    when(articleReadService.queryArticles(any(), any(), any(), any()))
        .thenReturn(Arrays.asList("id1"));
    when(articleReadService.countArticle(any(), any(), any())).thenReturn(1);
    ArticleData articleData = createArticleData("id1");
    when(articleReadService.findArticles(anyList())).thenReturn(Arrays.asList(articleData));
    when(articleFavoritesReadService.articlesFavoriteCount(anyList()))
        .thenReturn(Arrays.asList(new ArticleFavoriteCount("id1", 3)));
    when(articleFavoritesReadService.userFavorites(anyList(), eq(user)))
        .thenReturn(Collections.emptySet());
    when(userRelationshipQueryService.followingAuthors(eq(user.getId()), anyList()))
        .thenReturn(Collections.emptySet());

    ArticleDataList result = articleQueryService.findRecentArticles(null, null, null, page, user);

    assertFalse(result.getArticleDatas().isEmpty());
    assertEquals(1, result.getCount());
  }

  @Test
  public void should_find_user_feed_empty_followers() {
    Page page = new Page();
    when(userRelationshipQueryService.followedUsers(eq(user.getId())))
        .thenReturn(Collections.emptyList());

    ArticleDataList result = articleQueryService.findUserFeed(user, page);

    assertTrue(result.getArticleDatas().isEmpty());
    assertEquals(0, result.getCount());
  }

  @Test
  public void should_find_user_feed_with_results() {
    Page page = new Page();
    when(userRelationshipQueryService.followedUsers(eq(user.getId())))
        .thenReturn(Arrays.asList("followed-user-id"));
    ArticleData articleData = createArticleData("id1");
    when(articleReadService.findArticlesOfAuthors(anyList(), any()))
        .thenReturn(Arrays.asList(articleData));
    when(articleReadService.countFeedSize(anyList())).thenReturn(1);
    when(articleFavoritesReadService.articlesFavoriteCount(anyList()))
        .thenReturn(Arrays.asList(new ArticleFavoriteCount("id1", 0)));
    when(articleFavoritesReadService.userFavorites(anyList(), eq(user)))
        .thenReturn(Collections.emptySet());
    when(userRelationshipQueryService.followingAuthors(eq(user.getId()), anyList()))
        .thenReturn(Collections.emptySet());

    ArticleDataList result = articleQueryService.findUserFeed(user, page);

    assertFalse(result.getArticleDatas().isEmpty());
    assertEquals(1, result.getCount());
  }
}
