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
import io.spring.application.CursorPageParameter;
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
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.joda.time.DateTime;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;

class ArticleDatafetcherTest {

  private ArticleDatafetcher articleDatafetcher;
  private ArticleQueryService articleQueryService;
  private UserRepository userRepository;
  private User user;

  @BeforeEach
  void setUp() {
    articleQueryService = mock(ArticleQueryService.class);
    userRepository = mock(UserRepository.class);
    articleDatafetcher = new ArticleDatafetcher(articleQueryService, userRepository);

    user = new User("test@example.com", "testuser", "password", "bio", "image");
    UsernamePasswordAuthenticationToken auth =
        new UsernamePasswordAuthenticationToken(
            user, null, AuthorityUtils.createAuthorityList("ROLE_USER"));
    SecurityContextHolder.getContext().setAuthentication(auth);
  }

  @AfterEach
  void tearDown() {
    SecurityContextHolder.clearContext();
  }

  private ArticleData createArticleData(String slug) {
    DateTime now = DateTime.now();
    ProfileData profile = new ProfileData("userId", "author", "bio", "img", false);
    return new ArticleData(
        "id-" + slug, slug, "Title " + slug, "desc", "body", false, 0, now, now,
        Arrays.asList("tag1"), profile);
  }

  // === getFeed tests ===

  @Test
  void should_get_feed_with_first() {
    DgsDataFetchingEnvironment dfe = mock(DgsDataFetchingEnvironment.class);
    ArticleData articleData = createArticleData("test-article");
    CursorPager<ArticleData> pager = new CursorPager<>(
        Arrays.asList(articleData), Direction.NEXT, false);

    when(articleQueryService.findUserFeedWithCursor(any(), any(CursorPageParameter.class)))
        .thenReturn(pager);

    DataFetcherResult<ArticlesConnection> result =
        articleDatafetcher.getFeed(10, null, null, null, dfe);

    assertNotNull(result);
    assertNotNull(result.getData());
    assertFalse(result.getData().getEdges().isEmpty());
  }

  @Test
  void should_get_feed_with_last() {
    DgsDataFetchingEnvironment dfe = mock(DgsDataFetchingEnvironment.class);
    CursorPager<ArticleData> pager = new CursorPager<>(
        Collections.emptyList(), Direction.PREV, false);

    when(articleQueryService.findUserFeedWithCursor(any(), any(CursorPageParameter.class)))
        .thenReturn(pager);

    DataFetcherResult<ArticlesConnection> result =
        articleDatafetcher.getFeed(null, null, 5, null, dfe);

    assertNotNull(result);
    assertNotNull(result.getData());
  }

  @Test
  void should_throw_when_feed_no_first_or_last() {
    DgsDataFetchingEnvironment dfe = mock(DgsDataFetchingEnvironment.class);

    assertThrows(
        IllegalArgumentException.class,
        () -> articleDatafetcher.getFeed(null, null, null, null, dfe));
  }

  // === userFeed tests ===

