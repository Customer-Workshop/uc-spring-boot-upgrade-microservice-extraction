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
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
public class ArticleDatafetcherTest {

  @Mock private ArticleQueryService articleQueryService;
  @Mock private UserRepository userRepository;
  @Mock private DgsDataFetchingEnvironment dfe;
  @Mock private DataFetchingEnvironment dataFetchingEnvironment;

  private ArticleDatafetcher articleDatafetcher;
  private User user;

  @BeforeEach
  void setUp() {
    articleDatafetcher = new ArticleDatafetcher(articleQueryService, userRepository);
    user = new User("test@test.com", "testuser", "password", "bio", "image");
  }

  @AfterEach
  void tearDown() {
    SecurityContextHolder.clearContext();
  }

  private void setAuthenticated(User u) {
    SecurityContextHolder.getContext()
        .setAuthentication(
            new UsernamePasswordAuthenticationToken(
                u, null, Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))));
  }

  private void setAnonymous() {
    SecurityContextHolder.getContext()
        .setAuthentication(
            new AnonymousAuthenticationToken(
                "key",
                "anonymous",
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_ANONYMOUS"))));
  }

  private ArticleData buildArticleData(String slug, String title) {
    return new ArticleData(
        "id-" + slug,
        slug,
        title,
        "desc",
        "body",
        false,
        0,
        new DateTime(),
        new DateTime(),
        Collections.emptyList(),
        new ProfileData(user.getId(), "testuser", "", "", false));
  }

  @Test
  void should_get_feed_with_first() {
    setAuthenticated(user);
    ArticleData articleData = buildArticleData("test-slug", "Test Title");
    CursorPager<ArticleData> pager =
        new CursorPager<>(Arrays.asList(articleData), Direction.NEXT, true);

    when(articleQueryService.findUserFeedWithCursor(eq(user), any())).thenReturn(pager);

    DataFetcherResult<ArticlesConnection> result =
        articleDatafetcher.getFeed(10, null, null, null, dfe);

    assertNotNull(result);
    assertNotNull(result.getData());
    assertFalse(result.getData().getEdges().isEmpty());
  }

  @Test
  void should_get_feed_with_last() {
    setAuthenticated(user);
    ArticleData articleData = buildArticleData("test-slug", "Test Title");
    CursorPager<ArticleData> pager =
        new CursorPager<>(Arrays.asList(articleData), Direction.PREV, false);

    when(articleQueryService.findUserFeedWithCursor(eq(user), any())).thenReturn(pager);

    DataFetcherResult<ArticlesConnection> result =
        articleDatafetcher.getFeed(null, null, 5, null, dfe);

    assertNotNull(result);
  }

  @Test
  void should_throw_when_both_first_and_last_null_for_feed() {
    setAuthenticated(user);
    assertThrows(
        IllegalArgumentException.class,
        () -> articleDatafetcher.getFeed(null, null, null, null, dfe));
  }

  @Test
  void should_get_user_feed() {
    setAnonymous();
    Profile profile = Profile.newBuilder().username("testuser").build();
    doReturn(profile).when(dfe).getSource();
    when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));

    ArticleData articleData = buildArticleData("test-slug", "Test Title");
    CursorPager<ArticleData> pager =
        new CursorPager<>(Arrays.asList(articleData), Direction.NEXT, false);
    when(articleQueryService.findUserFeedWithCursor(eq(user), any())).thenReturn(pager);

    DataFetcherResult<ArticlesConnection> result =
        articleDatafetcher.userFeed(10, null, null, null, dfe);

    assertNotNull(result);
  }

  @Test
  void should_throw_not_found_when_user_feed_for_nonexistent_user() {
    setAnonymous();
    Profile profile = Profile.newBuilder().username("nonexistent").build();
    doReturn(profile).when(dfe).getSource();
    when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

    assertThrows(
        ResourceNotFoundException.class,
        () -> articleDatafetcher.userFeed(10, null, null, null, dfe));
  }

  @Test
  void should_get_user_favorites() {
    setAnonymous();
    Profile profile = Profile.newBuilder().username("testuser").build();
    doReturn(profile).when(dfe).getSource();

    CursorPager<ArticleData> pager =
        new CursorPager<>(Collections.emptyList(), Direction.NEXT, false);
    when(articleQueryService.findRecentArticlesWithCursor(
            any(), any(), eq("testuser"), any(), any()))
        .thenReturn(pager);

    DataFetcherResult<ArticlesConnection> result =
        articleDatafetcher.userFavorites(10, null, null, null, dfe);

    assertNotNull(result);
    assertTrue(result.getData().getEdges().isEmpty());
  }

  @Test
  void should_get_user_favorites_with_last() {
    setAnonymous();
    Profile profile = Profile.newBuilder().username("testuser").build();
    doReturn(profile).when(dfe).getSource();

    CursorPager<ArticleData> pager =
        new CursorPager<>(Collections.emptyList(), Direction.NEXT, false);
    when(articleQueryService.findRecentArticlesWithCursor(
            any(), any(), eq("testuser"), any(), any()))
        .thenReturn(pager);

    DataFetcherResult<ArticlesConnection> result =
        articleDatafetcher.userFavorites(null, null, 5, null, dfe);

    assertNotNull(result);
  }

  @Test
  void should_get_user_articles() {
    setAnonymous();
    Profile profile = Profile.newBuilder().username("testuser").build();
    doReturn(profile).when(dfe).getSource();

    CursorPager<ArticleData> pager =
        new CursorPager<>(Collections.emptyList(), Direction.NEXT, false);
    when(articleQueryService.findRecentArticlesWithCursor(
            any(), eq("testuser"), any(), any(), any()))
        .thenReturn(pager);

    DataFetcherResult<ArticlesConnection> result =
        articleDatafetcher.userArticles(10, null, null, null, dfe);

    assertNotNull(result);
  }

  @Test
  void should_get_user_articles_with_last() {
    setAnonymous();
    Profile profile = Profile.newBuilder().username("testuser").build();
    doReturn(profile).when(dfe).getSource();

    CursorPager<ArticleData> pager =
        new CursorPager<>(Collections.emptyList(), Direction.NEXT, false);
    when(articleQueryService.findRecentArticlesWithCursor(
            any(), eq("testuser"), any(), any(), any()))
        .thenReturn(pager);

    DataFetcherResult<ArticlesConnection> result =
        articleDatafetcher.userArticles(null, null, 5, null, dfe);

    assertNotNull(result);
  }

  @Test
  void should_get_articles_with_filters() {
    setAnonymous();
    CursorPager<ArticleData> pager =
        new CursorPager<>(Collections.emptyList(), Direction.NEXT, false);
    when(articleQueryService.findRecentArticlesWithCursor(
            eq("java"), eq("testuser"), eq("favuser"), any(), any()))
        .thenReturn(pager);

    DataFetcherResult<ArticlesConnection> result =
        articleDatafetcher.getArticles(10, null, null, null, "testuser", "favuser", "java", dfe);

    assertNotNull(result);
  }

  @Test
  void should_get_articles_with_last() {
    setAnonymous();
    CursorPager<ArticleData> pager =
        new CursorPager<>(Collections.emptyList(), Direction.NEXT, false);
    when(articleQueryService.findRecentArticlesWithCursor(any(), any(), any(), any(), any()))
        .thenReturn(pager);

    DataFetcherResult<ArticlesConnection> result =
        articleDatafetcher.getArticles(null, null, 5, null, null, null, null, dfe);

    assertNotNull(result);
  }

  @Test
  void should_throw_when_both_first_and_last_null_for_articles() {
    assertThrows(
        IllegalArgumentException.class,
        () -> articleDatafetcher.getArticles(null, null, null, null, null, null, null, dfe));
  }

  @Test
  void should_get_article_payload() {
    setAnonymous();
    io.spring.core.article.Article coreArticle =
        new io.spring.core.article.Article(
            "Title", "Desc", "Body", Collections.emptyList(), user.getId());
    doReturn(coreArticle).when(dataFetchingEnvironment).getLocalContext();

    ArticleData articleData = buildArticleData("title", "Title");
    when(articleQueryService.findById(coreArticle.getId(), null))
        .thenReturn(Optional.of(articleData));

    DataFetcherResult<Article> result = articleDatafetcher.getArticle(dataFetchingEnvironment);

    assertNotNull(result);
    assertEquals("Title", result.getData().getTitle());
  }

  @Test
  void should_throw_not_found_when_article_payload_not_found() {
    setAnonymous();
    io.spring.core.article.Article coreArticle =
        new io.spring.core.article.Article(
            "Title", "Desc", "Body", Collections.emptyList(), user.getId());
    doReturn(coreArticle).when(dataFetchingEnvironment).getLocalContext();

    when(articleQueryService.findById(coreArticle.getId(), null)).thenReturn(Optional.empty());

    assertThrows(
        ResourceNotFoundException.class,
        () -> articleDatafetcher.getArticle(dataFetchingEnvironment));
  }

  @Test
  void should_get_comment_article() {
    setAnonymous();
    ProfileData profile = new ProfileData(user.getId(), "testuser", "", "", false);
    CommentData commentData =
        new CommentData("cid", "body", "article-id", new DateTime(), new DateTime(), profile);
    doReturn(commentData).when(dataFetchingEnvironment).getLocalContext();

    ArticleData articleData = buildArticleData("title", "Title");
    when(articleQueryService.findById("article-id", null)).thenReturn(Optional.of(articleData));

    DataFetcherResult<Article> result =
        articleDatafetcher.getCommentArticle(dataFetchingEnvironment);

    assertNotNull(result);
    assertEquals("Title", result.getData().getTitle());
  }

  @Test
  void should_find_article_by_slug() {
    setAnonymous();
    ArticleData articleData = buildArticleData("test-slug", "Test Title");
    when(articleQueryService.findBySlug("test-slug", null)).thenReturn(Optional.of(articleData));

    DataFetcherResult<Article> result = articleDatafetcher.findArticleBySlug("test-slug");

    assertNotNull(result);
    assertEquals("Test Title", result.getData().getTitle());
    assertEquals("test-slug", result.getData().getSlug());
  }

  @Test
  void should_throw_not_found_when_slug_not_found() {
    setAnonymous();
    when(articleQueryService.findBySlug("missing", null)).thenReturn(Optional.empty());

    assertThrows(
        ResourceNotFoundException.class, () -> articleDatafetcher.findArticleBySlug("missing"));
  }
}
