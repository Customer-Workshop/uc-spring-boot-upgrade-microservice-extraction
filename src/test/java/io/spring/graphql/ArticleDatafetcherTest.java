package io.spring.graphql;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
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
import io.spring.core.user.User;
import io.spring.core.user.UserRepository;
import io.spring.graphql.types.Article;
import io.spring.graphql.types.ArticlesConnection;
import io.spring.graphql.types.Profile;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import org.joda.time.DateTime;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
public class ArticleDatafetcherTest {

  @Mock private ArticleQueryService articleQueryService;
  @Mock private UserRepository userRepository;

  private ArticleDatafetcher articleDatafetcher;
  private User user;
  private ArticleData articleData;
  private DateTime now;

  @BeforeEach
  void setUp() {
    articleDatafetcher = new ArticleDatafetcher(articleQueryService, userRepository);
    user = new User("test@test.com", "testuser", "password", "bio", "image");
    now = new DateTime();
    articleData =
        new ArticleData(
            "article-id",
            "test-article",
            "Test Article",
            "desc",
            "body",
            false,
            0,
            now,
            now,
            Arrays.asList("java"),
            new ProfileData(
                user.getId(), user.getUsername(), user.getBio(), user.getImage(), false));
    SecurityContextHolder.getContext()
        .setAuthentication(new TestingAuthenticationToken(user, null));
  }

  @AfterEach
  void tearDown() {
    SecurityContextHolder.clearContext();
  }

  @Test
  void getFeed_withFirst_returnsArticlesConnection() {
    CursorPager<ArticleData> pager =
        new CursorPager<>(Arrays.asList(articleData), Direction.NEXT, false);
    when(articleQueryService.findUserFeedWithCursor(any(User.class), any())).thenReturn(pager);
    DgsDataFetchingEnvironment dfe = mock(DgsDataFetchingEnvironment.class);

    DataFetcherResult<ArticlesConnection> result =
        articleDatafetcher.getFeed(10, null, null, null, dfe);

    assertNotNull(result);
    assertNotNull(result.getData());
    assertEquals(1, result.getData().getEdges().size());
    assertEquals("test-article", result.getData().getEdges().get(0).getNode().getSlug());
  }

  @Test
  void getFeed_withLast_returnsArticlesConnection() {
    CursorPager<ArticleData> pager =
        new CursorPager<>(Arrays.asList(articleData), Direction.PREV, false);
    when(articleQueryService.findUserFeedWithCursor(any(User.class), any())).thenReturn(pager);
    DgsDataFetchingEnvironment dfe = mock(DgsDataFetchingEnvironment.class);

    DataFetcherResult<ArticlesConnection> result =
        articleDatafetcher.getFeed(null, null, 5, null, dfe);

    assertNotNull(result);
    assertEquals(1, result.getData().getEdges().size());
  }

  @Test
  void getFeed_withNullFirstAndLast_throwsException() {
    DgsDataFetchingEnvironment dfe = mock(DgsDataFetchingEnvironment.class);

    assertThrows(
        IllegalArgumentException.class,
        () -> articleDatafetcher.getFeed(null, null, null, null, dfe));
  }

  @Test
  void getFeed_withEmptyResult_returnsEmptyConnection() {
    CursorPager<ArticleData> pager =
        new CursorPager<>(Collections.emptyList(), Direction.NEXT, false);
    when(articleQueryService.findUserFeedWithCursor(any(User.class), any())).thenReturn(pager);
    DgsDataFetchingEnvironment dfe = mock(DgsDataFetchingEnvironment.class);

    DataFetcherResult<ArticlesConnection> result =
        articleDatafetcher.getFeed(10, null, null, null, dfe);

    assertNotNull(result);
    assertTrue(result.getData().getEdges().isEmpty());
  }

  @Test
  void userFeed_withFirst_returnsArticlesConnection() {
    CursorPager<ArticleData> pager =
        new CursorPager<>(Arrays.asList(articleData), Direction.NEXT, true);
    when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
    when(articleQueryService.findUserFeedWithCursor(any(User.class), any())).thenReturn(pager);

    DgsDataFetchingEnvironment dfe = mock(DgsDataFetchingEnvironment.class);
    Profile profile = Profile.newBuilder().username("testuser").build();
    when(dfe.getSource()).thenReturn(profile);

    DataFetcherResult<ArticlesConnection> result =
        articleDatafetcher.userFeed(10, null, null, null, dfe);

    assertNotNull(result);
    assertEquals(1, result.getData().getEdges().size());
    assertTrue(result.getData().getPageInfo().isHasNextPage());
  }

