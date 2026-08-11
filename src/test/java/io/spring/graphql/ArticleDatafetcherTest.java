package io.spring.graphql;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
import io.spring.core.user.User;
import io.spring.core.user.UserRepository;
import io.spring.graphql.types.ArticlesConnection;
import io.spring.graphql.types.Profile;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import org.joda.time.DateTime;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class ArticleDatafetcherTest extends GraphQLTestBase {

  @Mock private ArticleQueryService articleQueryService;
  @Mock private UserRepository userRepository;

  private ArticleDatafetcher articleDatafetcher;
  private User user;
  private ArticleData articleData;

  @BeforeEach
  public void setUp() {
    articleDatafetcher = new ArticleDatafetcher(articleQueryService, userRepository);
    user = new User("a@test.com", "a", "123", "bio", "image");
    articleData =
        new ArticleData(
            "article-id",
            "slug",
            "title",
            "desc",
            "body",
            false,
            0,
            new DateTime(),
            new DateTime(),
            Arrays.asList("java"),
            new ProfileData(user.getId(), user.getUsername(), "bio", "image", false));
  }

  private CursorPager<ArticleData> onePage(Direction direction, boolean hasExtra) {
    return new CursorPager<>(Arrays.asList(articleData), direction, hasExtra);
  }

  private DgsDataFetchingEnvironment profileEnvironment() {
    DgsDataFetchingEnvironment dfe = mock(DgsDataFetchingEnvironment.class);
    when(dfe.<Profile>getSource())
        .thenReturn(Profile.newBuilder().username(user.getUsername()).build());
    return dfe;
  }

  @Test
  public void should_get_feed_forward() {
    setCurrentUser(user);
    when(articleQueryService.findUserFeedWithCursor(eq(user), any()))
        .thenReturn(onePage(Direction.NEXT, true));

    DataFetcherResult<ArticlesConnection> result =
        articleDatafetcher.getFeed(10, null, null, null, mock(DgsDataFetchingEnvironment.class));

    Assertions.assertEquals(1, result.getData().getEdges().size());
    Assertions.assertEquals("slug", result.getData().getEdges().get(0).getNode().getSlug());
    Assertions.assertTrue(result.getData().getPageInfo().isHasNextPage());
  }

  @Test
  public void should_get_feed_backward() {
    when(articleQueryService.findUserFeedWithCursor(isNull(), any()))
        .thenReturn(onePage(Direction.PREV, true));

    DataFetcherResult<ArticlesConnection> result =
        articleDatafetcher.getFeed(
            null,
            null,
            10,
            String.valueOf(new DateTime().getMillis()),
            mock(DgsDataFetchingEnvironment.class));

    Assertions.assertTrue(result.getData().getPageInfo().isHasPreviousPage());
    ArgumentCaptor<CursorPageParameter<DateTime>> captor =
        ArgumentCaptor.forClass(CursorPageParameter.class);
    verify(articleQueryService).findUserFeedWithCursor(isNull(), captor.capture());
    Assertions.assertEquals(Direction.PREV, captor.getValue().getDirection());
  }

  @Test
  public void should_reject_feed_without_first_and_last() {
    DgsDataFetchingEnvironment dfe = mock(DgsDataFetchingEnvironment.class);

    Assertions.assertThrows(
        IllegalArgumentException.class,
        () -> articleDatafetcher.getFeed(null, null, null, null, dfe));
  }

  @Test
  public void should_get_user_feed() {
    when(userRepository.findByUsername(eq(user.getUsername()))).thenReturn(Optional.of(user));
    when(articleQueryService.findUserFeedWithCursor(eq(user), any()))
        .thenReturn(onePage(Direction.NEXT, false));

    DataFetcherResult<ArticlesConnection> result =
        articleDatafetcher.userFeed(10, null, null, null, profileEnvironment());

    Assertions.assertEquals(1, result.getData().getEdges().size());
  }

  @Test
  public void should_get_user_feed_backward() {
    when(userRepository.findByUsername(eq(user.getUsername()))).thenReturn(Optional.of(user));
    when(articleQueryService.findUserFeedWithCursor(eq(user), any()))
        .thenReturn(onePage(Direction.PREV, false));

    DataFetcherResult<ArticlesConnection> result =
        articleDatafetcher.userFeed(null, null, 10, null, profileEnvironment());

    Assertions.assertEquals(1, result.getData().getEdges().size());
  }

  @Test
  public void should_reject_user_feed_without_first_and_last() {
    DgsDataFetchingEnvironment dfe = mock(DgsDataFetchingEnvironment.class);

    Assertions.assertThrows(
        IllegalArgumentException.class,
        () -> articleDatafetcher.userFeed(null, null, null, null, dfe));
  }

  @Test
  public void should_throw_not_found_for_unknown_user_feed() {
    when(userRepository.findByUsername(eq(user.getUsername()))).thenReturn(Optional.empty());
    DgsDataFetchingEnvironment dfe = profileEnvironment();

    Assertions.assertThrows(
        ResourceNotFoundException.class,
        () -> articleDatafetcher.userFeed(10, null, null, null, dfe));
  }

  @Test
  public void should_get_user_favorites() {
    when(articleQueryService.findRecentArticlesWithCursor(
            isNull(), isNull(), eq(user.getUsername()), any(), isNull()))
        .thenReturn(onePage(Direction.NEXT, false));

    DataFetcherResult<ArticlesConnection> result =
        articleDatafetcher.userFavorites(10, null, null, null, profileEnvironment());

    Assertions.assertEquals(1, result.getData().getEdges().size());
  }

  @Test
  public void should_get_user_favorites_backward() {
    when(articleQueryService.findRecentArticlesWithCursor(
            isNull(), isNull(), eq(user.getUsername()), any(), isNull()))
        .thenReturn(onePage(Direction.PREV, false));

    DataFetcherResult<ArticlesConnection> result =
        articleDatafetcher.userFavorites(null, null, 10, null, profileEnvironment());

    Assertions.assertEquals(1, result.getData().getEdges().size());
  }

  @Test
  public void should_reject_user_favorites_without_first_and_last() {
    DgsDataFetchingEnvironment dfe = mock(DgsDataFetchingEnvironment.class);

    Assertions.assertThrows(
        IllegalArgumentException.class,
        () -> articleDatafetcher.userFavorites(null, null, null, null, dfe));
  }

  @Test
  public void should_get_user_articles() {
    when(articleQueryService.findRecentArticlesWithCursor(
            isNull(), eq(user.getUsername()), isNull(), any(), isNull()))
        .thenReturn(onePage(Direction.NEXT, false));

    DataFetcherResult<ArticlesConnection> result =
        articleDatafetcher.userArticles(10, null, null, null, profileEnvironment());

    Assertions.assertEquals(1, result.getData().getEdges().size());
  }

  @Test
  public void should_get_user_articles_backward() {
    when(articleQueryService.findRecentArticlesWithCursor(
            isNull(), eq(user.getUsername()), isNull(), any(), isNull()))
        .thenReturn(onePage(Direction.PREV, false));

    DataFetcherResult<ArticlesConnection> result =
        articleDatafetcher.userArticles(null, null, 10, null, profileEnvironment());

    Assertions.assertEquals(1, result.getData().getEdges().size());
  }

  @Test
  public void should_reject_user_articles_without_first_and_last() {
    DgsDataFetchingEnvironment dfe = mock(DgsDataFetchingEnvironment.class);

    Assertions.assertThrows(
        IllegalArgumentException.class,
        () -> articleDatafetcher.userArticles(null, null, null, null, dfe));
  }

  @Test
  public void should_get_articles_with_filters() {
    when(articleQueryService.findRecentArticlesWithCursor(
            eq("java"), eq("a"), eq("b"), any(), isNull()))
        .thenReturn(onePage(Direction.NEXT, false));

    DataFetcherResult<ArticlesConnection> result =
        articleDatafetcher.getArticles(
            10, null, null, null, "a", "b", "java", mock(DgsDataFetchingEnvironment.class));

    Assertions.assertEquals(1, result.getData().getEdges().size());
  }

  @Test
  public void should_get_articles_backward() {
    when(articleQueryService.findRecentArticlesWithCursor(
            isNull(), isNull(), isNull(), any(), isNull()))
        .thenReturn(new CursorPager<>(new ArrayList<>(), Direction.PREV, false));

    DataFetcherResult<ArticlesConnection> result =
        articleDatafetcher.getArticles(
            null, null, 10, null, null, null, null, mock(DgsDataFetchingEnvironment.class));

    Assertions.assertTrue(result.getData().getEdges().isEmpty());
    Assertions.assertNull(result.getData().getPageInfo().getEndCursor());
  }

  @Test
  public void should_reject_articles_without_first_and_last() {
    DgsDataFetchingEnvironment dfe = mock(DgsDataFetchingEnvironment.class);

    Assertions.assertThrows(
        IllegalArgumentException.class,
        () -> articleDatafetcher.getArticles(null, null, null, null, null, null, null, dfe));
  }

  @Test
  public void should_get_article_from_local_context() {
    io.spring.core.article.Article article =
        new io.spring.core.article.Article(
            "title", "desc", "body", Collections.emptyList(), user.getId());
    DataFetchingEnvironment dfe = mock(DataFetchingEnvironment.class);
    when(dfe.<io.spring.core.article.Article>getLocalContext()).thenReturn(article);
    when(articleQueryService.findById(eq(article.getId()), isNull()))
        .thenReturn(Optional.of(articleData));

    DataFetcherResult<io.spring.graphql.types.Article> result = articleDatafetcher.getArticle(dfe);

    Assertions.assertEquals("slug", result.getData().getSlug());
  }

  @Test
  public void should_throw_not_found_for_unknown_article() {
    io.spring.core.article.Article article =
        new io.spring.core.article.Article(
            "title", "desc", "body", Collections.emptyList(), user.getId());
    DataFetchingEnvironment dfe = mock(DataFetchingEnvironment.class);
    when(dfe.<io.spring.core.article.Article>getLocalContext()).thenReturn(article);
    when(articleQueryService.findById(eq(article.getId()), isNull())).thenReturn(Optional.empty());

    Assertions.assertThrows(
        ResourceNotFoundException.class, () -> articleDatafetcher.getArticle(dfe));
  }

  @Test
  public void should_get_comment_article() {
    CommentData commentData =
        new CommentData(
            "comment-id",
            "content",
            "article-id",
            new DateTime(),
            new DateTime(),
            new ProfileData(user.getId(), user.getUsername(), "bio", "image", false));
    DataFetchingEnvironment dfe = mock(DataFetchingEnvironment.class);
    when(dfe.<CommentData>getLocalContext()).thenReturn(commentData);
    when(articleQueryService.findById(eq("article-id"), isNull()))
        .thenReturn(Optional.of(articleData));

    DataFetcherResult<io.spring.graphql.types.Article> result =
        articleDatafetcher.getCommentArticle(dfe);

    Assertions.assertEquals("slug", result.getData().getSlug());
  }

  @Test
  public void should_throw_not_found_for_unknown_comment_article() {
    CommentData commentData =
        new CommentData(
            "comment-id",
            "content",
            "unknown",
            new DateTime(),
            new DateTime(),
            new ProfileData(user.getId(), user.getUsername(), "bio", "image", false));
    DataFetchingEnvironment dfe = mock(DataFetchingEnvironment.class);
    when(dfe.<CommentData>getLocalContext()).thenReturn(commentData);
    when(articleQueryService.findById(eq("unknown"), isNull())).thenReturn(Optional.empty());

    Assertions.assertThrows(
        ResourceNotFoundException.class, () -> articleDatafetcher.getCommentArticle(dfe));
  }

  @Test
  public void should_find_article_by_slug() {
    when(articleQueryService.findBySlug(eq("slug"), isNull())).thenReturn(Optional.of(articleData));

    DataFetcherResult<io.spring.graphql.types.Article> result =
        articleDatafetcher.findArticleBySlug("slug");

    Assertions.assertEquals("title", result.getData().getTitle());
    Assertions.assertEquals(Arrays.asList("java"), result.getData().getTagList());
  }

  @Test
  public void should_throw_not_found_for_unknown_slug() {
    when(articleQueryService.findBySlug(eq("unknown"), isNull())).thenReturn(Optional.empty());

    Assertions.assertThrows(
        ResourceNotFoundException.class, () -> articleDatafetcher.findArticleBySlug("unknown"));
  }
}