  @Test
  void should_get_user_feed_with_first() {
    DgsDataFetchingEnvironment dfe = mock(DgsDataFetchingEnvironment.class);
    Profile profile = Profile.newBuilder().username("testuser").build();
    when(dfe.getSource()).thenReturn(profile);
    when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));

    CursorPager<ArticleData> pager = new CursorPager<>(
        Collections.emptyList(), Direction.NEXT, false);
    when(articleQueryService.findUserFeedWithCursor(any(), any(CursorPageParameter.class)))
        .thenReturn(pager);

    DataFetcherResult<ArticlesConnection> result =
        articleDatafetcher.userFeed(10, null, null, null, dfe);

    assertNotNull(result);
  }

  @Test
  void should_get_user_feed_with_last() {
    DgsDataFetchingEnvironment dfe = mock(DgsDataFetchingEnvironment.class);
    Profile profile = Profile.newBuilder().username("testuser").build();
    when(dfe.getSource()).thenReturn(profile);
    when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));

    CursorPager<ArticleData> pager = new CursorPager<>(
        Collections.emptyList(), Direction.PREV, false);
    when(articleQueryService.findUserFeedWithCursor(any(), any(CursorPageParameter.class)))
        .thenReturn(pager);

    DataFetcherResult<ArticlesConnection> result =
        articleDatafetcher.userFeed(null, null, 5, null, dfe);

    assertNotNull(result);
  }

  @Test
  void should_throw_when_user_feed_no_first_or_last() {
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

  // === userFavorites tests ===

  @Test
  void should_get_user_favorites_with_first() {
    DgsDataFetchingEnvironment dfe = mock(DgsDataFetchingEnvironment.class);
    Profile profile = Profile.newBuilder().username("testuser").build();
    when(dfe.getSource()).thenReturn(profile);

    CursorPager<ArticleData> pager = new CursorPager<>(
        Collections.emptyList(), Direction.NEXT, false);
    when(articleQueryService.findRecentArticlesWithCursor(
        any(), any(), eq("testuser"), any(CursorPageParameter.class), any()))
        .thenReturn(pager);

    DataFetcherResult<ArticlesConnection> result =
        articleDatafetcher.userFavorites(10, null, null, null, dfe);

    assertNotNull(result);
  }

  @Test
  void should_get_user_favorites_with_last() {
    DgsDataFetchingEnvironment dfe = mock(DgsDataFetchingEnvironment.class);
    Profile profile = Profile.newBuilder().username("testuser").build();
    when(dfe.getSource()).thenReturn(profile);

    CursorPager<ArticleData> pager = new CursorPager<>(
        Collections.emptyList(), Direction.PREV, false);
    when(articleQueryService.findRecentArticlesWithCursor(
        any(), any(), eq("testuser"), any(CursorPageParameter.class), any()))
        .thenReturn(pager);

    DataFetcherResult<ArticlesConnection> result =
        articleDatafetcher.userFavorites(null, null, 5, null, dfe);

    assertNotNull(result);
  }

  @Test
  void should_throw_when_user_favorites_no_first_or_last() {
    DgsDataFetchingEnvironment dfe = mock(DgsDataFetchingEnvironment.class);

    assertThrows(
        IllegalArgumentException.class,
        () -> articleDatafetcher.userFavorites(null, null, null, null, dfe));
  }

  // === userArticles tests ===

  @Test
  void should_get_user_articles_with_first() {
    DgsDataFetchingEnvironment dfe = mock(DgsDataFetchingEnvironment.class);
    Profile profile = Profile.newBuilder().username("testuser").build();
    when(dfe.getSource()).thenReturn(profile);

    CursorPager<ArticleData> pager = new CursorPager<>(
        Collections.emptyList(), Direction.NEXT, false);
    when(articleQueryService.findRecentArticlesWithCursor(
        any(), eq("testuser"), any(), any(CursorPageParameter.class), any()))
        .thenReturn(pager);

    DataFetcherResult<ArticlesConnection> result =
        articleDatafetcher.userArticles(10, null, null, null, dfe);

    assertNotNull(result);
  }

  @Test
  void should_get_user_articles_with_last() {
    DgsDataFetchingEnvironment dfe = mock(DgsDataFetchingEnvironment.class);
    Profile profile = Profile.newBuilder().username("testuser").build();
    when(dfe.getSource()).thenReturn(profile);

    CursorPager<ArticleData> pager = new CursorPager<>(
        Collections.emptyList(), Direction.PREV, false);
    when(articleQueryService.findRecentArticlesWithCursor(
        any(), eq("testuser"), any(), any(CursorPageParameter.class), any()))
        .thenReturn(pager);

    DataFetcherResult<ArticlesConnection> result =
        articleDatafetcher.userArticles(null, null, 5, null, dfe);

    assertNotNull(result);
  }

  @Test
  void should_throw_when_user_articles_no_first_or_last() {
    DgsDataFetchingEnvironment dfe = mock(DgsDataFetchingEnvironment.class);

    assertThrows(
        IllegalArgumentException.class,
        () -> articleDatafetcher.userArticles(null, null, null, null, dfe));
  }

  // === getArticles tests ===

  @Test
  void should_get_articles_with_first() {
    DgsDataFetchingEnvironment dfe = mock(DgsDataFetchingEnvironment.class);
    ArticleData articleData = createArticleData("test-slug");
    CursorPager<ArticleData> pager = new CursorPager<>(
        Arrays.asList(articleData), Direction.NEXT, false);

    when(articleQueryService.findRecentArticlesWithCursor(
        any(), any(), any(), any(CursorPageParameter.class), any()))
        .thenReturn(pager);

    DataFetcherResult<ArticlesConnection> result =
        articleDatafetcher.getArticles(10, null, null, null, null, null, null, dfe);

    assertNotNull(result);
    assertNotNull(result.getData());
    assertEquals(1, result.getData().getEdges().size());
  }

  @Test
  void should_get_articles_with_last() {
    DgsDataFetchingEnvironment dfe = mock(DgsDataFetchingEnvironment.class);
    CursorPager<ArticleData> pager = new CursorPager<>(
        Collections.emptyList(), Direction.PREV, false);

    when(articleQueryService.findRecentArticlesWithCursor(
        any(), any(), any(), any(CursorPageParameter.class), any()))
        .thenReturn(pager);

    DataFetcherResult<ArticlesConnection> result =
        articleDatafetcher.getArticles(null, null, 5, null, "author", "fav", "tag", dfe);

    assertNotNull(result);
  }

  @Test
  void should_throw_when_articles_no_first_or_last() {
    DgsDataFetchingEnvironment dfe = mock(DgsDataFetchingEnvironment.class);

    assertThrows(
        IllegalArgumentException.class,
        () -> articleDatafetcher.getArticles(null, null, null, null, null, null, null, dfe));
  }

  // === getArticle tests ===

  @Test
  void should_get_article_from_payload() {
    DataFetchingEnvironment dfe = mock(DataFetchingEnvironment.class);
    Article coreArticle = new Article("Title", "Desc", "Body", Collections.emptyList(), user.getId());
    when(dfe.getLocalContext()).thenReturn(coreArticle);

    ArticleData articleData = createArticleData("title");
    when(articleQueryService.findById(coreArticle.getId(), user))
        .thenReturn(Optional.of(articleData));

    DataFetcherResult<io.spring.graphql.types.Article> result =
        articleDatafetcher.getArticle(dfe);

    assertNotNull(result);
    assertNotNull(result.getData());
    assertEquals("title", result.getData().getSlug());
  }

  @Test
  void should_throw_when_article_not_found_in_payload() {
    DataFetchingEnvironment dfe = mock(DataFetchingEnvironment.class);
    Article coreArticle = new Article("Title", "Desc", "Body", Collections.emptyList(), user.getId());
    when(dfe.getLocalContext()).thenReturn(coreArticle);
    when(articleQueryService.findById(coreArticle.getId(), user))
        .thenReturn(Optional.empty());

    assertThrows(ResourceNotFoundException.class, () -> articleDatafetcher.getArticle(dfe));
  }

  // === getCommentArticle tests ===

  @Test
  void should_get_comment_article() {
    DataFetchingEnvironment dfe = mock(DataFetchingEnvironment.class);
    DateTime now = DateTime.now();
    ProfileData profile = new ProfileData("userId", "author", "bio", "img", false);
    CommentData commentData = new CommentData("c1", "body", "article-id", now, now, profile);
    when(dfe.getLocalContext()).thenReturn(commentData);

    ArticleData articleData = createArticleData("slug");
    when(articleQueryService.findById("article-id", user))
        .thenReturn(Optional.of(articleData));

    DataFetcherResult<io.spring.graphql.types.Article> result =
        articleDatafetcher.getCommentArticle(dfe);

    assertNotNull(result);
    assertNotNull(result.getData());
  }

  // === findArticleBySlug tests ===

  @Test
  void should_find_article_by_slug() {
    ArticleData articleData = createArticleData("my-article");
    when(articleQueryService.findBySlug("my-article", user))
        .thenReturn(Optional.of(articleData));

    DataFetcherResult<io.spring.graphql.types.Article> result =
        articleDatafetcher.findArticleBySlug("my-article");

    assertNotNull(result);
    assertEquals("my-article", result.getData().getSlug());
  }

  @Test
  void should_throw_when_article_slug_not_found() {
    when(articleQueryService.findBySlug("nonexistent", user))
        .thenReturn(Optional.empty());

    assertThrows(
        ResourceNotFoundException.class,
        () -> articleDatafetcher.findArticleBySlug("nonexistent"));
  }
}
