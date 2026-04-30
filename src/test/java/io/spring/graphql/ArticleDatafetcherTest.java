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
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
public class ArticleDatafetcherTest {

  @Mock private ArticleQueryService articleQueryService;
  @Mock private UserRepository userRepository;
  @Mock private DgsDataFetchingEnvironment dfe;

  private ArticleDatafetcher fetcher;
  private User user;

  @BeforeEach
  void setUp() {
    fetcher = new ArticleDatafetcher(articleQueryService, userRepository);
    user = new User("test@test.com", "testuser", "password", "bio", "image");
    SecurityContextHolder.getContext()
        .setAuthentication(new UsernamePasswordAuthenticationToken(user, null));
  }

  @AfterEach
  void tearDown() {
    SecurityContextHolder.clearContext();
  }

  @Test
  void should_get_feed_with_first() {
    ArticleData articleData = createArticleData("1", "slug-1");
    CursorPager<ArticleData> pager = new CursorPager<>(List.of(articleData), Direction.NEXT, false);
    when(articleQueryService.findUserFeedWithCursor(any(), any())).thenReturn(pager);

    DataFetcherResult<ArticlesConnection> result = fetcher.getFeed(10, null, null, null, dfe);

    assertNotNull(result);
    assertNotNull(result.getData());
    assertEquals(1, result.getData().getEdges().size());
  }

  @Test
  void should_get_feed_with_last() {
    ArticleData articleData = createArticleData("1", "slug-1");
    CursorPager<ArticleData> pager = new CursorPager<>(List.of(articleData), Direction.PREV, false);
    when(articleQueryService.findUserFeedWithCursor(any(), any())).thenReturn(pager);

    DataFetcherResult<ArticlesConnection> result = fetcher.getFeed(null, null, 5, null, dfe);

    assertNotNull(result);
    assertEquals(1, result.getData().getEdges().size());
  }

  @Test
  void should_throw_when_feed_first_and_last_both_null() {
    assertThrows(
        IllegalArgumentException.class, () -> fetcher.getFeed(null, null, null, null, dfe));
  }

