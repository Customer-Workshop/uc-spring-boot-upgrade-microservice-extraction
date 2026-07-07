package io.spring.graphql;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.netflix.graphql.dgs.DgsDataFetchingEnvironment;
import graphql.execution.DataFetcherResult;
import graphql.schema.DataFetchingEnvironment;
import io.spring.api.exception.ResourceNotFoundException;
import io.spring.application.ArticleQueryService;
import io.spring.application.CursorPager;
import io.spring.application.CursorPager.Direction;
import io.spring.application.data.ArticleData;
import io.spring.application.data.CommentData;
import io.spring.application.data.ProfileData;
import io.spring.core.article.Article;
import io.spring.core.user.User;
import io.spring.core.user.UserRepository;
import io.spring.graphql.types.ArticlesConnection;
import io.spring.graphql.types.Profile;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import org.joda.time.DateTime;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

public class ArticleDatafetcherTest {

  private ArticleQueryService articleQueryService;
  private UserRepository userRepository;
  private ArticleDatafetcher articleDatafetcher;
  private MockedStatic<SecurityUtil> securityUtil;
  private User user;

  @BeforeEach
  public void setUp() {
    articleQueryService = Mockito.mock(ArticleQueryService.class);
    userRepository = Mockito.mock(UserRepository.class);
    articleDatafetcher = new ArticleDatafetcher(articleQueryService, userRepository);
    user = new User("test@example.com", "tester", "123", "", "");
    securityUtil = Mockito.mockStatic(SecurityUtil.class);
    securityUtil.when(SecurityUtil::getCurrentUser).thenReturn(Optional.of(user));
  }

  @AfterEach
  public void tearDown() {
    securityUtil.close();
  }

  private ArticleData articleData(String seed) {
    DateTime now = new DateTime();
    return new ArticleData(
        seed + "-id",
        "slug-" + seed,
        "title " + seed,
        "desc " + seed,
        "body " + seed,
        false,
        0,
        now,
        now,
        Arrays.asList("java"),
        new ProfileData("uid", "author", "bio", "img", false));
  }

  private CursorPager<ArticleData> pagerWith(String seed, Direction direction) {
    return new CursorPager<>(Arrays.asList(articleData(seed)), direction, false);
  }

  @Test
  public void should_get_feed_with_first() {
    when(articleQueryService.findUserFeedWithCursor(any(), any()))
        .thenReturn(pagerWith("a", Direction.NEXT));
    DgsDataFetchingEnvironment dfe = Mockito.mock(DgsDataFetchingEnvironment.class);

    DataFetcherResult<ArticlesConnection> result =
        articleDatafetcher.getFeed(10, null, null, null, dfe);

    assertEquals(1, result.getData().getEdges().size());
  }

  @Test
  public void should_get_feed_with_last() {
    when(articleQueryService.findUserFeedWithCursor(any(), any()))
        .thenReturn(new CursorPager<>(Collections.emptyList(), Direction.PREV, false));
    DgsDataFetchingEnvironment dfe = Mockito.mock(DgsDataFetchingEnvironment.class);

    DataFetcherResult<ArticlesConnection> result =
        articleDatafetcher.getFeed(null, null, 10, null, dfe);

    assertEquals(0, result.getData().getEdges().size());
  }

  @Test
  public void should_throw_when_get_feed_no_first_and_last() {
    DgsDataFetchingEnvironment dfe = Mockito.mock(DgsDataFetchingEnvironment.class);
    assertThrows(
        IllegalArgumentException.class,
        () -> articleDatafetcher.getFeed(null, null, null, null, dfe));
  }

  @Test
  public void should_get_user_feed_with_first() {
    when(userRepository.findByUsername("author")).thenReturn(Optional.of(user));
    when(articleQueryService.findUserFeedWithCursor(any(), any()))
        .thenReturn(pagerWith("a", Direction.NEXT));
    DgsDataFetchingEnvironment dfe = Mockito.mock(DgsDataFetchingEnvironment.class);
    when(dfe.getSource()).thenReturn(Profile.newBuilder().username("author").build());

    DataFetcherResult<ArticlesConnection> result =
        articleDatafetcher.userFeed(10, null, null, null, dfe);

    assertEquals(1, result.getData().getEdges().size());
  }

