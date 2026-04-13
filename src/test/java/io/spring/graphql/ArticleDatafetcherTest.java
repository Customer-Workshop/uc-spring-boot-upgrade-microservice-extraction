package io.spring.graphql;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import com.netflix.graphql.dgs.DgsDataFetchingEnvironment;
import graphql.execution.DataFetcherResult;
import graphql.schema.DataFetchingEnvironment;
import io.spring.application.ArticleQueryService;
import io.spring.application.CursorPager;
import io.spring.application.CursorPager.Direction;
import io.spring.application.data.ArticleData;
import io.spring.application.data.CommentData;
import io.spring.application.data.ProfileData;
import io.spring.core.user.User;
import io.spring.core.user.UserRepository;
import io.spring.graphql.types.Article;
import io.spring.graphql.types.ArticlesConnection;
import io.spring.graphql.types.Profile;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;
import org.joda.time.DateTime;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

public class ArticleDatafetcherTest {

  @Mock private ArticleQueryService articleQueryService;
  @Mock private UserRepository userRepository;
  @Mock private DgsDataFetchingEnvironment dfe;
  @Mock private DataFetchingEnvironment dataFetchingEnvironment;

  private ArticleDatafetcher articleDatafetcher;
  private User user;
  private AutoCloseable closeable;

  @BeforeEach
  void setUp() {
    closeable = MockitoAnnotations.openMocks(this);
    articleDatafetcher = new ArticleDatafetcher(articleQueryService, userRepository);
    user = new User("test@test.com", "testuser", "password", "bio", "image");
    SecurityContextHolder.getContext()
        .setAuthentication(new TestingAuthenticationToken(user, null));
  }

  @AfterEach
  void tearDown() throws Exception {
    SecurityContextHolder.clearContext();
    closeable.close();
  }

  private ArticleData createArticleData(String seed) {
    DateTime now = new DateTime();
    return new ArticleData(
        seed + "-id",
        "slug-" + seed,
        "title-" + seed,
        "desc-" + seed,
        "body-" + seed,
        false,
        0,
        now,
        now,
        new ArrayList<>(),
        new ProfileData("userId", "author", "bio", "image", false));
  }

  @Test
  void should_get_feed_with_first_parameter() {
    ArticleData articleData = createArticleData("1");
    CursorPager<ArticleData> pager =
        new CursorPager<>(Arrays.asList(articleData), Direction.NEXT, false);
    when(articleQueryService.findUserFeedWithCursor(eq(user), any())).thenReturn(pager);

    DataFetcherResult<ArticlesConnection> result =
        articleDatafetcher.getFeed(10, null, null, null, dfe);

    assertNotNull(result);
    assertNotNull(result.getData());
    assertEquals(1, result.getData().getEdges().size());
  }

  @Test
  void should_get_feed_with_last_parameter() {
    ArticleData articleData = createArticleData("1");
    CursorPager<ArticleData> pager =
        new CursorPager<>(Arrays.asList(articleData), Direction.PREV, false);
    when(articleQueryService.findUserFeedWithCursor(eq(user), any())).thenReturn(pager);

    DataFetcherResult<ArticlesConnection> result =
        articleDatafetcher.getFeed(null, null, 10, null, dfe);

    assertNotNull(result);
    assertEquals(1, result.getData().getEdges().size());
  }

  @Test
  void should_throw_when_feed_has_no_first_or_last() {
    assertThrows(
        IllegalArgumentException.class,
        () -> articleDatafetcher.getFeed(null, null, null, null, dfe));
  }

  @Test
  void should_get_feed_with_empty_results() {
    CursorPager<ArticleData> pager = new CursorPager<>(new ArrayList<>(), Direction.NEXT, false);
    when(articleQueryService.findUserFeedWithCursor(eq(user), any())).thenReturn(pager);

    DataFetcherResult<ArticlesConnection> result =
        articleDatafetcher.getFeed(10, null, null, null, dfe);

    assertNotNull(result);
    assertEquals(0, result.getData().getEdges().size());
  }

  @Test
  void should_get_user_feed() {
    Profile profile = Profile.newBuilder().username("targetuser").build();
    when(dfe.getSource()).thenReturn(profile);
    User target = new User("target@test.com", "targetuser", "pass", "", "");
    when(userRepository.findByUsername("targetuser")).thenReturn(Optional.of(target));
    ArticleData articleData = createArticleData("1");
    CursorPager<ArticleData> pager =
        new CursorPager<>(Arrays.asList(articleData), Direction.NEXT, false);
    when(articleQueryService.findUserFeedWithCursor(eq(target), any())).thenReturn(pager);

    DataFetcherResult<ArticlesConnection> result =
        articleDatafetcher.userFeed(10, null, null, null, dfe);

    assertNotNull(result);
    assertEquals(1, result.getData().getEdges().size());
  }