  @Test
  void should_get_user_feed_with_first() {
    Profile profile = Profile.newBuilder().username("testuser").build();
    when(dfe.getSource()).thenReturn(profile);
    when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));

    ArticleData articleData = createArticleData("1", "slug-1");
    CursorPager<ArticleData> pager = new CursorPager<>(List.of(articleData), Direction.NEXT, false);
    when(articleQueryService.findUserFeedWithCursor(any(), any())).thenReturn(pager);

    DataFetcherResult<ArticlesConnection> result = fetcher.userFeed(10, null, null, null, dfe);

    assertNotNull(result);
    assertEquals(1, result.getData().getEdges().size());
  }

  @Test
  void should_get_user_feed_with_last() {
    Profile profile = Profile.newBuilder().username("testuser").build();
    when(dfe.getSource()).thenReturn(profile);
    when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));

    ArticleData articleData = createArticleData("1", "slug-1");
    CursorPager<ArticleData> pager = new CursorPager<>(List.of(articleData), Direction.PREV, false);
    when(articleQueryService.findUserFeedWithCursor(any(), any())).thenReturn(pager);

    DataFetcherResult<ArticlesConnection> result = fetcher.userFeed(null, null, 5, null, dfe);

    assertNotNull(result);
    assertEquals(1, result.getData().getEdges().size());
  }

  @Test
  void should_throw_when_user_feed_first_and_last_both_null() {
    assertThrows(
        IllegalArgumentException.class, () -> fetcher.userFeed(null, null, null, null, dfe));
  }

  @Test
  void should_throw_when_user_feed_user_not_found() {
    Profile profile = Profile.newBuilder().username("unknown").build();
    when(dfe.getSource()).thenReturn(profile);
    when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());

    assertThrows(
        ResourceNotFoundException.class, () -> fetcher.userFeed(10, null, null, null, dfe));
  }

  @Test
  void should_get_user_favorites_with_first() {
    Profile profile = Profile.newBuilder().username("testuser").build();
    when(dfe.getSource()).thenReturn(profile);

    ArticleData articleData = createArticleData("1", "slug-1");
    CursorPager<ArticleData> pager = new CursorPager<>(List.of(articleData), Direction.NEXT, false);
    when(articleQueryService.findRecentArticlesWithCursor(
            any(), any(), eq("testuser"), any(), any()))
        .thenReturn(pager);

    DataFetcherResult<ArticlesConnection> result = fetcher.userFavorites(10, null, null, null, dfe);

    assertNotNull(result);
    assertEquals(1, result.getData().getEdges().size());
  }

  @Test
  void should_get_user_favorites_with_last() {
    Profile profile = Profile.newBuilder().username("testuser").build();
    when(dfe.getSource()).thenReturn(profile);

    ArticleData articleData = createArticleData("1", "slug-1");
    CursorPager<ArticleData> pager = new CursorPager<>(List.of(articleData), Direction.PREV, false);
    when(articleQueryService.findRecentArticlesWithCursor(
            any(), any(), eq("testuser"), any(), any()))
        .thenReturn(pager);

    DataFetcherResult<ArticlesConnection> result = fetcher.userFavorites(null, null, 5, null, dfe);

    assertNotNull(result);
    assertEquals(1, result.getData().getEdges().size());
  }

  @Test
  void should_throw_when_user_favorites_first_and_last_both_null() {
    assertThrows(
        IllegalArgumentException.class, () -> fetcher.userFavorites(null, null, null, null, dfe));
  }

  @Test
  void should_get_user_articles_with_first() {
    Profile profile = Profile.newBuilder().username("testuser").build();
    when(dfe.getSource()).thenReturn(profile);

    ArticleData articleData = createArticleData("1", "slug-1");
    CursorPager<ArticleData> pager = new CursorPager<>(List.of(articleData), Direction.NEXT, false);
    when(articleQueryService.findRecentArticlesWithCursor(
            any(), eq("testuser"), any(), any(), any()))
        .thenReturn(pager);

    DataFetcherResult<ArticlesConnection> result = fetcher.userArticles(10, null, null, null, dfe);

    assertNotNull(result);
    assertEquals(1, result.getData().getEdges().size());
  }

  @Test
  void should_get_user_articles_with_last() {
    Profile profile = Profile.newBuilder().username("testuser").build();
    when(dfe.getSource()).thenReturn(profile);

    ArticleData articleData = createArticleData("1", "slug-1");
    CursorPager<ArticleData> pager = new CursorPager<>(List.of(articleData), Direction.PREV, false);
    when(articleQueryService.findRecentArticlesWithCursor(
            any(), eq("testuser"), any(), any(), any()))
        .thenReturn(pager);

    DataFetcherResult<ArticlesConnection> result = fetcher.userArticles(null, null, 5, null, dfe);

    assertNotNull(result);
    assertEquals(1, result.getData().getEdges().size());
  }

  @Test
  void should_throw_when_user_articles_first_and_last_both_null() {
    assertThrows(
        IllegalArgumentException.class, () -> fetcher.userArticles(null, null, null, null, dfe));
  }

  @Test
  void should_get_articles_with_first() {
    ArticleData articleData = createArticleData("1", "slug-1");
    CursorPager<ArticleData> pager = new CursorPager<>(List.of(articleData), Direction.NEXT, false);
    when(articleQueryService.findRecentArticlesWithCursor(any(), any(), any(), any(), any()))
        .thenReturn(pager);

    DataFetcherResult<ArticlesConnection> result =
        fetcher.getArticles(10, null, null, null, "author", "fav", "tag", dfe);

    assertNotNull(result);
    assertEquals(1, result.getData().getEdges().size());
  }

  @Test
  void should_get_articles_with_last() {
    ArticleData articleData = createArticleData("1", "slug-1");
    CursorPager<ArticleData> pager = new CursorPager<>(List.of(articleData), Direction.PREV, false);
    when(articleQueryService.findRecentArticlesWithCursor(any(), any(), any(), any(), any()))
        .thenReturn(pager);

    DataFetcherResult<ArticlesConnection> result =
        fetcher.getArticles(null, null, 5, null, "author", "fav", "tag", dfe);

    assertNotNull(result);
    assertEquals(1, result.getData().getEdges().size());
  }

  @Test
  void should_throw_when_articles_first_and_last_both_null() {
    assertThrows(
        IllegalArgumentException.class,
        () -> fetcher.getArticles(null, null, null, null, null, null, null, dfe));
  }

  @Test
  void should_get_article_from_payload() {
    io.spring.core.article.Article coreArticle =
        new io.spring.core.article.Article("title", "desc", "body", List.of("tag"), user.getId());
    DataFetchingEnvironment env = mock(DataFetchingEnvironment.class);
    when(env.getLocalContext()).thenReturn(coreArticle);

    ArticleData articleData = createArticleData(coreArticle.getId(), coreArticle.getSlug());
    when(articleQueryService.findById(eq(coreArticle.getId()), any()))
        .thenReturn(Optional.of(articleData));

    DataFetcherResult<Article> result = fetcher.getArticle(env);

    assertNotNull(result);
    assertNotNull(result.getData());
    assertEquals(coreArticle.getSlug(), result.getData().getSlug());
  }

  @Test
  void should_get_comment_article() {
    CommentData commentData =
        new CommentData("c1", "body", "article1", new DateTime(), new DateTime(), null);
    DataFetchingEnvironment env = mock(DataFetchingEnvironment.class);
    when(env.getLocalContext()).thenReturn(commentData);

    ArticleData articleData = createArticleData("article1", "slug-1");
    when(articleQueryService.findById(eq("article1"), any())).thenReturn(Optional.of(articleData));

    DataFetcherResult<Article> result = fetcher.getCommentArticle(env);

    assertNotNull(result);
    assertNotNull(result.getData());
  }

  @Test
  void should_find_article_by_slug() {
    ArticleData articleData = createArticleData("1", "test-slug");
    when(articleQueryService.findBySlug(eq("test-slug"), any()))
        .thenReturn(Optional.of(articleData));

    DataFetcherResult<Article> result = fetcher.findArticleBySlug("test-slug");

    assertNotNull(result);
    assertEquals("test-slug", result.getData().getSlug());
  }

  @Test
  void should_throw_when_article_by_slug_not_found() {
    when(articleQueryService.findBySlug(eq("missing"), any())).thenReturn(Optional.empty());

    assertThrows(ResourceNotFoundException.class, () -> fetcher.findArticleBySlug("missing"));
  }

  @Test
  void should_handle_empty_pager() {
    CursorPager<ArticleData> pager = new CursorPager<>(List.of(), Direction.NEXT, false);
    when(articleQueryService.findUserFeedWithCursor(any(), any())).thenReturn(pager);

    DataFetcherResult<ArticlesConnection> result = fetcher.getFeed(10, null, null, null, dfe);

    assertNotNull(result);
    assertEquals(0, result.getData().getEdges().size());
  }

  private ArticleData createArticleData(String id, String slug) {
    return new ArticleData(
        id,
        slug,
        "title",
        "description",
        "body",
        false,
        0,
        new DateTime(),
        new DateTime(),
        List.of("tag1"),
        new ProfileData("userId", "testuser", "bio", "image", false));
  }
}
