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
import io.spring.application.data.ArticleData;
import io.spring.application.data.CommentData;
import io.spring.application.data.ProfileData;
import io.spring.core.user.User;
import io.spring.core.user.UserRepository;
import io.spring.graphql.types.Article;
import io.spring.graphql.types.ArticlesConnection;
import io.spring.graphql.types.Profile;
import java.util.*;
import org.joda.time.DateTime;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
public class ArticleDatafetcherTest {

  @Mock private ArticleQueryService articleQueryService;
  @Mock private UserRepository userRepository;
  @Mock private DgsDataFetchingEnvironment dfe;
  @Mock private DataFetchingEnvironment rawDfe;

  @InjectMocks private ArticleDatafetcher articleDatafetcher;

  private User user;
  private ArticleData articleData;
  private ProfileData profileData;

  @BeforeEach
  void setUp() {
    user = new User("test@test.com", "testuser", "password", "bio", "image");
    profileData = new ProfileData(user.getId(), "testuser", "bio", "image", false);
    articleData =
        new ArticleData(
            "articleId",
            "test-slug",
            "Test Title",
            "desc",
            "body",
            false,
            0,
            new DateTime(),
            new DateTime(),
            Arrays.asList("java"),
            profileData);
    SecurityContextHolder.getContext()
        .setAuthentication(new TestingAuthenticationToken(user, null));
  }

  @AfterEach
  void tearDown() {
    SecurityContextHolder.clearContext();
  }

  @Test
  void should_get_feed_with_first_parameter() {
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
    CursorPager<ArticleData> pager =
        new CursorPager<>(Arrays.asList(articleData), Direction.PREV, false);

    when(articleQueryService.findUserFeedWithCursor(eq(user), any())).thenReturn(pager);

    DataFetcherResult<ArticlesConnection> result =
        articleDatafetcher.getFeed(null, null, 10, null, dfe);

    assertNotNull(result);
    assertEquals(1, result.getData().getEdges().size());
  }

  @Test
  void should_fail_get_feed_when_neither_first_nor_last() {
    assertThrows(
        IllegalArgumentException.class,
        () -> articleDatafetcher.getFeed(null, null, null, null, dfe));
  }

  @Test
  void should_get_feed_with_empty_result() {
    CursorPager<ArticleData> pager = new CursorPager<>(new ArrayList<>(), Direction.NEXT, false);

    when(articleQueryService.findUserFeedWithCursor(eq(user), any())).thenReturn(pager);

    DataFetcherResult<ArticlesConnection> result =
        articleDatafetcher.getFeed(10, null, null, null, dfe);

    assertNotNull(result);
    assertEquals(0, result.getData().getEdges().size());
  }

  @Test
  void should_get_articles_with_first_parameter() {
    CursorPager<ArticleData> pager =
        new CursorPager<>(Arrays.asList(articleData), Direction.NEXT, true);

    when(articleQueryService.findRecentArticlesWithCursor(any(), any(), any(), any(), any()))
        .thenReturn(pager);

    DataFetcherResult<ArticlesConnection> result =
        articleDatafetcher.getArticles(10, null, null, null, null, null, null, dfe);

    assertNotNull(result);
    assertEquals(1, result.getData().getEdges().size());
    assertTrue(result.getData().getPageInfo().isHasNextPage());
  }

  @Test
  void should_get_articles_with_last_parameter() {
    CursorPager<ArticleData> pager =
        new CursorPager<>(Arrays.asList(articleData), Direction.PREV, true);

    when(articleQueryService.findRecentArticlesWithCursor(any(), any(), any(), any(), any()))
        .thenReturn(pager);

    DataFetcherResult<ArticlesConnection> result =
        articleDatafetcher.getArticles(null, null, 10, null, null, null, null, dfe);

    assertNotNull(result);
    assertEquals(1, result.getData().getEdges().size());
  }

  @Test
  void should_fail_get_articles_when_neither_first_nor_last() {
    assertThrows(
        IllegalArgumentException.class,
        () -> articleDatafetcher.getArticles(null, null, null, null, null, null, null, dfe));
  }

  @Test
  void should_find_article_by_slug() {
    when(articleQueryService.findBySlug(eq("test-slug"), eq(user)))
        .thenReturn(Optional.of(articleData));

    DataFetcherResult<Article> result = articleDatafetcher.findArticleBySlug("test-slug");

    assertNotNull(result);
    assertEquals("test-slug", result.getData().getSlug());
    assertEquals("Test Title", result.getData().getTitle());
  }

  @Test
  void should_fail_find_article_by_slug_when_not_found() {
    when(articleQueryService.findBySlug(eq("missing"), any())).thenReturn(Optional.empty());

    assertThrows(
        ResourceNotFoundException.class, () -> articleDatafetcher.findArticleBySlug("missing"));
  }

  @Test
  void should_get_article_from_payload() {
    io.spring.core.article.Article coreArticle =
        new io.spring.core.article.Article(
            "Test Title", "desc", "body", Arrays.asList("java"), user.getId());

    when(rawDfe.getLocalContext()).thenReturn(coreArticle);
    when(articleQueryService.findById(eq(coreArticle.getId()), eq(user)))
        .thenReturn(Optional.of(articleData));

    DataFetcherResult<Article> result = articleDatafetcher.getArticle(rawDfe);

    assertNotNull(result);
    assertEquals("test-slug", result.getData().getSlug());
  }

