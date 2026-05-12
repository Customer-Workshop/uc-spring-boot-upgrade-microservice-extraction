package io.spring.graphql;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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
import io.spring.core.article.Article;
import io.spring.core.user.User;
import io.spring.core.user.UserRepository;
import io.spring.graphql.types.ArticlesConnection;
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

  @BeforeEach
  public void setUp() {
    articleDatafetcher = new ArticleDatafetcher(articleQueryService, userRepository);
    user = new User("test@test.com", "testuser", "password", "bio", "image");
    setAnonymous();
  }

  private void setAnonymous() {
    AnonymousAuthenticationToken auth =
        new AnonymousAuthenticationToken(
            "key", "anonymous", AuthorityUtils.createAuthorityList("ROLE_ANONYMOUS"));
    SecurityContextHolder.getContext().setAuthentication(auth);
  }

  @AfterEach
  public void tearDown() {
    SecurityContextHolder.clearContext();
  }

  private ArticleData createArticleData(String id, String slug) {
    return new ArticleData(
        id,
        slug,
        "Title " + slug,
        "Desc",
        "Body",
        false,
        0,
        new DateTime(),
        new DateTime(),
        Collections.emptyList(),
        new ProfileData(user.getId(), "testuser", "bio", "image", false));
  }

  @Test
  public void should_get_feed_with_first_parameter() {
    TestingAuthenticationToken auth = new TestingAuthenticationToken(user, null);
    SecurityContextHolder.getContext().setAuthentication(auth);
    ArticleData articleData = createArticleData("id1", "slug1");
    CursorPager<ArticleData> pager =
        new CursorPager<>(Arrays.asList(articleData), Direction.NEXT, false);
    when(articleQueryService.findUserFeedWithCursor(any(), any())).thenReturn(pager);

    DataFetcherResult<ArticlesConnection> result =
        articleDatafetcher.getFeed(10, null, null, null, dfe);

    assertNotNull(result);
    assertEquals(1, result.getData().getEdges().size());
  }

  @Test
  public void should_get_feed_with_last_parameter() {
    TestingAuthenticationToken auth = new TestingAuthenticationToken(user, null);
    SecurityContextHolder.getContext().setAuthentication(auth);
    CursorPager<ArticleData> pager =
        new CursorPager<>(Collections.emptyList(), Direction.PREV, false);
    when(articleQueryService.findUserFeedWithCursor(any(), any())).thenReturn(pager);

    DataFetcherResult<ArticlesConnection> result =
        articleDatafetcher.getFeed(null, null, 10, null, dfe);

    assertNotNull(result);
    assertTrue(result.getData().getEdges().isEmpty());
  }

  @Test
  public void should_throw_when_feed_has_no_first_or_last() {
    assertThrows(
        IllegalArgumentException.class,
        () -> articleDatafetcher.getFeed(null, null, null, null, dfe));
  }

  @Test
  public void should_get_articles_with_first_parameter() {
    CursorPager<ArticleData> pager =
        new CursorPager<>(Collections.emptyList(), Direction.NEXT, false);
    when(articleQueryService.findRecentArticlesWithCursor(any(), any(), any(), any(), any()))
        .thenReturn(pager);

    DataFetcherResult<ArticlesConnection> result =
        articleDatafetcher.getArticles(10, null, null, null, null, null, null, dfe);

    assertNotNull(result);
  }

  @Test
  public void should_get_articles_with_last_parameter() {
    CursorPager<ArticleData> pager =
        new CursorPager<>(Collections.emptyList(), Direction.PREV, false);
    when(articleQueryService.findRecentArticlesWithCursor(any(), any(), any(), any(), any()))
        .thenReturn(pager);

    DataFetcherResult<ArticlesConnection> result =
        articleDatafetcher.getArticles(null, null, 10, null, null, null, null, dfe);

    assertNotNull(result);
  }

  @Test
  public void should_throw_when_articles_has_no_first_or_last() {
    assertThrows(
        IllegalArgumentException.class,
        () -> articleDatafetcher.getArticles(null, null, null, null, null, null, null, dfe));
  }

  @Test
  public void should_find_article_by_slug() {
    ArticleData articleData = createArticleData("id1", "test-slug");
    when(articleQueryService.findBySlug(eq("test-slug"), any()))
        .thenReturn(Optional.of(articleData));

    DataFetcherResult<io.spring.graphql.types.Article> result =
        articleDatafetcher.findArticleBySlug("test-slug");

    assertNotNull(result);
    assertEquals("test-slug", result.getData().getSlug());
    assertEquals("Body", result.getData().getBody());
  }

  @Test
  public void should_throw_not_found_for_nonexistent_slug() {
    when(articleQueryService.findBySlug(eq("nonexistent"), any())).thenReturn(Optional.empty());

    assertThrows(
        ResourceNotFoundException.class, () -> articleDatafetcher.findArticleBySlug("nonexistent"));
  }

  @Test
  public void should_get_article_from_article_payload() {
    Article coreArticle =
        new Article("Test Title", "Desc", "Body", Collections.emptyList(), user.getId());
    when(dataFetchingEnvironment.getLocalContext()).thenReturn(coreArticle);
    ArticleData articleData = createArticleData(coreArticle.getId(), "test-title");
    when(articleQueryService.findById(eq(coreArticle.getId()), any()))
        .thenReturn(Optional.of(articleData));

    DataFetcherResult<io.spring.graphql.types.Article> result =
        articleDatafetcher.getArticle(dataFetchingEnvironment);

    assertNotNull(result);
    assertEquals("test-title", result.getData().getSlug());
  }

  @Test
  public void should_get_article_from_comment() {
    CommentData commentData =
        new CommentData(
            "comment-id",
            "body",
            "article-id",
            new DateTime(),
            new DateTime(),
            new ProfileData(user.getId(), "testuser", "bio", "image", false));
    when(dataFetchingEnvironment.getLocalContext()).thenReturn(commentData);
    ArticleData articleData = createArticleData("article-id", "test-slug");
    when(articleQueryService.findById(eq("article-id"), any()))
        .thenReturn(Optional.of(articleData));

    DataFetcherResult<io.spring.graphql.types.Article> result =
        articleDatafetcher.getCommentArticle(dataFetchingEnvironment);

    assertNotNull(result);
    assertEquals("test-slug", result.getData().getSlug());
  }

  @Test
  public void should_get_user_feed_with_first_parameter() {
    io.spring.graphql.types.Profile profile =
        io.spring.graphql.types.Profile.newBuilder().username("targetuser").build();
    when(dfe.getSource()).thenReturn(profile);
    User target = new User("target@test.com", "targetuser", "pass", "", "");
    when(userRepository.findByUsername(eq("targetuser"))).thenReturn(Optional.of(target));
    CursorPager<ArticleData> pager =
        new CursorPager<>(Collections.emptyList(), Direction.NEXT, false);
    when(articleQueryService.findUserFeedWithCursor(any(), any())).thenReturn(pager);

    DataFetcherResult<ArticlesConnection> result =
        articleDatafetcher.userFeed(10, null, null, null, dfe);

    assertNotNull(result);
  }

  @Test
  public void should_get_user_feed_with_last_parameter() {
    io.spring.graphql.types.Profile profile =
        io.spring.graphql.types.Profile.newBuilder().username("targetuser").build();
    when(dfe.getSource()).thenReturn(profile);
    User target = new User("target@test.com", "targetuser", "pass", "", "");
    when(userRepository.findByUsername(eq("targetuser"))).thenReturn(Optional.of(target));
    CursorPager<ArticleData> pager =
        new CursorPager<>(Collections.emptyList(), Direction.PREV, false);
    when(articleQueryService.findUserFeedWithCursor(any(), any())).thenReturn(pager);

    DataFetcherResult<ArticlesConnection> result =
        articleDatafetcher.userFeed(null, null, 10, null, dfe);

    assertNotNull(result);
  }

  @Test
  public void should_throw_when_user_feed_has_no_first_or_last() {
    assertThrows(
        IllegalArgumentException.class,
        () -> articleDatafetcher.userFeed(null, null, null, null, dfe));
  }

  @Test
  public void should_throw_not_found_for_user_feed_with_unknown_user() {
    io.spring.graphql.types.Profile profile =
        io.spring.graphql.types.Profile.newBuilder().username("unknown").build();
    when(dfe.getSource()).thenReturn(profile);
    when(userRepository.findByUsername(eq("unknown"))).thenReturn(Optional.empty());

    assertThrows(
        ResourceNotFoundException.class,
        () -> articleDatafetcher.userFeed(10, null, null, null, dfe));
  }

  @Test
  public void should_get_user_favorites_with_first_parameter() {
    io.spring.graphql.types.Profile profile =
        io.spring.graphql.types.Profile.newBuilder().username("testuser").build();
    when(dfe.getSource()).thenReturn(profile);
    CursorPager<ArticleData> pager =
        new CursorPager<>(Collections.emptyList(), Direction.NEXT, false);
    when(articleQueryService.findRecentArticlesWithCursor(any(), any(), any(), any(), any()))
        .thenReturn(pager);

    DataFetcherResult<ArticlesConnection> result =
        articleDatafetcher.userFavorites(10, null, null, null, dfe);

    assertNotNull(result);
  }

  @Test
  public void should_get_user_favorites_with_last_parameter() {
    io.spring.graphql.types.Profile profile =
        io.spring.graphql.types.Profile.newBuilder().username("testuser").build();
    when(dfe.getSource()).thenReturn(profile);
    CursorPager<ArticleData> pager =
        new CursorPager<>(Collections.emptyList(), Direction.PREV, false);
    when(articleQueryService.findRecentArticlesWithCursor(any(), any(), any(), any(), any()))
        .thenReturn(pager);

    DataFetcherResult<ArticlesConnection> result =
        articleDatafetcher.userFavorites(null, null, 10, null, dfe);

    assertNotNull(result);
  }

  @Test
  public void should_throw_when_user_favorites_has_no_first_or_last() {
    assertThrows(
        IllegalArgumentException.class,
        () -> articleDatafetcher.userFavorites(null, null, null, null, dfe));
  }

  @Test
  public void should_get_user_articles_with_first_parameter() {
    io.spring.graphql.types.Profile profile =
        io.spring.graphql.types.Profile.newBuilder().username("testuser").build();
    when(dfe.getSource()).thenReturn(profile);
    CursorPager<ArticleData> pager =
        new CursorPager<>(Collections.emptyList(), Direction.NEXT, false);
    when(articleQueryService.findRecentArticlesWithCursor(any(), any(), any(), any(), any()))
        .thenReturn(pager);

    DataFetcherResult<ArticlesConnection> result =
        articleDatafetcher.userArticles(10, null, null, null, dfe);

    assertNotNull(result);
  }

  @Test
  public void should_get_user_articles_with_last_parameter() {
    io.spring.graphql.types.Profile profile =
        io.spring.graphql.types.Profile.newBuilder().username("testuser").build();
    when(dfe.getSource()).thenReturn(profile);
    CursorPager<ArticleData> pager =
        new CursorPager<>(Collections.emptyList(), Direction.PREV, false);
    when(articleQueryService.findRecentArticlesWithCursor(any(), any(), any(), any(), any()))
        .thenReturn(pager);

    DataFetcherResult<ArticlesConnection> result =
        articleDatafetcher.userArticles(null, null, 10, null, dfe);

    assertNotNull(result);
  }

  @Test
  public void should_throw_when_user_articles_has_no_first_or_last() {
    assertThrows(
        IllegalArgumentException.class,
        () -> articleDatafetcher.userArticles(null, null, null, null, dfe));
  }
}