  @Test
  void userFeed_withLast_returnsArticlesConnection() {
    CursorPager<ArticleData> pager =
        new CursorPager<>(Arrays.asList(articleData), Direction.PREV, false);
    when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
    when(articleQueryService.findUserFeedWithCursor(any(User.class), any())).thenReturn(pager);

    DgsDataFetchingEnvironment dfe = mock(DgsDataFetchingEnvironment.class);
    Profile profile = Profile.newBuilder().username("testuser").build();
    when(dfe.getSource()).thenReturn(profile);

    DataFetcherResult<ArticlesConnection> result =
        articleDatafetcher.userFeed(null, null, 5, null, dfe);

    assertNotNull(result);
    assertEquals(1, result.getData().getEdges().size());
  }

  @Test
  void userFeed_withNullFirstAndLast_throwsException() {
    DgsDataFetchingEnvironment dfe = mock(DgsDataFetchingEnvironment.class);

    assertThrows(
        IllegalArgumentException.class,
        () -> articleDatafetcher.userFeed(null, null, null, null, dfe));
  }

  @Test
  void userFeed_userNotFound_throwsResourceNotFoundException() {
    when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());
    DgsDataFetchingEnvironment dfe = mock(DgsDataFetchingEnvironment.class);
    Profile profile = Profile.newBuilder().username("unknown").build();
    when(dfe.getSource()).thenReturn(profile);