  @Test
  public void should_get_user_feed_with_last() {
    when(userRepository.findByUsername("author")).thenReturn(Optional.of(user));
    when(articleQueryService.findUserFeedWithCursor(any(), any()))
        .thenReturn(pagerWith("a", Direction.PREV));
    DgsDataFetchingEnvironment dfe = Mockito.mock(DgsDataFetchingEnvironment.class);
    when(dfe.getSource()).thenReturn(Profile.newBuilder().username("author").build());

    DataFetcherResult<ArticlesConnection> result =
        articleDatafetcher.userFeed(null, null, 10, null, dfe);

    assertEquals(1, result.getData().getEdges().size());
  }

  @Test
  public void should_throw_when_user_feed_user_not_found() {
    when(userRepository.findByUsername("author")).thenReturn(Optional.empty());
    DgsDataFetchingEnvironment dfe = Mockito.mock(DgsDataFetchingEnvironment.class);
    when(dfe.getSource()).thenReturn(Profile.newBuilder().username("author").build());

    assertThrows(
        ResourceNotFoundException.class,
        () -> articleDatafetcher.userFeed(10, null, null, null, dfe));
  }

  @Test
  public void should_throw_when_user_feed_no_first_and_last() {
    DgsDataFetchingEnvironment dfe = Mockito.mock(DgsDataFetchingEnvironment.class);
    assertThrows(
        IllegalArgumentException.class,
        () -> articleDatafetcher.userFeed(null, null, null, null, dfe));
  }

  @Test
  public void should_get_user_favorites_with_first() {
    when(articleQueryService.findRecentArticlesWithCursor(any(), any(), any(), any(), any()))
        .thenReturn(pagerWith("a", Direction.NEXT));
    DgsDataFetchingEnvironment dfe = Mockito.mock(DgsDataFetchingEnvironment.class);
    when(dfe.getSource()).thenReturn(Profile.newBuilder().username("author").build());

    DataFetcherResult<ArticlesConnection> result =
        articleDatafetcher.userFavorites(10, null, null, null, dfe);

    assertEquals(1, result.getData().getEdges().size());
  }

  @Test
  public void should_get_user_favorites_with_last() {
    when(articleQueryService.findRecentArticlesWithCursor(any(), any(), any(), any(), any()))
        .thenReturn(pagerWith("a", Direction.PREV));
    DgsDataFetchingEnvironment dfe = Mockito.mock(DgsDataFetchingEnvironment.class);
    when(dfe.getSource()).thenReturn(Profile.newBuilder().username("author").build());

    DataFetcherResult<ArticlesConnection> result =
        articleDatafetcher.userFavorites(null, null, 10, null, dfe);

    assertEquals(1, result.getData().getEdges().size());
  }

  @Test
  public void should_throw_when_user_favorites_no_first_and_last() {
    DgsDataFetchingEnvironment dfe = Mockito.mock(DgsDataFetchingEnvironment.class);
    assertThrows(
        IllegalArgumentException.class,
        () -> articleDatafetcher.userFavorites(null, null, null, null, dfe));
  }

  @Test
  public void should_get_user_articles_with_first() {
    when(articleQueryService.findRecentArticlesWithCursor(any(), any(), any(), any(), any()))
        .thenReturn(pagerWith("a", Direction.NEXT));
    DgsDataFetchingEnvironment dfe = Mockito.mock(DgsDataFetchingEnvironment.class);
    when(dfe.getSource()).thenReturn(Profile.newBuilder().username("author").build());

    DataFetcherResult<ArticlesConnection> result =
        articleDatafetcher.userArticles(10, null, null, null, dfe);

    assertEquals(1, result.getData().getEdges().size());
  }

  @Test
  public void should_get_user_articles_with_last() {
    when(articleQueryService.findRecentArticlesWithCursor(any(), any(), any(), any(), any()))
        .thenReturn(pagerWith("a", Direction.PREV));
    DgsDataFetchingEnvironment dfe = Mockito.mock(DgsDataFetchingEnvironment.class);
    when(dfe.getSource()).thenReturn(Profile.newBuilder().username("author").build());

    DataFetcherResult<ArticlesConnection> result =
        articleDatafetcher.userArticles(null, null, 10, null, dfe);

    assertEquals(1, result.getData().getEdges().size());
  }

  @Test
  public void should_throw_when_user_articles_no_first_and_last() {
    DgsDataFetchingEnvironment dfe = Mockito.mock(DgsDataFetchingEnvironment.class);
    assertThrows(
        IllegalArgumentException.class,
        () -> articleDatafetcher.userArticles(null, null, null, null, dfe));
  }

