package io.spring.graphql;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import com.netflix.graphql.dgs.DgsDataFetchingEnvironment;
import graphql.execution.DataFetcherResult;
import graphql.schema.DataFetchingEnvironment;
import io.spring.TestHelper;
import io.spring.api.exception.ResourceNotFoundException;
import io.spring.application.ArticleQueryService;
import io.spring.application.CursorPager;
import io.spring.application.data.ArticleData;
import io.spring.application.data.CommentData;
import io.spring.application.data.ProfileData;
import io.spring.core.article.Article;
import io.spring.core.user.User;
import io.spring.core.user.UserRepository;
import io.spring.graphql.types.ArticlesConnection;
import io.spring.graphql.types.Profile;
import java.util.Arrays;
import java.util.Optional;
import org.joda.time.DateTime;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

public class ArticleDatafetcherTest {

  private ArticleQueryService articleQueryService;
  private UserRepository userRepository;
  private ArticleDatafetcher articleDatafetcher;
  private User user;

  @BeforeEach
  public void setUp() {
    articleQueryService = Mockito.mock(ArticleQueryService.class);
    userRepository = Mockito.mock(UserRepository.class);
    articleDatafetcher = new ArticleDatafetcher(articleQueryService, userRepository);
    user = new User("test@test.com", "testuser", "123", "", "");
    SecurityContextHolder.getContext()
        .setAuthentication(new TestingAuthenticationToken(user, null));
  }

  @AfterEach
  public void tearDown() {
    SecurityContextHolder.clearContext();
  }

  @Test
  public void should_get_feed_with_first() {
    ArticleData articleData = TestHelper.articleDataFixture("1", user);
    CursorPager<ArticleData> pager =
        new CursorPager<>(Arrays.asList(articleData), CursorPager.Direction.NEXT, false);
    when(articleQueryService.findUserFeedWithCursor(eq(user), any())).thenReturn(pager);

    DgsDataFetchingEnvironment dfe = Mockito.mock(DgsDataFetchingEnvironment.class);
    DataFetcherResult<ArticlesConnection> result =
        articleDatafetcher.getFeed(10, null, null, null, dfe);
    assertNotNull(result);
    assertEquals(1, result.getData().getEdges().size());
  }

  @Test
  public void should_get_feed_with_last() {
    ArticleData articleData = TestHelper.articleDataFixture("1", user);
    CursorPager<ArticleData> pager =
        new CursorPager<>(Arrays.asList(articleData), CursorPager.Direction.PREV, false);
    when(articleQueryService.findUserFeedWithCursor(eq(user), any())).thenReturn(pager);

    DgsDataFetchingEnvironment dfe = Mockito.mock(DgsDataFetchingEnvironment.class);
    DataFetcherResult<ArticlesConnection> result =
        articleDatafetcher.getFeed(null, null, 10, null, dfe);
    assertNotNull(result);
    assertEquals(1, result.getData().getEdges().size());
  }

  @Test
  public void should_throw_when_first_and_last_both_null_on_feed() {
    DgsDataFetchingEnvironment dfe = Mockito.mock(DgsDataFetchingEnvironment.class);
    assertThrows(
        IllegalArgumentException.class,
        () -> articleDatafetcher.getFeed(null, null, null, null, dfe));
  }

  @Test
  public void should_get_user_feed() {
    User target = new User("target@test.com", "targetuser", "123", "", "");
    when(userRepository.findByUsername(eq("targetuser"))).thenReturn(Optional.of(target));
    ArticleData articleData = TestHelper.articleDataFixture("1", target);
    CursorPager<ArticleData> pager =
        new CursorPager<>(Arrays.asList(articleData), CursorPager.Direction.NEXT, false);
    when(articleQueryService.findUserFeedWithCursor(eq(target), any())).thenReturn(pager);

    DgsDataFetchingEnvironment dfe = Mockito.mock(DgsDataFetchingEnvironment.class);
    Profile profile = Profile.newBuilder().username("targetuser").build();
    when(dfe.getSource()).thenReturn(profile);

    DataFetcherResult<ArticlesConnection> result =
        articleDatafetcher.userFeed(10, null, null, null, dfe);
    assertNotNull(result);
    assertEquals(1, result.getData().getEdges().size());
  }