  @Test
  void should_get_user_feed_with_last() {
    Profile profile = Profile.newBuilder().username("targetuser").build();
    when(dfe.getSource()).thenReturn(profile);
    User target = new User("target@test.com", "targetuser", "pass", "", "");
    when(userRepository.findByUsername("targetuser")).thenReturn(Optional.of(target));
    CursorPager<ArticleData> pager = new CursorPager<>(new ArrayList<>(), Direction.PREV, false);
    when(articleQueryService.findUserFeedWithCursor(eq(target), any())).thenReturn(pager);

    DataFetcherResult<ArticlesConnection> result =
        articleDatafetcher.userFeed(null, null, 5, null, dfe);

    assertNotNull(result);
    assertEquals(0, result.getData().getEdges().size());
  }

  @Test
  void should_throw_when_user_feed_has_no_first_or_last() {
    assertThrows(
        IllegalArgumentException.class,
        () -> articleDatafetcher.userFeed(null, null, null, null, dfe));
  }

  @Test
  void should_get_user_favorites_with_first() {
    Profile profile = Profile.newBuilder().username("testuser").build();
    when(dfe.getSource()).thenReturn(profile);
    ArticleData articleData = createArticleData("fav");
    CursorPager<ArticleData> pager =
        new CursorPager<>(Arrays.asList(articleData), Direction.NEXT, false);
    when(articleQueryService.findRecentArticlesWithCursor(
            eq(null), eq(null), eq("testuser"), any(), eq(user)))
        .thenReturn(pager);

    DataFetcherResult<ArticlesConnection> result =
        articleDatafetcher.userFavorites(10, null, null, null, dfe);

    assertNotNull(result);
    assertEquals(1, result.getData().getEdges().size());
  }

  @Test
  void should_get_user_favorites_with_last() {
    Profile profile = Profile.newBuilder().username("testuser").build();
    when(dfe.getSource()).thenReturn(profile);
    CursorPager<ArticleData> pager = new CursorPager<>(new ArrayList<>(), Direction.PREV, false);
    when(articleQueryService.findRecentArticlesWithCursor(
            eq(null), eq(null), eq("testuser"), any(), eq(user)))
        .thenReturn(pager);

    DataFetcherResult<ArticlesConnection> result =
        articleDatafetcher.userFavorites(null, null, 5, null, dfe);

    assertNotNull(result);
    assertEquals(0, result.getData().getEdges().size());
  }

  @Test
  void should_throw_when_user_favorites_has_no_first_or_last() {
    assertThrows(
        IllegalArgumentException.class,
        () -> articleDatafetcher.userFavorites(null, null, null, null, dfe));
  }

  @Test
  void should_get_user_articles_with_first() {
    Profile profile = Profile.newBuilder().username("testuser").build();
    when(dfe.getSource()).thenReturn(profile);
    ArticleData articleData = createArticleData("art");
    CursorPager<ArticleData> pager =
        new CursorPager<>(Arrays.asList(articleData), Direction.NEXT, false);
    when(articleQueryService.findRecentArticlesWithCursor(
            eq(null), eq("testuser"), eq(null), any(), eq(user)))
        .thenReturn(pager);

    DataFetcherResult<ArticlesConnection> result =
        articleDatafetcher.userArticles(10, null, null, null, dfe);

    assertNotNull(result);
    assertEquals(1, result.getData().getEdges().size());
  }

  @Test
  void should_get_user_articles_with_last() {
    Profile profile = Profile.newBuilder().username("testuser").build();
    when(dfe.getSource()).thenReturn(profile);
    CursorPager<ArticleData> pager = new CursorPager<>(new ArrayList<>(), Direction.PREV, false);
    when(articleQueryService.findRecentArticlesWithCursor(
            eq(null), eq("testuser"), eq(null), any(), eq(user)))
        .thenReturn(pager);

    DataFetcherResult<ArticlesConnection> result =
        articleDatafetcher.userArticles(null, null, 5, null, dfe);

    assertNotNull(result);
  }

  @Test
  void should_throw_when_user_articles_has_no_first_or_last() {
    assertThrows(
        IllegalArgumentException.class,
        () -> articleDatafetcher.userArticles(null, null, null, null, dfe));
  }

  @Test
  void should_get_articles_with_first() {
    ArticleData articleData = createArticleData("list");
    CursorPager<ArticleData> pager =
        new CursorPager<>(Arrays.asList(articleData), Direction.NEXT, true);
    when(articleQueryService.findRecentArticlesWithCursor(
            eq("java"), eq("author1"), eq("fav1"), any(), eq(user)))
        .thenReturn(pager);

    DataFetcherResult<ArticlesConnection> result =
        articleDatafetcher.getArticles(10, null, null, null, "author1", "fav1", "java", dfe);

    assertNotNull(result);
    assertEquals(1, result.getData().getEdges().size());
    assertTrue(result.getData().getPageInfo().isHasNextPage());
  }

