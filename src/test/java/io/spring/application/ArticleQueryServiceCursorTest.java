package io.spring.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;

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
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class ArticleQueryServiceCursorTest {

  @Mock private ArticleReadService articleReadService;

  @Mock private UserRelationshipQueryService userRelationshipQueryService;

  @Mock private ArticleFavoritesReadService articleFavoritesReadService;

  @InjectMocks private ArticleQueryService articleQueryService;

  private User user;

  @BeforeEach
  public void setUp() {
    user = new User("john@jacob.com", "johnjacob", "123", "bio", "image");
  }

  private ArticleData articleData(String id) {
    return new ArticleData(
        id,
        "slug-" + id,
        "title",
        "desc",
        "body",
        false,
        0,
        new DateTime(),
        new DateTime(),
        Arrays.asList("java"),
        new ProfileData("author-id", "author", "bio", "image", false));
  }

  private void stubExtraInfo(String articleId) {
    when(articleFavoritesReadService.articlesFavoriteCount(anyList()))
        .thenReturn(Collections.singletonList(new ArticleFavoriteCount(articleId, 3)));
    when(articleFavoritesReadService.userFavorites(anyList(), eq(user)))
        .thenReturn(new HashSet<>(Collections.singletonList(articleId)));
    when(userRelationshipQueryService.followingAuthors(eq(user.getId()), anyList()))
        .thenReturn(new HashSet<>(Collections.singletonList("author-id")));
  }

  @Test
  public void should_return_empty_when_article_id_is_unknown() {
    when(articleReadService.findById(eq("unknown"))).thenReturn(null);

    assertFalse(articleQueryService.findById("unknown", user).isPresent());
  }

  @Test
  public void should_return_empty_when_slug_is_unknown() {
    when(articleReadService.findBySlug(eq("unknown"))).thenReturn(null);

    assertFalse(articleQueryService.findBySlug("unknown", user).isPresent());
  }

  @Test
  public void should_find_article_by_slug_without_current_user() {
    when(articleReadService.findBySlug(eq("slug-1"))).thenReturn(articleData("1"));

    Optional<ArticleData> optional = articleQueryService.findBySlug("slug-1", null);

    assertTrue(optional.isPresent());
    assertFalse(optional.get().isFavorited());
  }

  @Test
  public void should_return_empty_cursor_page_when_no_article_matches() {
    when(articleReadService.findArticlesWithCursor(isNull(), isNull(), isNull(), any()))
        .thenReturn(new ArrayList<>());

    CursorPager<ArticleData> pager =
        articleQueryService.findRecentArticlesWithCursor(
            null, null, null, new CursorPageParameter<>(null, 10, Direction.NEXT), user);

    assertTrue(pager.getData().isEmpty());
    assertFalse(pager.hasNext());
  }

  @Test
  public void should_trim_extra_article_and_report_next_page() {
    List<String> ids = new ArrayList<>(Arrays.asList("1", "2"));
    when(articleReadService.findArticlesWithCursor(isNull(), isNull(), isNull(), any()))
        .thenReturn(ids);
    when(articleReadService.findArticles(eq(Collections.singletonList("1"))))
        .thenReturn(new ArrayList<>(Collections.singletonList(articleData("1"))));
    stubExtraInfo("1");

    CursorPager<ArticleData> pager =
        articleQueryService.findRecentArticlesWithCursor(
            null, null, null, new CursorPageParameter<>(null, 1, Direction.NEXT), user);

    assertEquals(1, pager.getData().size());
    assertTrue(pager.hasNext());
    assertTrue(pager.getData().get(0).isFavorited());
    assertEquals(3, pager.getData().get(0).getFavoritesCount());
    assertTrue(pager.getData().get(0).getProfileData().isFollowing());
  }

  @Test
  public void should_reverse_articles_for_backward_page() {
    List<String> ids = new ArrayList<>(Arrays.asList("1", "2"));
    when(articleReadService.findArticlesWithCursor(isNull(), isNull(), isNull(), any()))
        .thenReturn(ids);
    when(articleReadService.findArticles(eq(Arrays.asList("2", "1"))))
        .thenReturn(new ArrayList<>(Arrays.asList(articleData("2"), articleData("1"))));
    when(articleFavoritesReadService.articlesFavoriteCount(anyList()))
        .thenReturn(
            Arrays.asList(new ArticleFavoriteCount("1", 1), new ArticleFavoriteCount("2", 2)));

    CursorPager<ArticleData> pager =
        articleQueryService.findRecentArticlesWithCursor(
            null, null, null, new CursorPageParameter<>(null, 10, Direction.PREV), null);

    assertEquals("2", pager.getData().get(0).getId());
    assertTrue(pager.hasPrevious() || !pager.hasNext());
  }

  @Test
  public void should_return_empty_feed_when_user_follows_nobody() {
    when(userRelationshipQueryService.followedUsers(eq(user.getId())))
        .thenReturn(new ArrayList<>());

    CursorPager<ArticleData> pager =
        articleQueryService.findUserFeedWithCursor(
            user, new CursorPageParameter<>(null, 10, Direction.NEXT));

    assertTrue(pager.getData().isEmpty());
  }

  @Test
  public void should_trim_extra_article_in_feed() {
    when(userRelationshipQueryService.followedUsers(eq(user.getId())))
        .thenReturn(new ArrayList<>(Collections.singletonList("author-id")));
    when(articleReadService.findArticlesOfAuthorsWithCursor(anyList(), any()))
        .thenReturn(new ArrayList<>(Arrays.asList(articleData("1"), articleData("2"))));
    stubExtraInfo("1");

    CursorPager<ArticleData> pager =
        articleQueryService.findUserFeedWithCursor(
            user, new CursorPageParameter<>(null, 1, Direction.NEXT));

    assertEquals(1, pager.getData().size());
    assertTrue(pager.hasNext());
  }

  @Test
  public void should_reverse_feed_for_backward_page() {
    when(userRelationshipQueryService.followedUsers(eq(user.getId())))
        .thenReturn(new ArrayList<>(Collections.singletonList("author-id")));
    when(articleReadService.findArticlesOfAuthorsWithCursor(anyList(), any()))
        .thenReturn(new ArrayList<>(Arrays.asList(articleData("1"), articleData("2"))));
    when(articleFavoritesReadService.articlesFavoriteCount(anyList()))
        .thenReturn(
            Arrays.asList(new ArticleFavoriteCount("1", 1), new ArticleFavoriteCount("2", 2)));
    when(articleFavoritesReadService.userFavorites(anyList(), eq(user)))
        .thenReturn(new HashSet<>());
    when(userRelationshipQueryService.followingAuthors(eq(user.getId()), anyList()))
        .thenReturn(new HashSet<>());

    CursorPager<ArticleData> pager =
        articleQueryService.findUserFeedWithCursor(
            user, new CursorPageParameter<>(null, 10, Direction.PREV));

    assertEquals("2", pager.getData().get(0).getId());
  }
}
