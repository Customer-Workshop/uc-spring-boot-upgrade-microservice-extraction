package io.spring.application.article;

import static io.spring.TestHelper.articleDataFixture;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptySet;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.spring.application.ArticleQueryService;
import io.spring.application.CursorPageParameter;
import io.spring.application.CursorPager;
import io.spring.application.CursorPager.Direction;
import io.spring.application.data.ArticleData;
import io.spring.application.data.ArticleFavoriteCount;
import io.spring.core.user.User;
import io.spring.infrastructure.mybatis.readservice.ArticleFavoritesReadService;
import io.spring.infrastructure.mybatis.readservice.ArticleReadService;
import io.spring.infrastructure.mybatis.readservice.UserRelationshipQueryService;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.joda.time.DateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

/**
 * Cursor pagination is driven by list sizes and ordering that are hard to force through the
 * database, so the read services are stubbed here.
 */
public class ArticleQueryServiceCursorTest {

  private ArticleReadService articleReadService;
  private UserRelationshipQueryService userRelationshipQueryService;
  private ArticleFavoritesReadService articleFavoritesReadService;
  private ArticleQueryService queryService;
  private User user;

  @BeforeEach
  public void setUp() {
    articleReadService = Mockito.mock(ArticleReadService.class);
    userRelationshipQueryService = Mockito.mock(UserRelationshipQueryService.class);
    articleFavoritesReadService = Mockito.mock(ArticleFavoritesReadService.class);
    queryService =
        new ArticleQueryService(
            articleReadService, userRelationshipQueryService, articleFavoritesReadService);
    user = new User("test@test.com", "test", "123", "", "");

    when(articleFavoritesReadService.articlesFavoriteCount(any()))
        .thenAnswer(
            invocation -> {
              List<String> ids = invocation.getArgument(0);
              return ids.stream()
                  .map(id -> new ArticleFavoriteCount(id, 0))
                  .collect(Collectors.toList());
            });
    when(articleFavoritesReadService.userFavorites(any(), any())).thenReturn(emptySet());
    when(userRelationshipQueryService.followingAuthors(anyString(), any())).thenReturn(emptySet());
  }

  @Test
  public void should_drop_extra_article_id_and_keep_order_when_paging_by_cursor() {
    when(articleReadService.findArticlesWithCursor(eq(null), eq(null), eq(null), any()))
        .thenReturn(new ArrayList<>(asList("1id", "2id", "3id")));
    when(articleReadService.findArticles(any()))
        .thenReturn(asList(articleDataFixture("1", user), articleDataFixture("2", user)));

    CursorPager<ArticleData> page =
        queryService.findRecentArticlesWithCursor(
            null, null, null, new CursorPageParameter<>(null, 2, Direction.NEXT), user);

    assertThat(page.getData().size(), is(2));
    assertThat(page.hasNext(), is(true));
    assertThat(articleIdsPassedToFindArticles(), is(asList("1id", "2id")));
  }

  @Test
  public void should_reverse_article_ids_when_paging_backwards_by_cursor() {
    when(articleReadService.findArticlesWithCursor(eq(null), eq(null), eq(null), any()))
        .thenReturn(new ArrayList<>(asList("1id", "2id", "3id")));
    when(articleReadService.findArticles(any()))
        .thenReturn(asList(articleDataFixture("2", user), articleDataFixture("1", user)));

    CursorPager<ArticleData> page =
        queryService.findRecentArticlesWithCursor(
            null, null, null, new CursorPageParameter<>(null, 2, Direction.PREV), user);

    assertThat(page.hasPrevious(), is(true));
    assertThat(articleIdsPassedToFindArticles(), is(asList("2id", "1id")));
  }

  @Test
  public void should_not_flag_next_page_when_article_ids_fit_the_limit() {
    when(articleReadService.findArticlesWithCursor(eq(null), eq(null), eq(null), any()))
        .thenReturn(new ArrayList<>(asList("1id", "2id")));
    when(articleReadService.findArticles(any()))
        .thenReturn(asList(articleDataFixture("1", user), articleDataFixture("2", user)));

    CursorPager<ArticleData> page =
        queryService.findRecentArticlesWithCursor(
            null, null, null, new CursorPageParameter<>(null, 2, Direction.NEXT), user);

    assertThat(page.hasNext(), is(false));
    assertThat(articleIdsPassedToFindArticles(), is(asList("1id", "2id")));
  }

  @Test
  public void should_drop_extra_feed_article_and_keep_order() {
    when(userRelationshipQueryService.followedUsers(user.getId())).thenReturn(asList("authorId"));
    when(articleReadService.findArticlesOfAuthorsWithCursor(any(), any()))
        .thenReturn(
            new ArrayList<>(
                asList(
                    articleDataFixture("1", user),
                    articleDataFixture("2", user),
                    articleDataFixture("3", user))));

    CursorPager<ArticleData> feed =
        queryService.findUserFeedWithCursor(
            user, new CursorPageParameter<>(null, 2, Direction.NEXT));

    assertThat(feed.hasNext(), is(true));
    assertThat(idsOf(feed), is(asList("1id", "2id")));
  }

  @Test
  public void should_reverse_feed_articles_when_paging_backwards() {
    when(userRelationshipQueryService.followedUsers(user.getId())).thenReturn(asList("authorId"));
    when(articleReadService.findArticlesOfAuthorsWithCursor(any(), any()))
        .thenReturn(
            new ArrayList<>(
                asList(
                    articleDataFixture("1", user),
                    articleDataFixture("2", user),
                    articleDataFixture("3", user))));

    CursorPager<ArticleData> feed =
        queryService.findUserFeedWithCursor(
            user, new CursorPageParameter<>(null, 2, Direction.PREV));

    assertThat(feed.hasPrevious(), is(true));
    assertThat(idsOf(feed), is(asList("2id", "1id")));
  }

  @Test
  public void should_not_flag_next_page_when_feed_articles_fit_the_limit() {
    when(userRelationshipQueryService.followedUsers(user.getId())).thenReturn(asList("authorId"));
    when(articleReadService.findArticlesOfAuthorsWithCursor(any(), any()))
        .thenReturn(
            new ArrayList<>(asList(articleDataFixture("1", user), articleDataFixture("2", user))));

    CursorPager<ArticleData> feed =
        queryService.findUserFeedWithCursor(
            user, new CursorPageParameter<>(null, 2, Direction.NEXT));

    assertThat(feed.hasNext(), is(false));
    assertThat(idsOf(feed), is(asList("1id", "2id")));
  }

  @Test
  public void should_return_empty_pager_when_user_follows_nobody() {
    when(userRelationshipQueryService.followedUsers(user.getId())).thenReturn(emptyList());

    CursorPager<ArticleData> feed =
        queryService.findUserFeedWithCursor(
            user, new CursorPageParameter<>(new DateTime(), 2, Direction.NEXT));

    assertThat(feed.getData().isEmpty(), is(true));
    assertThat(feed.hasNext(), is(false));
    verify(articleReadService, Mockito.never()).findArticlesOfAuthorsWithCursor(any(), any());
  }

  private List<String> idsOf(CursorPager<ArticleData> pager) {
    return pager.getData().stream().map(ArticleData::getId).collect(Collectors.toList());
  }

  @SuppressWarnings("unchecked")
  private List<String> articleIdsPassedToFindArticles() {
    ArgumentCaptor<List<String>> captor = ArgumentCaptor.forClass(List.class);
    verify(articleReadService).findArticles(captor.capture());
    return captor.getValue();
  }
}