  @Test
  public void should_get_user_feed_with_last() {
    User target = new User("target@test.com", "targetuser", "123", "", "");
    when(userRepository.findByUsername(eq("targetuser"))).thenReturn(Optional.of(target));
    ArticleData articleData = TestHelper.articleDataFixture("1", target);
    CursorPager<ArticleData> pager =
        new CursorPager<>(Arrays.asList(articleData), CursorPager.Direction.PREV, false);
    when(articleQueryService.findUserFeedWithCursor(eq(target), any())).thenReturn(pager);

    DgsDataFetchingEnvironment dfe = Mockito.mock(DgsDataFetchingEnvironment.class);
    Profile profile = Profile.newBuilder().username("targetuser").build();
    when(dfe.getSource()).thenReturn(profile);

    DataFetcherResult<ArticlesConnection> result =
        articleDatafetcher.userFeed(null, null, 10, null, dfe);
    assertNotNull(result);
  }

  @Test
  public void should_throw_when_first_and_last_both_null_on_user_feed() {
    DgsDataFetchingEnvironment dfe = Mockito.mock(DgsDataFetchingEnvironment.class);
    Profile profile = Profile.newBuilder().username("targetuser").build();
    when(dfe.getSource()).thenReturn(profile);
    assertThrows(
        IllegalArgumentException.class,
        () -> articleDatafetcher.userFeed(null, null, null, null, dfe));
  }

  @Test
  public void should_get_user_favorites_with_first() {
    ArticleData articleData = TestHelper.articleDataFixture("1", user);
    CursorPager<ArticleData> pager =
        new CursorPager<>(Arrays.asList(articleData), CursorPager.Direction.NEXT, false);
    when(articleQueryService.findRecentArticlesWithCursor(any(), any(), any(), any(), any()))
        .thenReturn(pager);

    DgsDataFetchingEnvironment dfe = Mockito.mock(DgsDataFetchingEnvironment.class);
    Profile profile = Profile.newBuilder().username("testuser").build();
    when(dfe.getSource()).thenReturn(profile);

    DataFetcherResult<ArticlesConnection> result =
        articleDatafetcher.userFavorites(10, null, null, null, dfe);
    assertNotNull(result);
    assertEquals(1, result.getData().getEdges().size());
  }

  @Test
  public void should_get_user_favorites_with_last() {
    ArticleData articleData = TestHelper.articleDataFixture("1", user);
    CursorPager<ArticleData> pager =
        new CursorPager<>(Arrays.asList(articleData), CursorPager.Direction.PREV, false);
    when(articleQueryService.findRecentArticlesWithCursor(any(), any(), any(), any(), any()))
        .thenReturn(pager);

    DgsDataFetchingEnvironment dfe = Mockito.mock(DgsDataFetchingEnvironment.class);
    Profile profile = Profile.newBuilder().username("testuser").build();
    when(dfe.getSource()).thenReturn(profile);

    DataFetcherResult<ArticlesConnection> result =
        articleDatafetcher.userFavorites(null, null, 10, null, dfe);
    assertNotNull(result);
  }

  @Test
  public void should_throw_when_first_and_last_both_null_on_user_favorites() {
    DgsDataFetchingEnvironment dfe = Mockito.mock(DgsDataFetchingEnvironment.class);
    Profile profile = Profile.newBuilder().username("testuser").build();
    when(dfe.getSource()).thenReturn(profile);
    assertThrows(
        IllegalArgumentException.class,
        () -> articleDatafetcher.userFavorites(null, null, null, null, dfe));
  }