  @Test
  void should_get_articles_with_last() {
    CursorPager<ArticleData> pager = new CursorPager<>(new ArrayList<>(), Direction.PREV, false);
    when(articleQueryService.findRecentArticlesWithCursor(
            eq(null), eq(null), eq(null), any(), eq(user)))
        .thenReturn(pager);

    DataFetcherResult<ArticlesConnection> result =
        articleDatafetcher.getArticles(null, null, 5, null, null, null, null, dfe);

    assertNotNull(result);
    assertEquals(0, result.getData().getEdges().size());
  }

  @Test
  void should_throw_when_articles_has_no_first_or_last() {
    assertThrows(
        IllegalArgumentException.class,
        () -> articleDatafetcher.getArticles(null, null, null, null, null, null, null, dfe));
  }

  @Test
  void should_get_article_from_payload() {
    io.spring.core.article.Article coreArticle =
        new io.spring.core.article.Article(
            "Test Title", "desc", "body", Arrays.asList("java"), user.getId());
    when(dataFetchingEnvironment.getLocalContext()).thenReturn(coreArticle);
    ArticleData articleData = createArticleData("payload");
    when(articleQueryService.findById(eq(coreArticle.getId()), eq(user)))
        .thenReturn(Optional.of(articleData));

    DataFetcherResult<Article> result = articleDatafetcher.getArticle(dataFetchingEnvironment);

    assertNotNull(result);
    assertNotNull(result.getData());
    assertEquals("slug-payload", result.getData().getSlug());
  }

  @Test
  void should_get_comment_article() {
    DateTime now = new DateTime();
    CommentData commentData =
        new CommentData(
            "comment-id",
            "body",
            "article-id",
            now,
            now,
            new ProfileData("userId", "author", "bio", "image", false));
    when(dataFetchingEnvironment.getLocalContext()).thenReturn(commentData);
    ArticleData articleData = createArticleData("comment-article");
    when(articleQueryService.findById(eq("article-id"), eq(user)))
        .thenReturn(Optional.of(articleData));

    DataFetcherResult<Article> result =
        articleDatafetcher.getCommentArticle(dataFetchingEnvironment);

    assertNotNull(result);
    assertEquals("slug-comment-article", result.getData().getSlug());
  }

  @Test
  void should_find_article_by_slug() {
    ArticleData articleData = createArticleData("found");
    when(articleQueryService.findBySlug(eq("slug-found"), eq(user)))
        .thenReturn(Optional.of(articleData));

    DataFetcherResult<Article> result = articleDatafetcher.findArticleBySlug("slug-found");

    assertNotNull(result);
    assertEquals("slug-found", result.getData().getSlug());
    assertEquals("title-found", result.getData().getTitle());
  }

  @Test
  void should_throw_when_article_by_slug_not_found() {
    when(articleQueryService.findBySlug(eq("missing"), any())).thenReturn(Optional.empty());

    assertThrows(
        io.spring.api.exception.ResourceNotFoundException.class,
        () -> articleDatafetcher.findArticleBySlug("missing"));
  }

  @Test
  void should_get_feed_with_cursor_parameters() {
    ArticleData articleData = createArticleData("cursor");
    CursorPager<ArticleData> pager =
        new CursorPager<>(Arrays.asList(articleData), Direction.NEXT, true);
    when(articleQueryService.findUserFeedWithCursor(eq(user), any())).thenReturn(pager);

    String afterCursor = String.valueOf(new DateTime().getMillis());
    DataFetcherResult<ArticlesConnection> result =
        articleDatafetcher.getFeed(10, afterCursor, null, null, dfe);

    assertNotNull(result);
    assertTrue(result.getData().getPageInfo().isHasNextPage());
  }

  @Test
  void should_get_articles_with_has_next_page_info() {
    ArticleData a1 = createArticleData("a1");
    ArticleData a2 = createArticleData("a2");
    CursorPager<ArticleData> pager = new CursorPager<>(Arrays.asList(a1, a2), Direction.NEXT, true);
    when(articleQueryService.findRecentArticlesWithCursor(any(), any(), any(), any(), any()))
        .thenReturn(pager);

    DataFetcherResult<ArticlesConnection> result =
        articleDatafetcher.getArticles(10, null, null, null, null, null, null, dfe);

    assertNotNull(result.getData().getPageInfo());
    assertTrue(result.getData().getPageInfo().isHasNextPage());
    assertFalse(result.getData().getPageInfo().isHasPreviousPage());
    assertEquals(2, result.getData().getEdges().size());
  }
}
