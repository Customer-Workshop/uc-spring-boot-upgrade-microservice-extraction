package io.spring.graphql;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
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
import java.util.Optional;
import org.joda.time.DateTime;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
public class ArticleDatafetcherTest {

  @Mock private ArticleQueryService articleQueryService;
  @Mock private UserRepository userRepository;
  @Mock private DataFetchingEnvironment dataFetchingEnvironment;

  private ArticleDatafetcher articleDatafetcher;
  private User user;
  private ArticleData articleData;

  @BeforeEach
  void setUp() {
    articleDatafetcher = new ArticleDatafetcher(articleQueryService, userRepository);
    user = new User("test@example.com", "testuser", "password", "bio", "image");

    DateTime now = new DateTime();
    articleData =
        new ArticleData(
            "article-id",
            "test-article",
            "Test Article",
            "description",
            "body",
            false,
            5,
            now,
            now,
            asList("java", "spring"),
            new ProfileData(user.getId(), "testuser", "bio", "image", false));
  }

  @AfterEach
  void tearDown() {
    SecurityContextHolder.clearContext();
  }

  private void setAuthenticatedUser(User user) {
    SecurityContextHolder.getContext()
        .setAuthentication(new TestingAuthenticationToken(user, null));
  }

  private void setAnonymousUser() {
    SecurityContextHolder.getContext()
        .setAuthentication(
            new AnonymousAuthenticationToken(
                "key", "anonymous", AuthorityUtils.createAuthorityList("ROLE_ANONYMOUS")));
  }

  private DgsDataFetchingEnvironment dgsEnv() {
    return new DgsDataFetchingEnvironment(dataFetchingEnvironment);
  }

  @Test
  void getFeed_withFirst_returnsArticlesConnection() {
    setAuthenticatedUser(user);
    CursorPager<ArticleData> pager = new CursorPager<>(asList(articleData), Direction.NEXT, false);
    when(articleQueryService.findUserFeedWithCursor(eq(user), any())).thenReturn(pager);

    DataFetcherResult<ArticlesConnection> result =
        articleDatafetcher.getFeed(10, null, null, null, dgsEnv());

    assertNotNull(result);
    assertNotNull(result.getData());
    assertEquals(1, result.getData().getEdges().size());
    assertEquals("test-article", result.getData().getEdges().get(0).getNode().getSlug());
  }

  @Test
  void getFeed_withLast_returnsArticlesConnection() {
    setAuthenticatedUser(user);
    CursorPager<ArticleData> pager = new CursorPager<>(asList(articleData), Direction.PREV, false);
    when(articleQueryService.findUserFeedWithCursor(eq(user), any())).thenReturn(pager);

    DataFetcherResult<ArticlesConnection> result =
        articleDatafetcher.getFeed(null, null, 5, null, dgsEnv());

    assertNotNull(result);
    assertEquals(1, result.getData().getEdges().size());
  }

  @Test
  void getFeed_withoutFirstOrLast_throwsException() {
    assertThrows(
        IllegalArgumentException.class,
        () -> articleDatafetcher.getFeed(null, null, null, null, dgsEnv()));
  }

  @Test
  void getFeed_withEmptyResult_returnsEmptyConnection() {
    setAuthenticatedUser(user);
    CursorPager<ArticleData> pager = new CursorPager<>(emptyList(), Direction.NEXT, false);
    when(articleQueryService.findUserFeedWithCursor(eq(user), any())).thenReturn(pager);

    DataFetcherResult<ArticlesConnection> result =
        articleDatafetcher.getFeed(10, null, null, null, dgsEnv());

    assertNotNull(result);
    assertTrue(result.getData().getEdges().isEmpty());
  }

  @Test
  void getFeed_withHasNext_setsPageInfoCorrectly() {
    setAuthenticatedUser(user);
    CursorPager<ArticleData> pager = new CursorPager<>(asList(articleData), Direction.NEXT, true);
    when(articleQueryService.findUserFeedWithCursor(eq(user), any())).thenReturn(pager);

    DataFetcherResult<ArticlesConnection> result =
        articleDatafetcher.getFeed(10, null, null, null, dgsEnv());

    assertNotNull(result.getData().getPageInfo());
    assertTrue(result.getData().getPageInfo().isHasNextPage());
    assertFalse(result.getData().getPageInfo().isHasPreviousPage());
  }