    assertThrows(
        ResourceNotFoundException.class,
        () -> articleDatafetcher.userFeed(10, null, null, null, dfe));
  }

  @Test
  void userFavorites_withFirst_returnsArticlesConnection() {
    CursorPager<ArticleData> pager =
        new CursorPager<>(Arrays.asList(articleData), Direction.NEXT, false);
    when(articleQueryService.findRecentArticlesWithCursor(
            isNull(), isNull(), eq("testuser"), any(), any(User.class)))
        .thenReturn(pager);

    DgsDataFetchingEnvironment dfe = mock(DgsDataFetchingEnvironment.class);
    Profile profile = Profile.newBuilder().username("testuser").build();
    when(dfe.getSource()).thenReturn(profile);

    DataFetcherResult<ArticlesConnection> result =
        articleDatafetcher.userFavorites(10, null, null, null, dfe);

    assertNotNull(result);
    assertEquals(1, result.getData().getEdges().size());
  }

  @Test
  void userFavorites_withLast_returnsArticlesConnection() {
    CursorPager<ArticleData> pager =
        new CursorPager<>(Arrays.asList(articleData), Direction.PREV, false);
    when(articleQueryService.findRecentArticlesWithCursor(
            isNull(), isNull(), eq("testuser"), any(), any(User.class)))
        .thenReturn(pager);

    DgsDataFetchingEnvironment dfe = mock(DgsDataFetchingEnvironment.class);
    Profile profile = Profile.newBuilder().username("testuser").build();
    when(dfe.getSource()).thenReturn(profile);

    DataFetcherResult<ArticlesConnection> result =
        articleDatafetcher.userFavorites(null, null, 5, null, dfe);

    assertNotNull(result);
    assertEquals(1, result.getData().getEdges().size());
  }

  @Test
  void userFavorites_withNullFirstAndLast_throwsException() {
    DgsDataFetchingEnvironment dfe = mock(DgsDataFetchingEnvironment.class);

    assertThrows(
        IllegalArgumentException.class,
        () -> articleDatafetcher.userFavorites(null, null, null, null, dfe));
  }

  @Test
  void userArticles_withFirst_returnsArticlesConnection() {
    CursorPager<ArticleData> pager =
        new CursorPager<>(Arrays.asList(articleData), Direction.NEXT, false);
    when(articleQueryService.findRecentArticlesWithCursor(
            isNull(), eq("testuser"), isNull(), any(), any(User.class)))
        .thenReturn(pager);

    DgsDataFetchingEnvironment dfe = mock(DgsDataFetchingEnvironment.class);
    Profile profile = Profile.newBuilder().username("testuser").build();
    when(dfe.getSource()).thenReturn(profile);

    DataFetcherResult<ArticlesConnection> result =
        articleDatafetcher.userArticles(10, null, null, null, dfe);

    assertNotNull(result);
    assertEquals(1, result.getData().getEdges().size());
  }

  @Test
  void userArticles_withLast_returnsArticlesConnection() {
    CursorPager<ArticleData> pager =
        new CursorPager<>(Arrays.asList(articleData), Direction.PREV, false);
    when(articleQueryService.findRecentArticlesWithCursor(
            isNull(), eq("testuser"), isNull(), any(), any(User.class)))
        .thenReturn(pager);

    DgsDataFetchingEnvironment dfe = mock(DgsDataFetchingEnvironment.class);
    Profile profile = Profile.newBuilder().username("testuser").build();
    when(dfe.getSource()).thenReturn(profile);

    DataFetcherResult<ArticlesConnection> result =
        articleDatafetcher.userArticles(null, null, 5, null, dfe);

    assertNotNull(result);
    assertEquals(1, result.getData().getEdges().size());
  }

  @Test
  void userArticles_withNullFirstAndLast_throwsException() {
    DgsDataFetchingEnvironment dfe = mock(DgsDataFetchingEnvironment.class);

    assertThrows(
        IllegalArgumentException.class,
        () -> articleDatafetcher.userArticles(null, null, null, null, dfe));
  }

  @Test
  void getArticles_withFirst_returnsArticlesConnection() {
    CursorPager<ArticleData> pager =
        new CursorPager<>(Arrays.asList(articleData), Direction.NEXT, false);
    when(articleQueryService.findRecentArticlesWithCursor(any(), any(), any(), any(), any()))
        .thenReturn(pager);
    DgsDataFetchingEnvironment dfe = mock(DgsDataFetchingEnvironment.class);

    DataFetcherResult<ArticlesConnection> result =
        articleDatafetcher.getArticles(10, null, null, null, "author", "fav", "java", dfe);

    assertNotNull(result);
    assertEquals(1, result.getData().getEdges().size());
  }

  @Test
  void getArticles_withLast_returnsArticlesConnection() {
    CursorPager<ArticleData> pager =
        new CursorPager<>(Arrays.asList(articleData), Direction.PREV, false);
    when(articleQueryService.findRecentArticlesWithCursor(any(), any(), any(), any(), any()))
        .thenReturn(pager);
    DgsDataFetchingEnvironment dfe = mock(DgsDataFetchingEnvironment.class);

    DataFetcherResult<ArticlesConnection> result =
        articleDatafetcher.getArticles(null, null, 5, null, null, null, null, dfe);

    assertNotNull(result);
    assertEquals(1, result.getData().getEdges().size());
  }

  @Test
  void getArticles_withNullFirstAndLast_throwsException() {
    DgsDataFetchingEnvironment dfe = mock(DgsDataFetchingEnvironment.class);

    assertThrows(
        IllegalArgumentException.class,
        () -> articleDatafetcher.getArticles(null, null, null, null, null, null, null, dfe));
  }

  @Test
  void getArticle_fromPayload_returnsArticle() {
    io.spring.core.article.Article coreArticle =
        new io.spring.core.article.Article(
            "Test", "desc", "body", Arrays.asList("java"), user.getId());
    when(articleQueryService.findById(eq(coreArticle.getId()), any()))
        .thenReturn(Optional.of(articleData));

    DataFetchingEnvironment dfe = mock(DataFetchingEnvironment.class);
    when(dfe.getLocalContext()).thenReturn(coreArticle);

    DataFetcherResult<Article> result = articleDatafetcher.getArticle(dfe);

    assertNotNull(result);
    assertNotNull(result.getData());
    assertEquals("test-article", result.getData().getSlug());
  }

  @Test
  void getArticle_notFound_throwsResourceNotFoundException() {
    io.spring.core.article.Article coreArticle =
        new io.spring.core.article.Article(
            "Test", "desc", "body", Arrays.asList("java"), user.getId());
    when(articleQueryService.findById(eq(coreArticle.getId()), any())).thenReturn(Optional.empty());

    DataFetchingEnvironment dfe = mock(DataFetchingEnvironment.class);
    when(dfe.getLocalContext()).thenReturn(coreArticle);

    assertThrows(ResourceNotFoundException.class, () -> articleDatafetcher.getArticle(dfe));
  }

  @Test
  void getCommentArticle_returnsArticle() {
    CommentData commentData = new CommentData("comment-id", "body", "article-id", now, now, null);
    when(articleQueryService.findById(eq("article-id"), any()))
        .thenReturn(Optional.of(articleData));

    DataFetchingEnvironment dfe = mock(DataFetchingEnvironment.class);
    when(dfe.getLocalContext()).thenReturn(commentData);

    DataFetcherResult<Article> result = articleDatafetcher.getCommentArticle(dfe);

    assertNotNull(result);
    assertEquals("test-article", result.getData().getSlug());
  }

  @Test
  void getCommentArticle_notFound_throwsResourceNotFoundException() {
    CommentData commentData = new CommentData("comment-id", "body", "article-id", now, now, null);
    when(articleQueryService.findById(eq("article-id"), any())).thenReturn(Optional.empty());

    DataFetchingEnvironment dfe = mock(DataFetchingEnvironment.class);
    when(dfe.getLocalContext()).thenReturn(commentData);

    assertThrows(ResourceNotFoundException.class, () -> articleDatafetcher.getCommentArticle(dfe));
  }

  @Test
  void findArticleBySlug_returnsArticle() {
    when(articleQueryService.findBySlug(eq("test-article"), any()))
        .thenReturn(Optional.of(articleData));

    DataFetcherResult<Article> result = articleDatafetcher.findArticleBySlug("test-article");

    assertNotNull(result);
    assertEquals("test-article", result.getData().getSlug());
    assertEquals("Test Article", result.getData().getTitle());
    assertEquals("body", result.getData().getBody());
    assertEquals("desc", result.getData().getDescription());
  }

  @Test
  void findArticleBySlug_notFound_throwsResourceNotFoundException() {
    when(articleQueryService.findBySlug(eq("nonexistent"), any())).thenReturn(Optional.empty());

    assertThrows(
        ResourceNotFoundException.class, () -> articleDatafetcher.findArticleBySlug("nonexistent"));
  }

  @Test
  void getFeed_withAnonymousUser_works() {
    SecurityContextHolder.getContext()
        .setAuthentication(
            new org.springframework.security.authentication.AnonymousAuthenticationToken(
                "key",
                "anonymous",
                java.util.Collections.singletonList(
                    new org.springframework.security.core.authority.SimpleGrantedAuthority(
                        "ROLE_ANONYMOUS"))));
    CursorPager<ArticleData> pager =
        new CursorPager<>(Collections.emptyList(), Direction.NEXT, false);
    when(articleQueryService.findUserFeedWithCursor(isNull(), any())).thenReturn(pager);
    DgsDataFetchingEnvironment dfe = mock(DgsDataFetchingEnvironment.class);

    DataFetcherResult<ArticlesConnection> result =
        articleDatafetcher.getFeed(10, null, null, null, dfe);

    assertNotNull(result);
  }

  @Test
  void getArticles_withCursorPaging_hasCorrectPageInfo() {
    CursorPager<ArticleData> pager =
        new CursorPager<>(Arrays.asList(articleData), Direction.NEXT, true);
    when(articleQueryService.findRecentArticlesWithCursor(any(), any(), any(), any(), any()))
        .thenReturn(pager);
    DgsDataFetchingEnvironment dfe = mock(DgsDataFetchingEnvironment.class);

    DataFetcherResult<ArticlesConnection> result =
        articleDatafetcher.getArticles(10, null, null, null, null, null, null, dfe);

    assertNotNull(result.getData().getPageInfo());
    assertTrue(result.getData().getPageInfo().isHasNextPage());
    assertFalse(result.getData().getPageInfo().isHasPreviousPage());
  }

  @Test
  void buildArticleResult_populatesAllFields() {
    when(articleQueryService.findBySlug(eq("test-article"), any()))
        .thenReturn(Optional.of(articleData));

    DataFetcherResult<Article> result = articleDatafetcher.findArticleBySlug("test-article");
    Article article = result.getData();

    assertEquals("body", article.getBody());
    assertEquals("desc", article.getDescription());
    assertFalse(article.getFavorited());
    assertEquals(0, article.getFavoritesCount());
    assertNotNull(article.getCreatedAt());
    assertNotNull(article.getUpdatedAt());
    assertEquals(Arrays.asList("java"), article.getTagList());
  }
}
