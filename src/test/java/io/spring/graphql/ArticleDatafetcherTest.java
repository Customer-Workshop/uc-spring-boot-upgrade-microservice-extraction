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
import io.spring.core.article.Article;
import io.spring.core.user.User;
import io.spring.core.user.UserRepository;
import io.spring.graphql.types.ArticlesConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;
import org.joda.time.DateTime;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
public class ArticleDatafetcherTest {

  @Mock private ArticleQueryService articleQueryService;
  @Mock private UserRepository userRepository;

  @InjectMocks private ArticleDatafetcher articleDatafetcher;

  private User user;
  private ArticleData articleData;

  @BeforeEach
  void setUp() {
    user = new User("test@test.com", "testuser", "password", "bio", "image");
    DateTime now = new DateTime();
    articleData =
        new ArticleData(
            "article-id",
            "test-title",
            "Test Title",
            "desc",
            "body",
            false,
            0,
            now,
            now,
            Arrays.asList("java"),
            new ProfileData(user.getId(), "testuser", "bio", "image", false));
    setAnonymousAuth();
  }

  @AfterEach
  void tearDown() {
    SecurityContextHolder.clearContext();
  }

  private void setAnonymousAuth() {
    SecurityContextHolder.getContext()
        .setAuthentication(
            new AnonymousAuthenticationToken(
                "key", "anonymous", AuthorityUtils.createAuthorityList("ROLE_ANONYMOUS")));
  }

  private void authenticateUser(User u) {
    SecurityContextHolder.getContext()
        .setAuthentication(new UsernamePasswordAuthenticationToken(u, null));
  }

  @Test
  public void should_get_feed_with_first_param() {
    authenticateUser(user);
    CursorPager<ArticleData> pager =
        new CursorPager<>(Arrays.asList(articleData), Direction.NEXT, false);
    when(articleQueryService.findUserFeedWithCursor(eq(user), any())).thenReturn(pager);

    DgsDataFetchingEnvironment dfe = mock(DgsDataFetchingEnvironment.class);
    DataFetcherResult<ArticlesConnection> result =
        articleDatafetcher.getFeed(10, null, null, null, dfe);
    assertNotNull(result);
    assertEquals(1, result.getData().getEdges().size());
  }

  @Test
  public void should_get_feed_with_last_param() {
    authenticateUser(user);
    CursorPager<ArticleData> pager =
        new CursorPager<>(Arrays.asList(articleData), Direction.PREV, false);
    when(articleQueryService.findUserFeedWithCursor(eq(user), any())).thenReturn(pager);

    DgsDataFetchingEnvironment dfe = mock(DgsDataFetchingEnvironment.class);
    DataFetcherResult<ArticlesConnection> result =
        articleDatafetcher.getFeed(null, null, 10, null, dfe);
    assertNotNull(result);
    assertEquals(1, result.getData().getEdges().size());
  }

  @Test
  public void should_throw_when_feed_missing_first_and_last() {
    DgsDataFetchingEnvironment dfe = mock(DgsDataFetchingEnvironment.class);
    assertThrows(
        IllegalArgumentException.class,
        () -> articleDatafetcher.getFeed(null, null, null, null, dfe));
  }

  @Test
  public void should_get_feed_with_empty_results() {
    authenticateUser(user);
    CursorPager<ArticleData> pager =
        new CursorPager<>(new ArrayList<>(), Direction.NEXT, false);
    when(articleQueryService.findUserFeedWithCursor(eq(user), any())).thenReturn(pager);

    DgsDataFetchingEnvironment dfe = mock(DgsDataFetchingEnvironment.class);
    DataFetcherResult<ArticlesConnection> result =
        articleDatafetcher.getFeed(10, null, null, null, dfe);
    assertNotNull(result);
    assertEquals(0, result.getData().getEdges().size());
  }

  @Test
  public void should_get_articles_with_first_param() {
    CursorPager<ArticleData> pager =
        new CursorPager<>(Arrays.asList(articleData), Direction.NEXT, false);
    when(articleQueryService.findRecentArticlesWithCursor(any(), any(), any(), any(), any()))
        .thenReturn(pager);

    DgsDataFetchingEnvironment dfe = mock(DgsDataFetchingEnvironment.class);
    DataFetcherResult<ArticlesConnection> result =
        articleDatafetcher.getArticles(10, null, null, null, null, null, null, dfe);
    assertNotNull(result);
    assertEquals(1, result.getData().getEdges().size());
  }

