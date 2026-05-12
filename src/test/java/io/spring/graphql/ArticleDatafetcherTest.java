package io.spring.graphql;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

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
import java.util.ArrayList;
import java.util.Arrays;
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
  public void setUp() {
    user = new User("test@test.com", "testuser", "123", "bio", "image");
    articleDatafetcher = new ArticleDatafetcher(articleQueryService, userRepository);
  }

  @AfterEach
  public void tearDown() {
    SecurityContextHolder.clearContext();
  }

  private void authenticateUser(User u) {
    TestingAuthenticationToken auth = new TestingAuthenticationToken(u, null);
    SecurityContextHolder.getContext().setAuthentication(auth);
  }

  private void setAnonymous() {
    SecurityContextHolder.getContext()
        .setAuthentication(
            new AnonymousAuthenticationToken(
                "key", "anonymous", AuthorityUtils.createAuthorityList("ROLE_ANONYMOUS")));
  }

  private ArticleData createArticleData(String seed) {
    DateTime now = new DateTime();
    return new ArticleData(
        seed + "id",
        "slug-" + seed,
        "title " + seed,
        "desc " + seed,
        "body " + seed,
        false,
        0,
        now,
        now,
        new ArrayList<>(),
        new ProfileData(user.getId(), user.getUsername(), user.getBio(), user.getImage(), false));
  }

  @Test
  public void should_find_article_by_slug_success() {
    authenticateUser(user);
    ArticleData articleData = createArticleData("1");
    when(articleQueryService.findBySlug(eq("slug-1"), eq(user)))
        .thenReturn(Optional.of(articleData));

    DataFetcherResult<Article> result = articleDatafetcher.findArticleBySlug("slug-1");
    assertNotNull(result);
    assertEquals("slug-1", result.getData().getSlug());
    assertEquals("title 1", result.getData().getTitle());
  }

  @Test
  public void should_throw_not_found_when_article_slug_not_exists() {
    authenticateUser(user);
    when(articleQueryService.findBySlug(eq("nonexistent"), eq(user))).thenReturn(Optional.empty());
    assertThrows(
        ResourceNotFoundException.class, () -> articleDatafetcher.findArticleBySlug("nonexistent"));
  }

  @Test
  public void should_find_article_by_slug_without_auth() {
    setAnonymous();
    ArticleData articleData = createArticleData("1");
    when(articleQueryService.findBySlug(eq("slug-1"), eq(null)))
        .thenReturn(Optional.of(articleData));

    DataFetcherResult<Article> result = articleDatafetcher.findArticleBySlug("slug-1");
    assertNotNull(result);
  }

  @Test
  public void should_get_article_from_article_payload() {
    authenticateUser(user);
    io.spring.core.article.Article coreArticle =
        new io.spring.core.article.Article(
            "Test Title", "desc", "body", Arrays.asList("java"), user.getId());
    ArticleData articleData = createArticleData("1");
    when(articleQueryService.findById(eq(coreArticle.getId()), eq(user)))
        .thenReturn(Optional.of(articleData));

    DataFetchingEnvironment dfe = mock(DataFetchingEnvironment.class);
    when(dfe.getLocalContext()).thenReturn(coreArticle);

    DataFetcherResult<Article> result = articleDatafetcher.getArticle(dfe);
    assertNotNull(result);
    assertNotNull(result.getData());
  }

  @Test
  public void should_throw_not_found_when_article_payload_article_not_found() {
    authenticateUser(user);
    io.spring.core.article.Article coreArticle =
        new io.spring.core.article.Article(
            "Test Title", "desc", "body", Arrays.asList("java"), user.getId());
    when(articleQueryService.findById(eq(coreArticle.getId()), eq(user)))
        .thenReturn(Optional.empty());

    DataFetchingEnvironment dfe = mock(DataFetchingEnvironment.class);
    when(dfe.getLocalContext()).thenReturn(coreArticle);

    assertThrows(ResourceNotFoundException.class, () -> articleDatafetcher.getArticle(dfe));
  }

  @Test
  public void should_get_comment_article() {
    authenticateUser(user);
    DateTime now = new DateTime();
    CommentData commentData =
        new CommentData(
            "commentId",
            "body",
            "articleId",
            now,
            now,
            new ProfileData(user.getId(), user.getUsername(), "", "", false));

    ArticleData articleData = createArticleData("1");
    when(articleQueryService.findById(eq("articleId"), eq(user)))
        .thenReturn(Optional.of(articleData));

    DataFetchingEnvironment dfe = mock(DataFetchingEnvironment.class);
    when(dfe.getLocalContext()).thenReturn(commentData);

    DataFetcherResult<Article> result = articleDatafetcher.getCommentArticle(dfe);
    assertNotNull(result);
    assertNotNull(result.getData());
  }

  @Test
  public void should_get_articles_with_first_param() {
    authenticateUser(user);
    ArticleData articleData = createArticleData("1");
    CursorPager<ArticleData> pager =
        new CursorPager<>(Arrays.asList(articleData), Direction.NEXT, false);
    when(articleQueryService.findRecentArticlesWithCursor(any(), any(), any(), any(), any()))
        .thenReturn(pager);

    com.netflix.graphql.dgs.DgsDataFetchingEnvironment dfe =
        mock(com.netflix.graphql.dgs.DgsDataFetchingEnvironment.class);

    DataFetcherResult<ArticlesConnection> result =
        articleDatafetcher.getArticles(10, null, null, null, null, null, null, dfe);
    assertNotNull(result);
    assertEquals(1, result.getData().getEdges().size());
  }

  @Test
  public void should_get_articles_with_last_param() {
    authenticateUser(user);
    ArticleData articleData = createArticleData("1");
    CursorPager<ArticleData> pager =
        new CursorPager<>(Arrays.asList(articleData), Direction.PREV, false);
    when(articleQueryService.findRecentArticlesWithCursor(any(), any(), any(), any(), any()))
        .thenReturn(pager);

    com.netflix.graphql.dgs.DgsDataFetchingEnvironment dfe =
        mock(com.netflix.graphql.dgs.DgsDataFetchingEnvironment.class);

    DataFetcherResult<ArticlesConnection> result =
        articleDatafetcher.getArticles(null, null, 10, null, null, null, null, dfe);
    assertNotNull(result);
  }

  @Test
  public void should_throw_when_both_first_and_last_are_null_get_articles() {
    com.netflix.graphql.dgs.DgsDataFetchingEnvironment dfe =
        mock(com.netflix.graphql.dgs.DgsDataFetchingEnvironment.class);
    assertThrows(
        IllegalArgumentException.class,
        () -> articleDatafetcher.getArticles(null, null, null, null, null, null, null, dfe));
  }

  @Test
  public void should_get_feed_with_first() {
    authenticateUser(user);
    CursorPager<ArticleData> pager = new CursorPager<>(new ArrayList<>(), Direction.NEXT, false);
    when(articleQueryService.findUserFeedWithCursor(any(), any())).thenReturn(pager);

    com.netflix.graphql.dgs.DgsDataFetchingEnvironment dfe =
        mock(com.netflix.graphql.dgs.DgsDataFetchingEnvironment.class);

    DataFetcherResult<ArticlesConnection> result =
        articleDatafetcher.getFeed(10, null, null, null, dfe);
    assertNotNull(result);
    assertEquals(0, result.getData().getEdges().size());
  }

  @Test
  public void should_get_feed_with_last() {
    authenticateUser(user);
    CursorPager<ArticleData> pager = new CursorPager<>(new ArrayList<>(), Direction.PREV, false);
    when(articleQueryService.findUserFeedWithCursor(any(), any())).thenReturn(pager);

    com.netflix.graphql.dgs.DgsDataFetchingEnvironment dfe =
        mock(com.netflix.graphql.dgs.DgsDataFetchingEnvironment.class);

    DataFetcherResult<ArticlesConnection> result =
        articleDatafetcher.getFeed(null, null, 10, null, dfe);
    assertNotNull(result);
  }

  @Test
  public void should_throw_when_both_first_and_last_null_feed() {
    com.netflix.graphql.dgs.DgsDataFetchingEnvironment dfe =
        mock(com.netflix.graphql.dgs.DgsDataFetchingEnvironment.class);
    assertThrows(
        IllegalArgumentException.class,
        () -> articleDatafetcher.getFeed(null, null, null, null, dfe));
  }

  @Test
  public void should_get_user_feed_with_first() {
    authenticateUser(user);
    io.spring.graphql.types.Profile profile =
        io.spring.graphql.types.Profile.newBuilder().username("targetuser").build();
    User target = new User("target@test.com", "targetuser", "123", "", "");
    when(userRepository.findByUsername(eq("targetuser"))).thenReturn(Optional.of(target));
    CursorPager<ArticleData> pager = new CursorPager<>(new ArrayList<>(), Direction.NEXT, false);
    when(articleQueryService.findUserFeedWithCursor(any(), any())).thenReturn(pager);

    com.netflix.graphql.dgs.DgsDataFetchingEnvironment dfe =
        mock(com.netflix.graphql.dgs.DgsDataFetchingEnvironment.class);
    when(dfe.getSource()).thenReturn(profile);

    DataFetcherResult<ArticlesConnection> result =
        articleDatafetcher.userFeed(10, null, null, null, dfe);
    assertNotNull(result);
  }

  @Test
  public void should_get_user_feed_with_last() {
    authenticateUser(user);
    io.spring.graphql.types.Profile profile =
        io.spring.graphql.types.Profile.newBuilder().username("targetuser").build();
    User target = new User("target@test.com", "targetuser", "123", "", "");
    when(userRepository.findByUsername(eq("targetuser"))).thenReturn(Optional.of(target));
    CursorPager<ArticleData> pager = new CursorPager<>(new ArrayList<>(), Direction.PREV, false);
    when(articleQueryService.findUserFeedWithCursor(any(), any())).thenReturn(pager);

    com.netflix.graphql.dgs.DgsDataFetchingEnvironment dfe =
        mock(com.netflix.graphql.dgs.DgsDataFetchingEnvironment.class);
    when(dfe.getSource()).thenReturn(profile);

    DataFetcherResult<ArticlesConnection> result =
        articleDatafetcher.userFeed(null, null, 10, null, dfe);
    assertNotNull(result);
  }

  @Test
  public void should_throw_when_both_null_user_feed() {
    com.netflix.graphql.dgs.DgsDataFetchingEnvironment dfe =
        mock(com.netflix.graphql.dgs.DgsDataFetchingEnvironment.class);
    assertThrows(
        IllegalArgumentException.class,
        () -> articleDatafetcher.userFeed(null, null, null, null, dfe));
  }

  @Test
  public void should_throw_not_found_user_feed_target_not_exists() {
    authenticateUser(user);
    io.spring.graphql.types.Profile profile =
        io.spring.graphql.types.Profile.newBuilder().username("nonexistent").build();
    when(userRepository.findByUsername(eq("nonexistent"))).thenReturn(Optional.empty());

    com.netflix.graphql.dgs.DgsDataFetchingEnvironment dfe =
        mock(com.netflix.graphql.dgs.DgsDataFetchingEnvironment.class);
    when(dfe.getSource()).thenReturn(profile);

    assertThrows(
        ResourceNotFoundException.class,
        () -> articleDatafetcher.userFeed(10, null, null, null, dfe));
  }

  @Test
  public void should_get_user_favorites_with_first() {
    authenticateUser(user);
    io.spring.graphql.types.Profile profile =
        io.spring.graphql.types.Profile.newBuilder().username("testuser").build();
    CursorPager<ArticleData> pager = new CursorPager<>(new ArrayList<>(), Direction.NEXT, false);
    when(articleQueryService.findRecentArticlesWithCursor(any(), any(), any(), any(), any()))
        .thenReturn(pager);

    com.netflix.graphql.dgs.DgsDataFetchingEnvironment dfe =
        mock(com.netflix.graphql.dgs.DgsDataFetchingEnvironment.class);
    when(dfe.getSource()).thenReturn(profile);

    DataFetcherResult<ArticlesConnection> result =
        articleDatafetcher.userFavorites(10, null, null, null, dfe);
    assertNotNull(result);
  }

  @Test
  public void should_get_user_favorites_with_last() {
    authenticateUser(user);
    io.spring.graphql.types.Profile profile =
        io.spring.graphql.types.Profile.newBuilder().username("testuser").build();
    CursorPager<ArticleData> pager = new CursorPager<>(new ArrayList<>(), Direction.PREV, false);
    when(articleQueryService.findRecentArticlesWithCursor(any(), any(), any(), any(), any()))
        .thenReturn(pager);

    com.netflix.graphql.dgs.DgsDataFetchingEnvironment dfe =
        mock(com.netflix.graphql.dgs.DgsDataFetchingEnvironment.class);
    when(dfe.getSource()).thenReturn(profile);

    DataFetcherResult<ArticlesConnection> result =
        articleDatafetcher.userFavorites(null, null, 10, null, dfe);
    assertNotNull(result);
  }

  @Test
  public void should_throw_when_both_null_user_favorites() {
    com.netflix.graphql.dgs.DgsDataFetchingEnvironment dfe =
        mock(com.netflix.graphql.dgs.DgsDataFetchingEnvironment.class);
    assertThrows(
        IllegalArgumentException.class,
        () -> articleDatafetcher.userFavorites(null, null, null, null, dfe));
  }

  @Test
  public void should_get_user_articles_with_first() {
    authenticateUser(user);
    io.spring.graphql.types.Profile profile =
        io.spring.graphql.types.Profile.newBuilder().username("testuser").build();
    CursorPager<ArticleData> pager = new CursorPager<>(new ArrayList<>(), Direction.NEXT, false);
    when(articleQueryService.findRecentArticlesWithCursor(any(), any(), any(), any(), any()))
        .thenReturn(pager);

    com.netflix.graphql.dgs.DgsDataFetchingEnvironment dfe =
        mock(com.netflix.graphql.dgs.DgsDataFetchingEnvironment.class);
    when(dfe.getSource()).thenReturn(profile);

    DataFetcherResult<ArticlesConnection> result =
        articleDatafetcher.userArticles(10, null, null, null, dfe);
    assertNotNull(result);
  }

  @Test
  public void should_get_user_articles_with_last() {
    authenticateUser(user);
    io.spring.graphql.types.Profile profile =
        io.spring.graphql.types.Profile.newBuilder().username("testuser").build();
    CursorPager<ArticleData> pager = new CursorPager<>(new ArrayList<>(), Direction.PREV, false);
    when(articleQueryService.findRecentArticlesWithCursor(any(), any(), any(), any(), any()))
        .thenReturn(pager);

    com.netflix.graphql.dgs.DgsDataFetchingEnvironment dfe =
        mock(com.netflix.graphql.dgs.DgsDataFetchingEnvironment.class);
    when(dfe.getSource()).thenReturn(profile);

    DataFetcherResult<ArticlesConnection> result =
        articleDatafetcher.userArticles(null, null, 10, null, dfe);
    assertNotNull(result);
  }

  @Test
  public void should_throw_when_both_null_user_articles() {
    com.netflix.graphql.dgs.DgsDataFetchingEnvironment dfe =
        mock(com.netflix.graphql.dgs.DgsDataFetchingEnvironment.class);
    assertThrows(
        IllegalArgumentException.class,
        () -> articleDatafetcher.userArticles(null, null, null, null, dfe));
  }

  @Test
  public void should_build_article_result_with_all_fields() {
    authenticateUser(user);
    ArticleData articleData = createArticleData("full");
    when(articleQueryService.findBySlug(eq("slug-full"), eq(user)))
        .thenReturn(Optional.of(articleData));

    DataFetcherResult<Article> result = articleDatafetcher.findArticleBySlug("slug-full");
    Article article = result.getData();
    assertNotNull(article.getCreatedAt());
    assertNotNull(article.getUpdatedAt());
    assertEquals("body full", article.getBody());
    assertEquals("desc full", article.getDescription());
    assertEquals("title full", article.getTitle());
    assertFalse(article.getFavorited());
    assertEquals(0, article.getFavoritesCount());
  }

  @Test
  public void should_get_articles_with_paging_has_next() {
    authenticateUser(user);
    ArticleData a1 = createArticleData("1");
    ArticleData a2 = createArticleData("2");
    CursorPager<ArticleData> pager = new CursorPager<>(Arrays.asList(a1, a2), Direction.NEXT, true);
    when(articleQueryService.findRecentArticlesWithCursor(any(), any(), any(), any(), any()))
        .thenReturn(pager);

    com.netflix.graphql.dgs.DgsDataFetchingEnvironment dfe =
        mock(com.netflix.graphql.dgs.DgsDataFetchingEnvironment.class);

    DataFetcherResult<ArticlesConnection> result =
        articleDatafetcher.getArticles(10, null, null, null, null, null, null, dfe);
    assertNotNull(result);
    assertEquals(2, result.getData().getEdges().size());
    assertTrue(result.getData().getPageInfo().isHasNextPage());
  }
}