  @Test
  public void should_get_user_articles_with_first() {
    ArticleData articleData = TestHelper.articleDataFixture("1", user);
    CursorPager<ArticleData> pager =
        new CursorPager<>(Arrays.asList(articleData), CursorPager.Direction.NEXT, false);
    when(articleQueryService.findRecentArticlesWithCursor(any(), any(), any(), any(), any()))
        .thenReturn(pager);

    DgsDataFetchingEnvironment dfe = Mockito.mock(DgsDataFetchingEnvironment.class);
    Profile profile = Profile.newBuilder().username("testuser").build();
    when(dfe.getSource()).thenReturn(profile);

    DataFetcherResult<ArticlesConnection> result =
        articleDatafetcher.userArticles(10, null, null, null, dfe);
    assertNotNull(result);
  }

  @Test
  public void should_get_user_articles_with_last() {
    ArticleData articleData = TestHelper.articleDataFixture("1", user);
    CursorPager<ArticleData> pager =
        new CursorPager<>(Arrays.asList(articleData), CursorPager.Direction.PREV, false);
    when(articleQueryService.findRecentArticlesWithCursor(any(), any(), any(), any(), any()))
        .thenReturn(pager);

    DgsDataFetchingEnvironment dfe = Mockito.mock(DgsDataFetchingEnvironment.class);
    Profile profile = Profile.newBuilder().username("testuser").build();
    when(dfe.getSource()).thenReturn(profile);

    DataFetcherResult<ArticlesConnection> result =
        articleDatafetcher.userArticles(null, null, 10, null, dfe);
    assertNotNull(result);
  }

  @Test
  public void should_throw_when_first_and_last_both_null_on_user_articles() {
    DgsDataFetchingEnvironment dfe = Mockito.mock(DgsDataFetchingEnvironment.class);
    Profile profile = Profile.newBuilder().username("testuser").build();
    when(dfe.getSource()).thenReturn(profile);
    assertThrows(
        IllegalArgumentException.class,
        () -> articleDatafetcher.userArticles(null, null, null, null, dfe));
  }

  @Test
  public void should_get_articles_with_first() {
    ArticleData articleData = TestHelper.articleDataFixture("1", user);
    CursorPager<ArticleData> pager =
        new CursorPager<>(Arrays.asList(articleData), CursorPager.Direction.NEXT, false);
    when(articleQueryService.findRecentArticlesWithCursor(any(), any(), any(), any(), any()))
        .thenReturn(pager);
    DgsDataFetchingEnvironment dfe = Mockito.mock(DgsDataFetchingEnvironment.class);

    DataFetcherResult<ArticlesConnection> result =
        articleDatafetcher.getArticles(10, null, null, null, "author", "fav", "tag", dfe);
    assertNotNull(result);
    assertEquals(1, result.getData().getEdges().size());
  }

  @Test
  public void should_get_articles_with_last() {
    ArticleData articleData = TestHelper.articleDataFixture("1", user);
    CursorPager<ArticleData> pager =
        new CursorPager<>(Arrays.asList(articleData), CursorPager.Direction.PREV, false);
    when(articleQueryService.findRecentArticlesWithCursor(any(), any(), any(), any(), any()))
        .thenReturn(pager);
    DgsDataFetchingEnvironment dfe = Mockito.mock(DgsDataFetchingEnvironment.class);

    DataFetcherResult<ArticlesConnection> result =
        articleDatafetcher.getArticles(null, null, 10, null, null, null, null, dfe);
    assertNotNull(result);
  }

  @Test
  public void should_throw_when_first_and_last_both_null_on_get_articles() {
    DgsDataFetchingEnvironment dfe = Mockito.mock(DgsDataFetchingEnvironment.class);
    assertThrows(
        IllegalArgumentException.class,
        () -> articleDatafetcher.getArticles(null, null, null, null, null, null, null, dfe));
  }