  @Test
  public void should_get_articles_with_last_param() {
    CursorPager<ArticleData> pager =
        new CursorPager<>(Arrays.asList(articleData), Direction.PREV, false);
    when(articleQueryService.findRecentArticlesWithCursor(any(), any(), any(), any(), any()))
        .thenReturn(pager);

    DgsDataFetchingEnvironment dfe = mock(DgsDataFetchingEnvironment.class);
    DataFetcherResult<ArticlesConnection> result =
        articleDatafetcher.getArticles(null, null, 10, null, "author", "fav", "java", dfe);
    assertNotNull(result);
  }

  @Test
  public void should_throw_when_articles_missing_first_and_last() {
    DgsDataFetchingEnvironment dfe = mock(DgsDataFetchingEnvironment.class);
    assertThrows(
        IllegalArgumentException.class,
        () -> articleDatafetcher.getArticles(null, null, null, null, null, null, null, dfe));
  }

  @Test
  public void should_find_article_by_slug() {
    when(articleQueryService.findBySlug(eq("test-title"), any()))
        .thenReturn(Optional.of(articleData));

    DataFetcherResult<io.spring.graphql.types.Article> result =
        articleDatafetcher.findArticleBySlug("test-title");
    assertNotNull(result);
    assertEquals("test-title", result.getData().getSlug());
    assertEquals("body", result.getData().getBody());
  }

  @Test
  public void should_throw_when_article_slug_not_found() {
    when(articleQueryService.findBySlug(eq("not-found"), any())).thenReturn(Optional.empty());
    assertThrows(
        ResourceNotFoundException.class,
        () -> articleDatafetcher.findArticleBySlug("not-found"));
  }

  @Test
  public void should_get_article_payload() {
    Article article =
        new Article("Test Title", "desc", "body", Arrays.asList("java"), user.getId());
    when(articleQueryService.findById(eq(article.getId()), any()))
        .thenReturn(Optional.of(articleData));

    DataFetchingEnvironment dfe = mock(DataFetchingEnvironment.class);
    when(dfe.getLocalContext()).thenReturn(article);

    DataFetcherResult<io.spring.graphql.types.Article> result = articleDatafetcher.getArticle(dfe);
    assertNotNull(result);
    assertEquals("body", result.getData().getBody());
  }

  @Test
  public void should_throw_when_article_payload_not_found() {
    Article article =
        new Article("Test Title", "desc", "body", Arrays.asList("java"), user.getId());
    when(articleQueryService.findById(eq(article.getId()), any())).thenReturn(Optional.empty());

    DataFetchingEnvironment dfe = mock(DataFetchingEnvironment.class);
    when(dfe.getLocalContext()).thenReturn(article);

    assertThrows(ResourceNotFoundException.class, () -> articleDatafetcher.getArticle(dfe));
  }

  @Test
  public void should_get_comment_article() {
    DateTime now = new DateTime();
    CommentData commentData =
        new CommentData(
            "comment-id",
            "body",
            articleData.getId(),
            now,
            now,
            new ProfileData(user.getId(), "testuser", "bio", "image", false));

    when(articleQueryService.findById(eq(articleData.getId()), any()))
        .thenReturn(Optional.of(articleData));

    DataFetchingEnvironment dfe = mock(DataFetchingEnvironment.class);
    when(dfe.getLocalContext()).thenReturn(commentData);

    DataFetcherResult<io.spring.graphql.types.Article> result =
        articleDatafetcher.getCommentArticle(dfe);
    assertNotNull(result);
  }

  @Test
  public void should_get_user_feed_with_first_param() {
    io.spring.graphql.types.Profile profile =
        io.spring.graphql.types.Profile.newBuilder().username("testuser").build();
    when(userRepository.findByUsername(eq("testuser"))).thenReturn(Optional.of(user));
    CursorPager<ArticleData> pager =
        new CursorPager<>(Arrays.asList(articleData), Direction.NEXT, false);
    when(articleQueryService.findUserFeedWithCursor(eq(user), any())).thenReturn(pager);

    DgsDataFetchingEnvironment dfe = mock(DgsDataFetchingEnvironment.class);
    when(dfe.getSource()).thenReturn(profile);

    DataFetcherResult<ArticlesConnection> result =
        articleDatafetcher.userFeed(10, null, null, null, dfe);
    assertNotNull(result);
    assertEquals(1, result.getData().getEdges().size());
  }

  @Test
  public void should_get_user_feed_with_last_param() {
    io.spring.graphql.types.Profile profile =
        io.spring.graphql.types.Profile.newBuilder().username("testuser").build();
    when(userRepository.findByUsername(eq("testuser"))).thenReturn(Optional.of(user));
    CursorPager<ArticleData> pager =
        new CursorPager<>(Arrays.asList(articleData), Direction.PREV, false);
    when(articleQueryService.findUserFeedWithCursor(eq(user), any())).thenReturn(pager);

    DgsDataFetchingEnvironment dfe = mock(DgsDataFetchingEnvironment.class);
    when(dfe.getSource()).thenReturn(profile);

    DataFetcherResult<ArticlesConnection> result =
        articleDatafetcher.userFeed(null, null, 10, null, dfe);
    assertNotNull(result);
  }

