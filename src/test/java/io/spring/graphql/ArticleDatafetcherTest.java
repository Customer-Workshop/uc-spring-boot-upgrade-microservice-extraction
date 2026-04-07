package io.spring.graphql;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
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

  private ArticleDatafetcher articleDatafetcher;
  private User user;

  @BeforeEach
  void setUp() {
    articleDatafetcher = new ArticleDatafetcher(articleQueryService, userRepository);
    user = new User("test@test.com", "testuser", "password", "bio", "image");
    SecurityContextHolder.getContext()
        .setAuthentication(new TestingAuthenticationToken(user, null));
  }

  @AfterEach
  void tearDown() {
    SecurityContextHolder.clearContext();
  }

  @Test
  void should_get_feed_with_first_parameter() {
    DgsDataFetchingEnvironment dfe = mock(DgsDataFetchingEnvironment.class);
    ArticleData articleData = createArticleData("1");
    CursorPager<ArticleData> cursorPager =
        new CursorPager<>(Arrays.asList(articleData), Direction.NEXT, false);

    when(articleQueryService.findUserFeedWithCursor(any(), any())).thenReturn(cursorPager);

    DataFetcherResult<ArticlesConnection> result =
        articleDatafetcher.getFeed(10, null, null, null, dfe);

    assertNotNull(result);
    assertNotNull(result.getData());
    assertEquals(1, result.getData().getEdges().size());
    assertEquals("test-slug-1", result.getData().getEdges().get(0).getNode().getSlug());
  }

  @Test
  void should_get_feed_with_last_parameter() {
    DgsDataFetchingEnvironment dfe = mock(DgsDataFetchingEnvironment.class);
    ArticleData articleData = createArticleData("1");
    CursorPager<ArticleData> cursorPager =
        new CursorPager<>(Arrays.asList(articleData), Direction.PREV, false);

    when(articleQueryService.findUserFeedWithCursor(any(), any())).thenReturn(cursorPager);

    DataFetcherResult<ArticlesConnection> result =
        articleDatafetcher.getFeed(null, null, 10, null, dfe);

    assertNotNull(result);
    assertNotNull(result.getData());
    assertEquals(1, result.getData().getEdges().size());
  }

  @Test
  void should_throw_when_feed_has_no_first_or_last() {
    DgsDataFetchingEnvironment dfe = mock(DgsDataFetchingEnvironment.class);
    assertThrows(
        IllegalArgumentException.class,
        () -> articleDatafetcher.getFeed(null, null, null, null, dfe));
  }

  @Test
  void should_get_feed_with_empty_result() {
    DgsDataFetchingEnvironment dfe = mock(DgsDataFetchingEnvironment.class);
    CursorPager<ArticleData> cursorPager =
        new CursorPager<>(new ArrayList<>(), Direction.NEXT, false);

    when(articleQueryService.findUserFeedWithCursor(any(), any())).thenReturn(cursorPager);

    DataFetcherResult<ArticlesConnection> result =
        articleDatafetcher.getFeed(10, null, null, null, dfe);

    assertNotNull(result);
    assertEquals(0, result.getData().getEdges().size());
  }

  @Test
  void should_get_user_feed_with_first_parameter() {
    DgsDataFetchingEnvironment dfe = mock(DgsDataFetchingEnvironment.class);
    Profile profile = Profile.newBuilder().username("testuser").build();
    when(dfe.getSource()).thenReturn(profile);
    when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));

    ArticleData articleData = createArticleData("1");
    CursorPager<ArticleData> cursorPager =
        new CursorPager<>(Arrays.asList(articleData), Direction.NEXT, false);
    when(articleQueryService.findUserFeedWithCursor(any(), any())).thenReturn(cursorPager);

    DataFetcherResult<ArticlesConnection> result =
        articleDatafetcher.userFeed(10, null, null, null, dfe);

    assertNotNull(result);
    assertEquals(1, result.getData().getEdges().size());
  }

  @Test
  void should_get_user_feed_with_last_parameter() {
    DgsDataFetchingEnvironment dfe = mock(DgsDataFetchingEnvironment.class);
    Profile profile = Profile.newBuilder().username("testuser").build();
    when(dfe.getSource()).thenReturn(profile);
    when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));

    ArticleData articleData = createArticleData("1");
    CursorPager<ArticleData> cursorPager =
        new CursorPager<>(Arrays.asList(articleData), Direction.PREV, false);
    when(articleQueryService.findUserFeedWithCursor(any(), any())).thenReturn(cursorPager);

    DataFetcherResult<ArticlesConnection> result =
        articleDatafetcher.userFeed(null, null, 5, null, dfe);

    assertNotNull(result);
    assertEquals(1, result.getData().getEdges().size());
  }

  @Test
  void should_throw_when_user_feed_has_no_first_or_last() {
    DgsDataFetchingEnvironment dfe = mock(DgsDataFetchingEnvironment.class);
    assertThrows(
        IllegalArgumentException.class,
        () -> articleDatafetcher.userFeed(null, null, null, null, dfe));
  }

  @Test
  void should_throw_when_user_feed_user_not_found() {
    DgsDataFetchingEnvironment dfe = mock(DgsDataFetchingEnvironment.class);
    Profile profile = Profile.newBuilder().username("nonexistent").build();
    when(dfe.getSource()).thenReturn(profile);
    when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

    assertThrows(
        ResourceNotFoundException.class,
        () -> articleDatafetcher.userFeed(10, null, null, null, dfe));
  }

  @Test
  void should_get_user_favorites_with_first_parameter() {
    DgsDataFetchingEnvironment dfe = mock(DgsDataFetchingEnvironment.class);
    Profile profile = Profile.newBuilder().username("testuser").build();
    when(dfe.getSource()).thenReturn(profile);

    ArticleData articleData = createArticleData("1");
    CursorPager<ArticleData> cursorPager =
        new CursorPager<>(Arrays.asList(articleData), Direction.NEXT, false);
    when(articleQueryService.findRecentArticlesWithCursor(any(), any(), any(), any(), any()))
        .thenReturn(cursorPager);

    DataFetcherResult<ArticlesConnection> result =
        articleDatafetcher.userFavorites(10, null, null, null, dfe);

    assertNotNull(result);
    assertEquals(1, result.getData().getEdges().size());
  }

  @Test
  void should_get_user_favorites_with_last_parameter() {
    DgsDataFetchingEnvironment dfe = mock(DgsDataFetchingEnvironment.class);
    Profile profile = Profile.newBuilder().username("testuser").build();
    when(dfe.getSource()).thenReturn(profile);

    ArticleData articleData = createArticleData("1");
    CursorPager<ArticleData> cursorPager =
        new CursorPager<>(Arrays.asList(articleData), Direction.PREV, false);
    when(articleQueryService.findRecentArticlesWithCursor(any(), any(), any(), any(), any()))
        .thenReturn(cursorPager);

    DataFetcherResult<ArticlesConnection> result =
        articleDatafetcher.userFavorites(null, null, 5, null, dfe);

    assertNotNull(result);
    assertEquals(1, result.getData().getEdges().size());
  }

  @Test
  void should_throw_when_user_favorites_has_no_first_or_last() {
    DgsDataFetchingEnvironment dfe = mock(DgsDataFetchingEnvironment.class);
    assertThrows(
        IllegalArgumentException.class,
        () -> articleDatafetcher.userFavorites(null, null, null, null, dfe));
  }

  @Test
  void should_get_user_articles_with_first_parameter() {
    DgsDataFetchingEnvironment dfe = mock(DgsDataFetchingEnvironment.class);
    Profile profile = Profile.newBuilder().username("testuser").build();
    when(dfe.getSource()).thenReturn(profile);

    ArticleData articleData = createArticleData("1");
    CursorPager<ArticleData> cursorPager =
        new CursorPager<>(Arrays.asList(articleData), Direction.NEXT, false);
    when(articleQueryService.findRecentArticlesWithCursor(any(), any(), any(), any(), any()))
        .thenReturn(cursorPager);

    DataFetcherResult<ArticlesConnection> result =
        articleDatafetcher.userArticles(10, null, null, null, dfe);

    assertNotNull(result);
    assertEquals(1, result.getData().getEdges().size());
  }

  @Test
  void should_get_user_articles_with_last_parameter() {
    DgsDataFetchingEnvironment dfe = mock(DgsDataFetchingEnvironment.class);
    Profile profile = Profile.newBuilder().username("testuser").build();
    when(dfe.getSource()).thenReturn(profile);

    ArticleData articleData = createArticleData("1");
    CursorPager<ArticleData> cursorPager =
        new CursorPager<>(Arrays.asList(articleData), Direction.PREV, false);
    when(articleQueryService.findRecentArticlesWithCursor(any(), any(), any(), any(), any()))
        .thenReturn(cursorPager);

    DataFetcherResult<ArticlesConnection> result =
        articleDatafetcher.userArticles(null, null, 5, null, dfe);

    assertNotNull(result);
    assertEquals(1, result.getData().getEdges().size());
  }

  @Test
  void should_throw_when_user_articles_has_no_first_or_last() {
    DgsDataFetchingEnvironment dfe = mock(DgsDataFetchingEnvironment.class);
    assertThrows(
        IllegalArgumentException.class,
        () -> articleDatafetcher.userArticles(null, null, null, null, dfe));
  }

  @Test
  void should_get_articles_with_first_parameter() {
    DgsDataFetchingEnvironment dfe = mock(DgsDataFetchingEnvironment.class);
    ArticleData articleData = createArticleData("1");
    CursorPager<ArticleData> cursorPager =
        new CursorPager<>(Arrays.asList(articleData), Direction.NEXT, false);
    when(articleQueryService.findRecentArticlesWithCursor(any(), any(), any(), any(), any()))
        .thenReturn(cursorPager);

    DataFetcherResult<ArticlesConnection> result =
        articleDatafetcher.getArticles(10, null, null, null, "author", "fav", "tag", dfe);

    assertNotNull(result);
    assertEquals(1, result.getData().getEdges().size());
  }

  @Test
  void should_get_articles_with_last_parameter() {
    DgsDataFetchingEnvironment dfe = mock(DgsDataFetchingEnvironment.class);
    ArticleData articleData = createArticleData("1");
    CursorPager<ArticleData> cursorPager =
        new CursorPager<>(Arrays.asList(articleData), Direction.PREV, false);
    when(articleQueryService.findRecentArticlesWithCursor(any(), any(), any(), any(), any()))
        .thenReturn(cursorPager);

    DataFetcherResult<ArticlesConnection> result =
        articleDatafetcher.getArticles(null, null, 5, null, null, null, null, dfe);

    assertNotNull(result);
    assertEquals(1, result.getData().getEdges().size());
  }

  @Test
  void should_throw_when_articles_has_no_first_or_last() {
    DgsDataFetchingEnvironment dfe = mock(DgsDataFetchingEnvironment.class);
    assertThrows(
        IllegalArgumentException.class,
        () -> articleDatafetcher.getArticles(null, null, null, null, null, null, null, dfe));
  }

  @Test
  void should_get_article_from_article_payload() {
    DataFetchingEnvironment dfe = mock(DataFetchingEnvironment.class);
    io.spring.core.article.Article coreArticle =
        new io.spring.core.article.Article("title", "desc", "body", new ArrayList<>(), user.getId());
    when(dfe.getLocalContext()).thenReturn(coreArticle);

    ArticleData articleData = createArticleData("1");
    when(articleQueryService.findById(eq(coreArticle.getId()), any()))
        .thenReturn(Optional.of(articleData));

    DataFetcherResult<Article> result = articleDatafetcher.getArticle(dfe);

    assertNotNull(result);
    assertNotNull(result.getData());
    assertEquals("test-slug-1", result.getData().getSlug());
  }

  @Test
  void should_throw_when_article_payload_article_not_found() {
    DataFetchingEnvironment dfe = mock(DataFetchingEnvironment.class);
    io.spring.core.article.Article coreArticle =
        new io.spring.core.article.Article("title", "desc", "body", new ArrayList<>(), user.getId());
    when(dfe.getLocalContext()).thenReturn(coreArticle);
    when(articleQueryService.findById(eq(coreArticle.getId()), any()))
        .thenReturn(Optional.empty());

    assertThrows(ResourceNotFoundException.class, () -> articleDatafetcher.getArticle(dfe));
  }

  @Test
  void should_get_comment_article() {
    DataFetchingEnvironment dfe = mock(DataFetchingEnvironment.class);
    DateTime now = new DateTime();
    CommentData commentData =
        new CommentData(
            "comment1",
            "body",
            "article1",
            now,
            now,
            new ProfileData("userId", "username", "bio", "image", false));
    when(dfe.getLocalContext()).thenReturn(commentData);

    ArticleData articleData = createArticleData("1");
    when(articleQueryService.findById(eq("article1"), any())).thenReturn(Optional.of(articleData));

    DataFetcherResult<Article> result = articleDatafetcher.getCommentArticle(dfe);

    assertNotNull(result);
    assertNotNull(result.getData());
    assertEquals("test-slug-1", result.getData().getSlug());
  }

  @Test
  void should_throw_when_comment_article_not_found() {
    DataFetchingEnvironment dfe = mock(DataFetchingEnvironment.class);
    DateTime now = new DateTime();
    CommentData commentData =
        new CommentData(
            "comment1",
            "body",
            "article1",
            now,
            now,
            new ProfileData("userId", "username", "bio", "image", false));
    when(dfe.getLocalContext()).thenReturn(commentData);
    when(articleQueryService.findById(eq("article1"), any())).thenReturn(Optional.empty());

    assertThrows(ResourceNotFoundException.class, () -> articleDatafetcher.getCommentArticle(dfe));
  }

  @Test
  void should_find_article_by_slug() {
    ArticleData articleData = createArticleData("1");
    when(articleQueryService.findBySlug(eq("test-slug-1"), any()))
        .thenReturn(Optional.of(articleData));

    DataFetcherResult<Article> result = articleDatafetcher.findArticleBySlug("test-slug-1");

    assertNotNull(result);
    assertNotNull(result.getData());
    assertEquals("test-slug-1", result.getData().getSlug());
    assertEquals("title-1", result.getData().getTitle());
    assertEquals("body-1", result.getData().getBody());
    assertEquals("desc-1", result.getData().getDescription());
  }

  @Test
  void should_throw_when_find_article_by_slug_not_found() {
    when(articleQueryService.findBySlug(eq("not-found"), any())).thenReturn(Optional.empty());

    assertThrows(
        ResourceNotFoundException.class,
        () -> articleDatafetcher.findArticleBySlug("not-found"));
  }

  @Test
  void should_get_feed_with_has_next_page() {
    DgsDataFetchingEnvironment dfe = mock(DgsDataFetchingEnvironment.class);
    ArticleData articleData = createArticleData("1");
    CursorPager<ArticleData> cursorPager =
        new CursorPager<>(Arrays.asList(articleData), Direction.NEXT, true);

    when(articleQueryService.findUserFeedWithCursor(any(), any())).thenReturn(cursorPager);

    DataFetcherResult<ArticlesConnection> result =
        articleDatafetcher.getFeed(10, null, null, null, dfe);

    assertNotNull(result);
    assertNotNull(result.getData().getPageInfo());
  }

  @Test
  void should_get_articles_with_cursor_params() {
    DgsDataFetchingEnvironment dfe = mock(DgsDataFetchingEnvironment.class);
    ArticleData articleData = createArticleData("1");
    CursorPager<ArticleData> cursorPager =
        new CursorPager<>(Arrays.asList(articleData), Direction.NEXT, false);
    when(articleQueryService.findRecentArticlesWithCursor(any(), any(), any(), any(), any()))
        .thenReturn(cursorPager);

    String afterCursor = String.valueOf(new DateTime().getMillis());
    DataFetcherResult<ArticlesConnection> result =
        articleDatafetcher.getArticles(10, afterCursor, null, null, null, null, null, dfe);

    assertNotNull(result);
    assertEquals(1, result.getData().getEdges().size());
  }

  @Test
  void should_get_feed_without_authentication() {
    SecurityContextHolder.getContext()
        .setAuthentication(
            new AnonymousAuthenticationToken(
                "key", "anonymous", AuthorityUtils.createAuthorityList("ROLE_ANONYMOUS")));
    DgsDataFetchingEnvironment dfe = mock(DgsDataFetchingEnvironment.class);
    ArticleData articleData = createArticleData("1");
    CursorPager<ArticleData> cursorPager =
        new CursorPager<>(Arrays.asList(articleData), Direction.NEXT, false);
    when(articleQueryService.findUserFeedWithCursor(any(), any())).thenReturn(cursorPager);

    DataFetcherResult<ArticlesConnection> result =
        articleDatafetcher.getFeed(10, null, null, null, dfe);

    assertNotNull(result);
  }

  private ArticleData createArticleData(String seed) {
    DateTime now = new DateTime();
    return new ArticleData(
        "id-" + seed,
        "test-slug-" + seed,
        "title-" + seed,
        "desc-" + seed,
        "body-" + seed,
        false,
        0,
        now,
        now,
        Arrays.asList("tag1"),
        new ProfileData(user.getId(), user.getUsername(), user.getBio(), user.getImage(), false));
  }
}