  @Test
  void should_get_comment_article() {
    CommentData commentData =
        new CommentData("cid", "body", "articleId", new DateTime(), new DateTime(), profileData);

    when(rawDfe.getLocalContext()).thenReturn(commentData);
    when(articleQueryService.findById(eq("articleId"), eq(user)))
        .thenReturn(Optional.of(articleData));

    DataFetcherResult<Article> result = articleDatafetcher.getCommentArticle(rawDfe);

    assertNotNull(result);
    assertEquals("test-slug", result.getData().getSlug());
  }

  @Test
  void should_get_user_favorites() {
    Profile profile = Profile.newBuilder().username("testuser").build();
    CursorPager<ArticleData> pager =
        new CursorPager<>(Arrays.asList(articleData), Direction.NEXT, false);

    when(dfe.getSource()).thenReturn(profile);
    when(articleQueryService.findRecentArticlesWithCursor(
            any(), any(), eq("testuser"), any(), any()))
        .thenReturn(pager);

    DataFetcherResult<ArticlesConnection> result =
        articleDatafetcher.userFavorites(10, null, null, null, dfe);

    assertNotNull(result);
    assertEquals(1, result.getData().getEdges().size());
  }

  @Test
  void should_get_user_favorites_with_last() {
    Profile profile = Profile.newBuilder().username("testuser").build();
    CursorPager<ArticleData> pager =
        new CursorPager<>(Arrays.asList(articleData), Direction.PREV, false);

    when(dfe.getSource()).thenReturn(profile);
    when(articleQueryService.findRecentArticlesWithCursor(
            any(), any(), eq("testuser"), any(), any()))
        .thenReturn(pager);

    DataFetcherResult<ArticlesConnection> result =
        articleDatafetcher.userFavorites(null, null, 10, null, dfe);

    assertNotNull(result);
    assertEquals(1, result.getData().getEdges().size());
  }

  @Test
  void should_fail_user_favorites_when_neither_first_nor_last() {
    assertThrows(
        IllegalArgumentException.class,
        () -> articleDatafetcher.userFavorites(null, null, null, null, dfe));
  }

  @Test
  void should_get_user_articles() {
    Profile profile = Profile.newBuilder().username("testuser").build();
    CursorPager<ArticleData> pager =
        new CursorPager<>(Arrays.asList(articleData), Direction.NEXT, false);

    when(dfe.getSource()).thenReturn(profile);
    when(articleQueryService.findRecentArticlesWithCursor(
            any(), eq("testuser"), any(), any(), any()))
        .thenReturn(pager);

    DataFetcherResult<ArticlesConnection> result =
        articleDatafetcher.userArticles(10, null, null, null, dfe);

    assertNotNull(result);
    assertEquals(1, result.getData().getEdges().size());
  }

  @Test
  void should_get_user_articles_with_last() {
    Profile profile = Profile.newBuilder().username("testuser").build();
    CursorPager<ArticleData> pager =
        new CursorPager<>(Arrays.asList(articleData), Direction.PREV, false);

    when(dfe.getSource()).thenReturn(profile);
    when(articleQueryService.findRecentArticlesWithCursor(
            any(), eq("testuser"), any(), any(), any()))
        .thenReturn(pager);

    DataFetcherResult<ArticlesConnection> result =
        articleDatafetcher.userArticles(null, null, 10, null, dfe);

    assertNotNull(result);
    assertEquals(1, result.getData().getEdges().size());
  }

  @Test
  void should_fail_user_articles_when_neither_first_nor_last() {
    assertThrows(
        IllegalArgumentException.class,
        () -> articleDatafetcher.userArticles(null, null, null, null, dfe));
  }

  @Test
  void should_get_user_feed_with_first() {
    Profile profile = Profile.newBuilder().username("testuser").build();
    CursorPager<ArticleData> pager =
        new CursorPager<>(Arrays.asList(articleData), Direction.NEXT, false);

    when(dfe.getSource()).thenReturn(profile);
    when(userRepository.findByUsername(eq("testuser"))).thenReturn(Optional.of(user));
    when(articleQueryService.findUserFeedWithCursor(eq(user), any())).thenReturn(pager);

    DataFetcherResult<ArticlesConnection> result =
        articleDatafetcher.userFeed(10, null, null, null, dfe);

    assertNotNull(result);
    assertEquals(1, result.getData().getEdges().size());
  }

  @Test
  void should_get_user_feed_with_last() {
    Profile profile = Profile.newBuilder().username("testuser").build();
    CursorPager<ArticleData> pager =
        new CursorPager<>(Arrays.asList(articleData), Direction.PREV, false);

    when(dfe.getSource()).thenReturn(profile);
    when(userRepository.findByUsername(eq("testuser"))).thenReturn(Optional.of(user));
    when(articleQueryService.findUserFeedWithCursor(eq(user), any())).thenReturn(pager);

    DataFetcherResult<ArticlesConnection> result =
        articleDatafetcher.userFeed(null, null, 10, null, dfe);

    assertNotNull(result);
    assertEquals(1, result.getData().getEdges().size());
  }

  @Test
  void should_fail_user_feed_when_neither_first_nor_last() {
    assertThrows(
        IllegalArgumentException.class,
        () -> articleDatafetcher.userFeed(null, null, null, null, dfe));
  }

  @Test
  void should_fail_user_feed_when_user_not_found() {
    Profile profile = Profile.newBuilder().username("missing").build();
    when(dfe.getSource()).thenReturn(profile);
    when(userRepository.findByUsername(eq("missing"))).thenReturn(Optional.empty());

    assertThrows(
        ResourceNotFoundException.class,
        () -> articleDatafetcher.userFeed(10, null, null, null, dfe));
  }
}