  @Test
  public void should_get_article_from_payload() {
    Article coreArticle = new Article("Title", "desc", "body", Arrays.asList("java"), user.getId());
    ArticleData articleData = TestHelper.getArticleDataFromArticleAndUser(coreArticle, user);
    when(articleQueryService.findById(eq(coreArticle.getId()), any()))
        .thenReturn(Optional.of(articleData));

    DataFetchingEnvironment dfe = Mockito.mock(DataFetchingEnvironment.class);
    when(dfe.getLocalContext()).thenReturn(coreArticle);

    DataFetcherResult<io.spring.graphql.types.Article> result = articleDatafetcher.getArticle(dfe);
    assertNotNull(result);
    assertEquals(articleData.getSlug(), result.getData().getSlug());
  }

  @Test
  public void should_get_comment_article() {
    Article coreArticle = new Article("Title", "desc", "body", Arrays.asList("java"), user.getId());
    ArticleData articleData = TestHelper.getArticleDataFromArticleAndUser(coreArticle, user);
    when(articleQueryService.findById(eq(coreArticle.getId()), any()))
        .thenReturn(Optional.of(articleData));
    CommentData commentData =
        new CommentData(
            "cid",
            "body",
            coreArticle.getId(),
            new DateTime(),
            new DateTime(),
            new ProfileData(user.getId(), user.getUsername(), "", "", false));

    DataFetchingEnvironment dfe = Mockito.mock(DataFetchingEnvironment.class);
    when(dfe.getLocalContext()).thenReturn(commentData);

    DataFetcherResult<io.spring.graphql.types.Article> result =
        articleDatafetcher.getCommentArticle(dfe);
    assertNotNull(result);
    assertEquals(articleData.getSlug(), result.getData().getSlug());
  }

  @Test
  public void should_find_article_by_slug() {
    ArticleData articleData = TestHelper.articleDataFixture("1", user);
    when(articleQueryService.findBySlug(eq("title-1"), any())).thenReturn(Optional.of(articleData));

    DataFetcherResult<io.spring.graphql.types.Article> result =
        articleDatafetcher.findArticleBySlug("title-1");
    assertNotNull(result);
    assertEquals("title-1", result.getData().getSlug());
  }

  @Test
  public void should_throw_when_article_not_found_by_slug() {
    when(articleQueryService.findBySlug(eq("missing"), any())).thenReturn(Optional.empty());
    assertThrows(
        ResourceNotFoundException.class, () -> articleDatafetcher.findArticleBySlug("missing"));
  }

  @Test
  public void should_build_page_info_with_cursors() {
    ArticleData a1 = TestHelper.articleDataFixture("1", user);
    ArticleData a2 = TestHelper.articleDataFixture("2", user);
    CursorPager<ArticleData> pager =
        new CursorPager<>(Arrays.asList(a1, a2), CursorPager.Direction.NEXT, true);
    when(articleQueryService.findUserFeedWithCursor(eq(user), any())).thenReturn(pager);

    DgsDataFetchingEnvironment dfe = Mockito.mock(DgsDataFetchingEnvironment.class);
    DataFetcherResult<ArticlesConnection> result =
        articleDatafetcher.getFeed(10, null, null, null, dfe);
    assertNotNull(result.getData().getPageInfo());
    assertTrue(result.getData().getPageInfo().isHasNextPage());
  }

  @Test
  public void should_build_page_info_with_null_cursors() {
    CursorPager<ArticleData> pager =
        new CursorPager<>(Arrays.asList(), CursorPager.Direction.NEXT, false);
    when(articleQueryService.findUserFeedWithCursor(eq(user), any())).thenReturn(pager);

    DgsDataFetchingEnvironment dfe = Mockito.mock(DgsDataFetchingEnvironment.class);
    DataFetcherResult<ArticlesConnection> result =
        articleDatafetcher.getFeed(10, null, null, null, dfe);
    assertNotNull(result.getData().getPageInfo());
    assertNull(result.getData().getPageInfo().getStartCursor());
    assertNull(result.getData().getPageInfo().getEndCursor());
  }
}
