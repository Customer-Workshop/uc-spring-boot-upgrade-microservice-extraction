package io.spring.graphql;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import com.netflix.graphql.dgs.DgsDataFetchingEnvironment;
import graphql.execution.DataFetcherResult;
import graphql.schema.DataFetchingEnvironment;
import io.spring.api.exception.ResourceNotFoundException;
import io.spring.application.ArticleQueryService;
import io.spring.application.CursorPager;
import io.spring.application.CursorPager.Direction;
import io.spring.application.DateTimeCursor;
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
  @Mock private DgsDataFetchingEnvironment dfe;
  @Mock private DataFetchingEnvironment dataFetchingEnvironment;

  private ArticleDatafetcher articleDatafetcher;
  private User user;
  private ArticleData articleData;
  private DateTime now;

  @BeforeEach
  void setUp() {
    articleDatafetcher = new ArticleDatafetcher(articleQueryService, userRepository);
    user = new User("test@test.com", "testuser", "password", "bio", "image");
    now = new DateTime();
    ProfileData profileData = new ProfileData(user.getId(), "testuser", "bio", "image", false);
    articleData =
        new ArticleData(
            "article-id",
            "test-title",
            "Test Title",
            "description",
            "body",
            false,
            0,
            now,
            now,
            Arrays.asList("java"),
            profileData);
  }

  private void setAuthentication(User user) {
    TestingAuthenticationToken auth = new TestingAuthenticationToken(user, null);
    SecurityContextHolder.getContext().setAuthentication(auth);
  }

  private void clearAuthentication() {
    SecurityContextHolder.clearContext();
  }

  private void setAnonymousAuthentication() {
    AnonymousAuthenticationToken anonymousAuth =
        new AnonymousAuthenticationToken(
            "key", "anonymous", AuthorityUtils.createAuthorityList("ROLE_ANONYMOUS"));
    SecurityContextHolder.getContext().setAuthentication(anonymousAuth);
  }

  @Test
  void should_get_feed_with_first_parameter() {
    setAuthentication(user);
    CursorPager<ArticleData> cursorPager =
        new CursorPager<>(Arrays.asList(articleData), Direction.NEXT, false);
    when(articleQueryService.findUserFeedWithCursor(any(User.class), any()))
        .thenReturn(cursorPager);

    DataFetcherResult<ArticlesConnection> result =
        articleDatafetcher.getFeed(10, null, null, null, dfe);

    assertNotNull(result);
    assertNotNull(result.getData());
    assertEquals(1, result.getData().getEdges().size());
    clearAuthentication();
  }

  @Test
  void should_get_feed_with_last_parameter() {
    setAuthentication(user);
    CursorPager<ArticleData> cursorPager =
        new CursorPager<>(Arrays.asList(articleData), Direction.PREV, false);
    when(articleQueryService.findUserFeedWithCursor(any(User.class), any()))
        .thenReturn(cursorPager);

    DataFetcherResult<ArticlesConnection> result =
        articleDatafetcher.getFeed(null, null, 10, null, dfe);

    assertNotNull(result);
    assertNotNull(result.getData());
    assertEquals(1, result.getData().getEdges().size());
    clearAuthentication();
  }

  @Test
  void should_throw_when_feed_missing_first_and_last() {
    setAuthentication(user);
    assertThrows(
        IllegalArgumentException.class,
        () -> articleDatafetcher.getFeed(null, null, null, null, dfe));
    clearAuthentication();
  }

  @Test
  void should_get_user_feed_with_first_parameter() {
    setAuthentication(user);
    Profile profile = Profile.newBuilder().username("testuser").build();
    when(dfe.getSource()).thenReturn(profile);
    when(userRepository.findByUsername(eq("testuser"))).thenReturn(Optional.of(user));

    CursorPager<ArticleData> cursorPager =
        new CursorPager<>(Arrays.asList(articleData), Direction.NEXT, true);
    when(articleQueryService.findUserFeedWithCursor(any(User.class), any()))
        .thenReturn(cursorPager);

    DataFetcherResult<ArticlesConnection> result =
        articleDatafetcher.userFeed(10, null, null, null, dfe);

    assertNotNull(result);
    assertEquals(1, result.getData().getEdges().size());
    assertTrue(result.getData().getPageInfo().isHasNextPage());
    clearAuthentication();
  }

  @Test
  void should_get_user_feed_with_last_parameter() {
    setAuthentication(user);
    Profile profile = Profile.newBuilder().username("testuser").build();
    when(dfe.getSource()).thenReturn(profile);
    when(userRepository.findByUsername(eq("testuser"))).thenReturn(Optional.of(user));

    CursorPager<ArticleData> cursorPager =
        new CursorPager<>(Arrays.asList(articleData), Direction.PREV, true);
    when(articleQueryService.findUserFeedWithCursor(any(User.class), any()))
        .thenReturn(cursorPager);

    DataFetcherResult<ArticlesConnection> result =
        articleDatafetcher.userFeed(null, null, 10, null, dfe);

    assertNotNull(result);
    assertTrue(result.getData().getPageInfo().isHasPreviousPage());
    clearAuthentication();
  }

  @Test
  void should_throw_when_user_feed_missing_first_and_last() {
    assertThrows(
        IllegalArgumentException.class,
        () -> articleDatafetcher.userFeed(null, null, null, null, dfe));
  }

  @Test
  void should_throw_when_user_feed_user_not_found() {
    Profile profile = Profile.newBuilder().username("nonexistent").build();
    when(dfe.getSource()).thenReturn(profile);
    when(userRepository.findByUsername(eq("nonexistent"))).thenReturn(Optional.empty());
    assertThrows(
        ResourceNotFoundException.class,
        () -> articleDatafetcher.userFeed(10, null, null, null, dfe));
  }

  @Test
  void should_get_user_favorites_with_first_parameter() {
    setAuthentication(user);
    Profile profile = Profile.newBuilder().username("testuser").build();
    when(dfe.getSource()).thenReturn(profile);

    CursorPager<ArticleData> cursorPager =
        new CursorPager<>(Arrays.asList(articleData), Direction.NEXT, false);
    when(articleQueryService.findRecentArticlesWithCursor(
            eq(null), eq(null), eq("testuser"), any(), any()))
        .thenReturn(cursorPager);

    DataFetcherResult<ArticlesConnection> result =
        articleDatafetcher.userFavorites(10, null, null, null, dfe);

    assertNotNull(result);
    assertEquals(1, result.getData().getEdges().size());
    clearAuthentication();
  }

  @Test
  void should_get_user_favorites_with_last_parameter() {
    setAuthentication(user);
    Profile profile = Profile.newBuilder().username("testuser").build();
    when(dfe.getSource()).thenReturn(profile);

    CursorPager<ArticleData> cursorPager =
        new CursorPager<>(Arrays.asList(articleData), Direction.PREV, false);
    when(articleQueryService.findRecentArticlesWithCursor(
            eq(null), eq(null), eq("testuser"), any(), any()))
        .thenReturn(cursorPager);

    DataFetcherResult<ArticlesConnection> result =
        articleDatafetcher.userFavorites(null, null, 5, null, dfe);

    assertNotNull(result);
    assertEquals(1, result.getData().getEdges().size());
    clearAuthentication();
  }

  @Test
  void should_throw_when_user_favorites_missing_first_and_last() {
    assertThrows(
        IllegalArgumentException.class,
        () -> articleDatafetcher.userFavorites(null, null, null, null, dfe));
  }

  @Test
  void should_get_user_articles_with_first_parameter() {
    setAuthentication(user);
    Profile profile = Profile.newBuilder().username("testuser").build();
    when(dfe.getSource()).thenReturn(profile);

    CursorPager<ArticleData> cursorPager =
        new CursorPager<>(Arrays.asList(articleData), Direction.NEXT, false);
    when(articleQueryService.findRecentArticlesWithCursor(
            eq(null), eq("testuser"), eq(null), any(), any()))
        .thenReturn(cursorPager);

    DataFetcherResult<ArticlesConnection> result =
        articleDatafetcher.userArticles(10, null, null, null, dfe);

    assertNotNull(result);
    assertEquals(1, result.getData().getEdges().size());
    clearAuthentication();
  }

  @Test
  void should_get_user_articles_with_last_parameter() {
    setAuthentication(user);
    Profile profile = Profile.newBuilder().username("testuser").build();
    when(dfe.getSource()).thenReturn(profile);

    CursorPager<ArticleData> cursorPager =
        new CursorPager<>(Arrays.asList(articleData), Direction.PREV, false);
    when(articleQueryService.findRecentArticlesWithCursor(
            eq(null), eq("testuser"), eq(null), any(), any()))
        .thenReturn(cursorPager);

    DataFetcherResult<ArticlesConnection> result =
        articleDatafetcher.userArticles(null, null, 5, null, dfe);

    assertNotNull(result);
    clearAuthentication();
  }

  @Test
  void should_throw_when_user_articles_missing_first_and_last() {
    assertThrows(
        IllegalArgumentException.class,
        () -> articleDatafetcher.userArticles(null, null, null, null, dfe));
  }

  @Test
  void should_get_articles_with_first_parameter() {
    setAuthentication(user);
    CursorPager<ArticleData> cursorPager =
        new CursorPager<>(Arrays.asList(articleData), Direction.NEXT, false);
    when(articleQueryService.findRecentArticlesWithCursor(
            eq("java"), eq("author"), eq("fav"), any(), any()))
        .thenReturn(cursorPager);

    DataFetcherResult<ArticlesConnection> result =
        articleDatafetcher.getArticles(10, null, null, null, "author", "fav", "java", dfe);

    assertNotNull(result);
    assertEquals(1, result.getData().getEdges().size());
    clearAuthentication();
  }

  @Test
  void should_get_articles_with_last_parameter() {
    setAuthentication(user);
    CursorPager<ArticleData> cursorPager =
        new CursorPager<>(Arrays.asList(articleData), Direction.PREV, false);
    when(articleQueryService.findRecentArticlesWithCursor(any(), any(), any(), any(), any()))
        .thenReturn(cursorPager);

    DataFetcherResult<ArticlesConnection> result =
        articleDatafetcher.getArticles(null, null, 10, null, null, null, null, dfe);

    assertNotNull(result);
    clearAuthentication();
  }

  @Test
  void should_throw_when_articles_missing_first_and_last() {
    assertThrows(
        IllegalArgumentException.class,
        () -> articleDatafetcher.getArticles(null, null, null, null, null, null, null, dfe));
  }

  @Test
  void should_get_articles_with_empty_result() {
    setAuthentication(user);
    CursorPager<ArticleData> cursorPager =
        new CursorPager<>(Collections.emptyList(), Direction.NEXT, false);
    when(articleQueryService.findRecentArticlesWithCursor(any(), any(), any(), any(), any()))
        .thenReturn(cursorPager);

    DataFetcherResult<ArticlesConnection> result =
        articleDatafetcher.getArticles(10, null, null, null, null, null, null, dfe);

    assertNotNull(result);
    assertTrue(result.getData().getEdges().isEmpty());
    assertNull(result.getData().getPageInfo().getStartCursor());
    assertNull(result.getData().getPageInfo().getEndCursor());
    clearAuthentication();
  }

  @Test
  void should_get_article_from_article_payload() {
    setAuthentication(user);
    io.spring.core.article.Article coreArticle =
        new io.spring.core.article.Article(
            "Test Title", "desc", "body", Arrays.asList("java"), user.getId());
    when(dataFetchingEnvironment.getLocalContext()).thenReturn(coreArticle);
    when(articleQueryService.findById(eq(coreArticle.getId()), any()))
        .thenReturn(Optional.of(articleData));

    DataFetcherResult<Article> result = articleDatafetcher.getArticle(dataFetchingEnvironment);

    assertNotNull(result);
    assertNotNull(result.getData());
    assertEquals("body", result.getData().getBody());
    clearAuthentication();
  }

  @Test
  void should_throw_when_article_payload_article_not_found() {
    setAuthentication(user);
    io.spring.core.article.Article coreArticle =
        new io.spring.core.article.Article(
            "Test Title", "desc", "body", Arrays.asList("java"), user.getId());
    when(dataFetchingEnvironment.getLocalContext()).thenReturn(coreArticle);
    when(articleQueryService.findById(eq(coreArticle.getId()), any())).thenReturn(Optional.empty());

    assertThrows(
        ResourceNotFoundException.class,
        () -> articleDatafetcher.getArticle(dataFetchingEnvironment));
    clearAuthentication();
  }

  @Test
  void should_get_comment_article() {
    setAuthentication(user);
    CommentData commentData =
        new CommentData("comment-id", "body", "article-id", now, now, null);
    when(dataFetchingEnvironment.getLocalContext()).thenReturn(commentData);
    when(articleQueryService.findById(eq("article-id"), any()))
        .thenReturn(Optional.of(articleData));

    DataFetcherResult<Article> result =
        articleDatafetcher.getCommentArticle(dataFetchingEnvironment);

    assertNotNull(result);
    assertNotNull(result.getData());
    clearAuthentication();
  }

  @Test
  void should_throw_when_comment_article_not_found() {
    setAuthentication(user);
    CommentData commentData =
        new CommentData("comment-id", "body", "article-id", now, now, null);
    when(dataFetchingEnvironment.getLocalContext()).thenReturn(commentData);
    when(articleQueryService.findById(eq("article-id"), any())).thenReturn(Optional.empty());

    assertThrows(
        ResourceNotFoundException.class,
        () -> articleDatafetcher.getCommentArticle(dataFetchingEnvironment));
    clearAuthentication();
  }

  @Test
  void should_find_article_by_slug() {
    setAuthentication(user);
    when(articleQueryService.findBySlug(eq("test-title"), any()))
        .thenReturn(Optional.of(articleData));

    DataFetcherResult<Article> result = articleDatafetcher.findArticleBySlug("test-title");

    assertNotNull(result);
    assertEquals("Test Title", result.getData().getTitle());
    assertEquals("body", result.getData().getBody());
    assertEquals("description", result.getData().getDescription());
    clearAuthentication();
  }

  @Test
  void should_throw_when_find_article_by_slug_not_found() {
    setAuthentication(user);
    when(articleQueryService.findBySlug(eq("non-existent"), any())).thenReturn(Optional.empty());

    assertThrows(
        ResourceNotFoundException.class,
        () -> articleDatafetcher.findArticleBySlug("non-existent"));
    clearAuthentication();
  }

  @Test
  void should_get_feed_without_authentication() {
    setAnonymousAuthentication();
    CursorPager<ArticleData> cursorPager =
        new CursorPager<>(Arrays.asList(articleData), Direction.NEXT, false);
    when(articleQueryService.findUserFeedWithCursor(eq(null), any())).thenReturn(cursorPager);

    DataFetcherResult<ArticlesConnection> result =
        articleDatafetcher.getFeed(10, null, null, null, dfe);

    assertNotNull(result);
  }

  @Test
  void should_get_feed_with_cursor_parameters() {
    setAuthentication(user);
    String afterCursor = String.valueOf(now.getMillis());
    CursorPager<ArticleData> cursorPager =
        new CursorPager<>(Arrays.asList(articleData), Direction.NEXT, false);
    when(articleQueryService.findUserFeedWithCursor(any(), any())).thenReturn(cursorPager);

    DataFetcherResult<ArticlesConnection> result =
        articleDatafetcher.getFeed(10, afterCursor, null, null, dfe);

    assertNotNull(result);
    assertNotNull(result.getData().getEdges().get(0).getCursor());
    clearAuthentication();
  }
}