  @Test
  void userFeed_withFirst_returnsArticlesConnection() {
    Profile profile = Profile.newBuilder().username("testuser").build();
    when(dataFetchingEnvironment.getSource()).thenReturn(profile);
    when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
    CursorPager<ArticleData> pager = new CursorPager<>(asList(articleData), Direction.NEXT, false);
    when(articleQueryService.findUserFeedWithCursor(eq(user), any())).thenReturn(pager);

    DataFetcherResult<ArticlesConnection> result =
        articleDatafetcher.userFeed(10, null, null, null, dgsEnv());

    assertNotNull(result);
    assertEquals(1, result.getData().getEdges().size());
  }

  @Test
  void userFeed_withLast_returnsArticlesConnection() {
    Profile profile = Profile.newBuilder().username("testuser").build();
    when(dataFetchingEnvironment.getSource()).thenReturn(profile);
    when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
    CursorPager<ArticleData> pager = new CursorPager<>(asList(articleData), Direction.PREV, false);
    when(articleQueryService.findUserFeedWithCursor(eq(user), any())).thenReturn(pager);

    DataFetcherResult<ArticlesConnection> result =
        articleDatafetcher.userFeed(null, null, 5, null, dgsEnv());

    assertNotNull(result);
    assertEquals(1, result.getData().getEdges().size());
  }

  @Test
  void userFeed_withoutFirstOrLast_throwsException() {
    assertThrows(
        IllegalArgumentException.class,
        () -> articleDatafetcher.userFeed(null, null, null, null, dgsEnv()));
  }

  @Test
  void userFavorites_withFirst_returnsArticlesConnection() {
    setAuthenticatedUser(user);
    Profile profile = Profile.newBuilder().username("testuser").build();
    when(dataFetchingEnvironment.getSource()).thenReturn(profile);
    CursorPager<ArticleData> pager = new CursorPager<>(asList(articleData), Direction.NEXT, false);
    when(articleQueryService.findRecentArticlesWithCursor(
            eq(null), eq(null), eq("testuser"), any(), eq(user)))
        .thenReturn(pager);

    DataFetcherResult<ArticlesConnection> result =
        articleDatafetcher.userFavorites(10, null, null, null, dgsEnv());

    assertNotNull(result);
    assertEquals(1, result.getData().getEdges().size());
  }

  @Test
  void userFavorites_withLast_returnsArticlesConnection() {
    setAuthenticatedUser(user);
    Profile profile = Profile.newBuilder().username("testuser").build();
    when(dataFetchingEnvironment.getSource()).thenReturn(profile);
    CursorPager<ArticleData> pager = new CursorPager<>(asList(articleData), Direction.PREV, false);
    when(articleQueryService.findRecentArticlesWithCursor(
            eq(null), eq(null), eq("testuser"), any(), eq(user)))
        .thenReturn(pager);

    DataFetcherResult<ArticlesConnection> result =
        articleDatafetcher.userFavorites(null, null, 5, null, dgsEnv());

    assertNotNull(result);
  }

  @Test
  void userFavorites_withoutFirstOrLast_throwsException() {
    assertThrows(
        IllegalArgumentException.class,
        () -> articleDatafetcher.userFavorites(null, null, null, null, dgsEnv()));
  }

  @Test
  void userArticles_withFirst_returnsArticlesConnection() {
    setAuthenticatedUser(user);
    Profile profile = Profile.newBuilder().username("testuser").build();
    when(dataFetchingEnvironment.getSource()).thenReturn(profile);
    CursorPager<ArticleData> pager = new CursorPager<>(asList(articleData), Direction.NEXT, false);
    when(articleQueryService.findRecentArticlesWithCursor(
            eq(null), eq("testuser"), eq(null), any(), eq(user)))
        .thenReturn(pager);

    DataFetcherResult<ArticlesConnection> result =
        articleDatafetcher.userArticles(10, null, null, null, dgsEnv());

    assertNotNull(result);
    assertEquals(1, result.getData().getEdges().size());
  }