  @Test
  public void should_get_articles_with_first() {
    when(articleQueryService.findRecentArticlesWithCursor(any(), any(), any(), any(), any()))
        .thenReturn(pagerWith("a", Direction.NEXT));
    DgsDataFetchingEnvironment dfe = Mockito.mock(DgsDataFetchingEnvironment.class);

    DataFetcherResult<ArticlesConnection> result =
        articleDatafetcher.getArticles(10, null, null, null, "author", "favoritedBy", "tag", dfe);

    assertEquals(1, result.getData().getEdges().size());
  }

  @Test
  public void should_get_articles_with_last() {
    when(articleQueryService.findRecentArticlesWithCursor(any(), any(), any(), any(), any()))
        .thenReturn(pagerWith("a", Direction.PREV));
    DgsDataFetchingEnvironment dfe = Mockito.mock(DgsDataFetchingEnvironment.class);

    DataFetcherResult<ArticlesConnection> result =
        articleDatafetcher.getArticles(null, null, 10, null, "author", "favoritedBy", "tag", dfe);

    assertEquals(1, result.getData().getEdges().size());
  }

  @Test
  public void should_throw_when_get_articles_no_first_and_last() {
    DgsDataFetchingEnvironment dfe = Mockito.mock(DgsDataFetchingEnvironment.class);
    assertThrows(
        IllegalArgumentException.class,
        () -> articleDatafetcher.getArticles(null, null, null, null, null, null, null, dfe));
  }

  @Test
  public void should_get_article() {
    Article article = new Article("title", "desc", "body", Arrays.asList("java"), user.getId());
    ArticleData data = articleData("a");
    when(articleQueryService.findById(eq(article.getId()), any())).thenReturn(Optional.of(data));
    DataFetchingEnvironment dfe = Mockito.mock(DataFetchingEnvironment.class);
    when(dfe.getLocalContext()).thenReturn(article);

    DataFetcherResult<io.spring.graphql.types.Article> result = articleDatafetcher.getArticle(dfe);

    assertNotNull(result.getData());
    assertEquals("slug-a", result.getData().getSlug());
  }

  @Test
  public void should_throw_when_get_article_not_found() {
    Article article = new Article("title", "desc", "body", Arrays.asList("java"), user.getId());
    when(articleQueryService.findById(eq(article.getId()), any())).thenReturn(Optional.empty());
    DataFetchingEnvironment dfe = Mockito.mock(DataFetchingEnvironment.class);
    when(dfe.getLocalContext()).thenReturn(article);

    assertThrows(ResourceNotFoundException.class, () -> articleDatafetcher.getArticle(dfe));
  }

  @Test
  public void should_get_comment_article() {
    CommentData comment =
        new CommentData(
            "c1",
            "body",
            "article-1",
            new DateTime(),
            new DateTime(),
            new ProfileData("uid", "author", "bio", "img", false));
    ArticleData data = articleData("a");
    when(articleQueryService.findById(eq("article-1"), any())).thenReturn(Optional.of(data));
    DataFetchingEnvironment dfe = Mockito.mock(DataFetchingEnvironment.class);
    when(dfe.getLocalContext()).thenReturn(comment);

    DataFetcherResult<io.spring.graphql.types.Article> result =
        articleDatafetcher.getCommentArticle(dfe);

    assertNotNull(result.getData());
  }

  @Test
  public void should_throw_when_comment_article_not_found() {
    CommentData comment =
        new CommentData(
            "c1",
            "body",
            "article-1",
            new DateTime(),
            new DateTime(),
            new ProfileData("uid", "author", "bio", "img", false));
    when(articleQueryService.findById(eq("article-1"), any())).thenReturn(Optional.empty());
    DataFetchingEnvironment dfe = Mockito.mock(DataFetchingEnvironment.class);
    when(dfe.getLocalContext()).thenReturn(comment);

    assertThrows(ResourceNotFoundException.class, () -> articleDatafetcher.getCommentArticle(dfe));
  }

  @Test
  public void should_find_article_by_slug() {
    ArticleData data = articleData("a");
    when(articleQueryService.findBySlug(eq("slug-a"), any())).thenReturn(Optional.of(data));

    DataFetcherResult<io.spring.graphql.types.Article> result =
        articleDatafetcher.findArticleBySlug("slug-a");

    assertNotNull(result.getData());
    assertEquals("slug-a", result.getData().getSlug());
  }

  @Test
  public void should_throw_when_find_article_by_slug_not_found() {
    when(articleQueryService.findBySlug(eq("missing"), any())).thenReturn(Optional.empty());

    assertThrows(
        ResourceNotFoundException.class, () -> articleDatafetcher.findArticleBySlug("missing"));
  }
}