  @Test
  public void should_throw_when_user_feed_user_not_found() {
    io.spring.graphql.types.Profile profile =
        io.spring.graphql.types.Profile.newBuilder().username("unknown").build();
    when(userRepository.findByUsername(eq("unknown"))).thenReturn(Optional.empty());

    DgsDataFetchingEnvironment dfe = mock(DgsDataFetchingEnvironment.class);
    when(dfe.getSource()).thenReturn(profile);

    assertThrows(
        ResourceNotFoundException.class,
        () -> articleDatafetcher.userFeed(10, null, null, null, dfe));
  }

  @Test
  public void should_get_user_favorites_with_first() {
    io.spring.graphql.types.Profile profile =
        io.spring.graphql.types.Profile.newBuilder().username("testuser").build();
    CursorPager<ArticleData> pager =
        new CursorPager<>(Arrays.asList(articleData), Direction.NEXT, false);
    when(articleQueryService.findRecentArticlesWithCursor(any(), any(), any(), any(), any()))
        .thenReturn(pager);

    DgsDataFetchingEnvironment dfe = mock(DgsDataFetchingEnvironment.class);
    when(dfe.getSource()).thenReturn(profile);

    DataFetcherResult<ArticlesConnection> result =
        articleDatafetcher.userFavorites(10, null, null, null, dfe);
    assertNotNull(result);
  }

  @Test
  public void should_get_user_favorites_with_last() {
    io.spring.graphql.types.Profile profile =
        io.spring.graphql.types.Profile.newBuilder().username("testuser").build();
    CursorPager<ArticleData> pager =
        new CursorPager<>(Arrays.asList(articleData), Direction.PREV, false);
    when(articleQueryService.findRecentArticlesWithCursor(any(), any(), any(), any(), any()))
        .thenReturn(pager);

    DgsDataFetchingEnvironment dfe = mock(DgsDataFetchingEnvironment.class);
    when(dfe.getSource()).thenReturn(profile);

    DataFetcherResult<ArticlesConnection> result =
        articleDatafetcher.userFavorites(null, null, 10, null, dfe);
    assertNotNull(result);
  }

  @Test
  public void should_throw_when_user_favorites_missing_first_and_last() {
    DgsDataFetchingEnvironment dfe = mock(DgsDataFetchingEnvironment.class);
    assertThrows(
        IllegalArgumentException.class,
        () -> articleDatafetcher.userFavorites(null, null, null, null, dfe));
  }

  @Test
  public void should_get_user_articles_with_first() {
    io.spring.graphql.types.Profile profile =
        io.spring.graphql.types.Profile.newBuilder().username("testuser").build();
    CursorPager<ArticleData> pager =
        new CursorPager<>(Arrays.asList(articleData), Direction.NEXT, false);
    when(articleQueryService.findRecentArticlesWithCursor(any(), any(), any(), any(), any()))
        .thenReturn(pager);

    DgsDataFetchingEnvironment dfe = mock(DgsDataFetchingEnvironment.class);
    when(dfe.getSource()).thenReturn(profile);

    DataFetcherResult<ArticlesConnection> result =
        articleDatafetcher.userArticles(10, null, null, null, dfe);
    assertNotNull(result);
  }

  @Test
  public void should_get_user_articles_with_last() {
    io.spring.graphql.types.Profile profile =
        io.spring.graphql.types.Profile.newBuilder().username("testuser").build();
    CursorPager<ArticleData> pager =
        new CursorPager<>(Arrays.asList(articleData), Direction.PREV, false);
    when(articleQueryService.findRecentArticlesWithCursor(any(), any(), any(), any(), any()))
        .thenReturn(pager);

    DgsDataFetchingEnvironment dfe = mock(DgsDataFetchingEnvironment.class);
    when(dfe.getSource()).thenReturn(profile);

    DataFetcherResult<ArticlesConnection> result =
        articleDatafetcher.userArticles(null, null, 10, null, dfe);
    assertNotNull(result);
  }

  @Test
  public void should_throw_when_user_articles_missing_first_and_last() {
    DgsDataFetchingEnvironment dfe = mock(DgsDataFetchingEnvironment.class);
    assertThrows(
        IllegalArgumentException.class,
        () -> articleDatafetcher.userArticles(null, null, null, null, dfe));
  }
}