  @Test
  void userArticles_withLast_returnsArticlesConnection() {
    setAuthenticatedUser(user);
    Profile profile = Profile.newBuilder().username("testuser").build();
    when(dataFetchingEnvironment.getSource()).thenReturn(profile);
    CursorPager<ArticleData> pager = new CursorPager<>(asList(articleData), Direction.PREV, false);
    when(articleQueryService.findRecentArticlesWithCursor(
            eq(null), eq("testuser"), eq(null), any(), eq(user)))
        .thenReturn(pager);

    DataFetcherResult<ArticlesConnection> result =
        articleDatafetcher.userArticles(null, null, 5, null, dgsEnv());

    assertNotNull(result);
  }

  @Test
  void userArticles_withoutFirstOrLast_throwsException() {
    assertThrows(
        IllegalArgumentException.class,
        () -> articleDatafetcher.userArticles(null, null, null, null, dgsEnv()));
  }

  @Test
  void getArticles_withFirst_returnsArticlesConnection() {
    setAuthenticatedUser(user);
    CursorPager<ArticleData> pager = new CursorPager<>(asList(articleData), Direction.NEXT, false);
    when(articleQueryService.findRecentArticlesWithCursor(
            eq("java"), eq("author"), eq("fav"), any(), eq(user)))
        .thenReturn(pager);

    DataFetcherResult<ArticlesConnection> result =
        articleDatafetcher.getArticles(10, null, null, null, "author", "fav", "java", dgsEnv());

    assertNotNull(result);
    assertEquals(1, result.getData().getEdges().size());
  }

  @Test
  void getArticles_withLast_returnsArticlesConnection() {
    setAuthenticatedUser(user);
    CursorPager<ArticleData> pager = new CursorPager<>(asList(articleData), Direction.PREV, false);
    when(articleQueryService.findRecentArticlesWithCursor(
            eq(null), eq(null), eq(null), any(), eq(user)))
        .thenReturn(pager);

    DataFetcherResult<ArticlesConnection> result =
        articleDatafetcher.getArticles(null, null, 5, null, null, null, null, dgsEnv());

    assertNotNull(result);
  }

  @Test
  void getArticles_withoutFirstOrLast_throwsException() {
    assertThrows(
        IllegalArgumentException.class,
        () -> articleDatafetcher.getArticles(null, null, null, null, null, null, null, dgsEnv()));
  }

  @Test
  void getArticle_returnsArticleFromLocalContext() {
    setAuthenticatedUser(user);
    io.spring.core.article.Article coreArticle =
        new io.spring.core.article.Article("Title", "desc", "body", emptyList(), user.getId());
    when(dataFetchingEnvironment.getLocalContext()).thenReturn(coreArticle);
    when(articleQueryService.findById(eq(coreArticle.getId()), eq(user)))
        .thenReturn(Optional.of(articleData));

    DataFetcherResult<Article> result = articleDatafetcher.getArticle(dataFetchingEnvironment);

    assertNotNull(result);
    assertEquals("test-article", result.getData().getSlug());
    assertEquals("Test Article", result.getData().getTitle());
    assertEquals("body", result.getData().getBody());
  }

  @Test
  void getCommentArticle_returnsArticleForComment() {
    setAuthenticatedUser(user);
    DateTime now = new DateTime();
    CommentData commentData =
        new CommentData(
            "comment-id",
            "comment body",
            "article-id",
            now,
            now,
            new ProfileData(user.getId(), "testuser", "bio", "image", false));
    when(dataFetchingEnvironment.getLocalContext()).thenReturn(commentData);
    when(articleQueryService.findById(eq("article-id"), eq(user)))
        .thenReturn(Optional.of(articleData));

    DataFetcherResult<Article> result =
        articleDatafetcher.getCommentArticle(dataFetchingEnvironment);

    assertNotNull(result);
    assertEquals("test-article", result.getData().getSlug());
  }

  @Test
  void findArticleBySlug_returnsArticle() {
    setAuthenticatedUser(user);
    when(articleQueryService.findBySlug(eq("test-article"), eq(user)))
        .thenReturn(Optional.of(articleData));

    DataFetcherResult<Article> result = articleDatafetcher.findArticleBySlug("test-article");

    assertNotNull(result);
    assertEquals("test-article", result.getData().getSlug());
    assertEquals("description", result.getData().getDescription());
    assertTrue(result.getData().getTagList().contains("java"));
  }

  @Test
  void findArticleBySlug_notFound_throwsException() {
    setAuthenticatedUser(user);
    when(articleQueryService.findBySlug(eq("nonexistent"), eq(user))).thenReturn(Optional.empty());

    assertThrows(Exception.class, () -> articleDatafetcher.findArticleBySlug("nonexistent"));
  }

  @Test
  void getFeed_withoutAuth_usesNullUser() {
    setAnonymousUser();
    CursorPager<ArticleData> pager = new CursorPager<>(asList(articleData), Direction.NEXT, false);
    when(articleQueryService.findUserFeedWithCursor(eq(null), any())).thenReturn(pager);

    DataFetcherResult<ArticlesConnection> result =
        articleDatafetcher.getFeed(10, null, null, null, dgsEnv());

    assertNotNull(result);
  }

  @Test
  void getArticles_withCursors_parsesCorrectly() {
    setAuthenticatedUser(user);
    CursorPager<ArticleData> pager = new CursorPager<>(asList(articleData), Direction.NEXT, false);
    when(articleQueryService.findRecentArticlesWithCursor(
            eq(null), eq(null), eq(null), any(), eq(user)))
        .thenReturn(pager);

    String cursor = String.valueOf(new DateTime().getMillis());
    DataFetcherResult<ArticlesConnection> result =
        articleDatafetcher.getArticles(10, cursor, null, null, null, null, null, dgsEnv());

    assertNotNull(result);
  }

  @Test
  void getFeed_withHasPrevious_setsPageInfoCorrectly() {
    setAuthenticatedUser(user);
    CursorPager<ArticleData> pager = new CursorPager<>(asList(articleData), Direction.PREV, true);
    when(articleQueryService.findUserFeedWithCursor(eq(user), any())).thenReturn(pager);

    DataFetcherResult<ArticlesConnection> result =
        articleDatafetcher.getFeed(null, null, 5, null, dgsEnv());

    assertNotNull(result.getData().getPageInfo());
    assertFalse(result.getData().getPageInfo().isHasNextPage());
    assertTrue(result.getData().getPageInfo().isHasPreviousPage());
  }

  @Test
  void getArticle_setsLocalContext() {
    setAuthenticatedUser(user);
    io.spring.core.article.Article coreArticle =
        new io.spring.core.article.Article("Title", "desc", "body", emptyList(), user.getId());
    when(dataFetchingEnvironment.getLocalContext()).thenReturn(coreArticle);
    when(articleQueryService.findById(eq(coreArticle.getId()), eq(user)))
        .thenReturn(Optional.of(articleData));

    DataFetcherResult<Article> result = articleDatafetcher.getArticle(dataFetchingEnvironment);

    assertNotNull(result.getLocalContext());
  }

  @Test
  void buildArticleResult_setsAllFields() {
    setAuthenticatedUser(user);
    when(articleQueryService.findBySlug(eq("test-article"), eq(user)))
        .thenReturn(Optional.of(articleData));

    DataFetcherResult<Article> result = articleDatafetcher.findArticleBySlug("test-article");

    Article article = result.getData();
    assertEquals("Test Article", article.getTitle());
    assertEquals("description", article.getDescription());
    assertEquals("body", article.getBody());
    assertEquals("test-article", article.getSlug());
    assertFalse(article.getFavorited());
    assertEquals(5, article.getFavoritesCount());
    assertNotNull(article.getCreatedAt());
    assertNotNull(article.getUpdatedAt());
    assertEquals(2, article.getTagList().size());
  }
}
