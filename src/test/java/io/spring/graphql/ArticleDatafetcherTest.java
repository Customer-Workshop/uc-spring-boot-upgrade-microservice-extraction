package io.spring.graphql;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
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
import io.spring.graphql.types.ArticlesConnection;
import io.spring.graphql.types.Profile;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.joda.time.DateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class ArticleDatafetcherTest extends GraphQLTestBase {

  @Mock private ArticleQueryService articleQueryService;

  @Mock private io.spring.core.user.UserRepository userRepository;

  @Mock private DgsDataFetchingEnvironment dgsDataFetchingEnvironment;

  @Mock private DataFetchingEnvironment dataFetchingEnvironment;

  @InjectMocks private ArticleDatafetcher articleDatafetcher;

  private User user;
  private ArticleData articleData;

  @BeforeEach
  public void setUp() {
    user = new User("john@jacob.com", "johnjacob", "123", "bio", "image");
    articleData = articleData("slug", user);
  }

  private ArticleData articleData(String slug, User author) {
    return new ArticleData(
        slug + "-id",
        slug,
        "title",
        "desc",
        "body",
        false,
        0,
        new DateTime(),
        new DateTime(),
        Arrays.asList("java"),
        new ProfileData(author.getId(), author.getUsername(), "bio", "image", false));
  }

  private CursorPager<ArticleData> pagerOf(ArticleData... data) {
    return new CursorPager<>(new java.util.ArrayList<>(Arrays.asList(data)), Direction.NEXT, true);
  }

  @Test
  public void should_get_feed_forward() {
    authenticate(user);
    when(articleQueryService.findUserFeedWithCursor(eq(user), any()))
        .thenReturn(pagerOf(articleData));

    DataFetcherResult<ArticlesConnection> result =
        articleDatafetcher.getFeed(10, null, null, null, dgsDataFetchingEnvironment);

    assertEquals(1, result.getData().getEdges().size());
    assertEquals("slug", result.getData().getEdges().get(0).getNode().getSlug());
    assertTrue(result.getData().getPageInfo().isHasNextPage());
    Map<String, ArticleData> localContext = (Map<String, ArticleData>) result.getLocalContext();
    assertTrue(localContext.containsKey("slug"));
  }

  @Test
  public void should_get_feed_backward_with_cursor() {
    anonymous();
    ArgumentCaptor<CursorPageParameter<DateTime>> captor =
        ArgumentCaptor.forClass(CursorPageParameter.class);
    when(articleQueryService.findUserFeedWithCursor(isNull(), captor.capture()))
        .thenReturn(pagerOf());

    DataFetcherResult<ArticlesConnection> result =
        articleDatafetcher.getFeed(null, null, 5, "1000", dgsDataFetchingEnvironment);

    assertTrue(result.getData().getEdges().isEmpty());
    assertEquals(Direction.PREV, captor.getValue().getDirection());
    assertEquals(1000L, captor.getValue().getCursor().getMillis());
  }

  @Test
  public void should_reject_feed_without_first_and_last() {
    assertThrows(
        IllegalArgumentException.class,
        () -> articleDatafetcher.getFeed(null, null, null, null, dgsDataFetchingEnvironment));
  }

  @Test
  public void should_get_user_feed_of_profile() {
    when(dgsDataFetchingEnvironment.<Profile>getSource())
        .thenReturn(Profile.newBuilder().username(user.getUsername()).build());
    when(userRepository.findByUsername(eq(user.getUsername()))).thenReturn(Optional.of(user));
    when(articleQueryService.findUserFeedWithCursor(eq(user), any()))
        .thenReturn(pagerOf(articleData));

    DataFetcherResult<ArticlesConnection> result =
        articleDatafetcher.userFeed(10, null, null, null, dgsDataFetchingEnvironment);

    assertEquals(1, result.getData().getEdges().size());
  }

  @Test
  public void should_get_user_feed_backward() {
    when(dgsDataFetchingEnvironment.<Profile>getSource())
        .thenReturn(Profile.newBuilder().username(user.getUsername()).build());
    when(userRepository.findByUsername(eq(user.getUsername()))).thenReturn(Optional.of(user));
    when(articleQueryService.findUserFeedWithCursor(eq(user), any())).thenReturn(pagerOf());

    DataFetcherResult<ArticlesConnection> result =
        articleDatafetcher.userFeed(null, null, 5, null, dgsDataFetchingEnvironment);

    assertTrue(result.getData().getEdges().isEmpty());
  }

  @Test
  public void should_throw_not_found_when_profile_user_does_not_exist() {
    when(dgsDataFetchingEnvironment.<Profile>getSource())
        .thenReturn(Profile.newBuilder().username("ghost").build());
    when(userRepository.findByUsername(eq("ghost"))).thenReturn(Optional.empty());

    assertThrows(
        ResourceNotFoundException.class,
        () -> articleDatafetcher.userFeed(10, null, null, null, dgsDataFetchingEnvironment));
  }

  @Test
  public void should_reject_user_feed_without_first_and_last() {
    assertThrows(
        IllegalArgumentException.class,
        () -> articleDatafetcher.userFeed(null, null, null, null, dgsDataFetchingEnvironment));
  }

  @Test
  public void should_get_user_favorites() {
    anonymous();
    when(dgsDataFetchingEnvironment.<Profile>getSource())
        .thenReturn(Profile.newBuilder().username(user.getUsername()).build());
    when(articleQueryService.findRecentArticlesWithCursor(
            isNull(), isNull(), eq(user.getUsername()), any(), isNull()))
        .thenReturn(pagerOf(articleData));

    DataFetcherResult<ArticlesConnection> result =
        articleDatafetcher.userFavorites(10, null, null, null, dgsDataFetchingEnvironment);

    assertEquals("slug", result.getData().getEdges().get(0).getNode().getSlug());
  }

  @Test
  public void should_get_user_favorites_backward() {
    anonymous();
    when(dgsDataFetchingEnvironment.<Profile>getSource())
        .thenReturn(Profile.newBuilder().username(user.getUsername()).build());
    when(articleQueryService.findRecentArticlesWithCursor(
            isNull(), isNull(), eq(user.getUsername()), any(), isNull()))
        .thenReturn(pagerOf());

    assertTrue(
        articleDatafetcher
            .userFavorites(null, null, 5, null, dgsDataFetchingEnvironment)
            .getData()
            .getEdges()
            .isEmpty());
  }

  @Test
  public void should_reject_user_favorites_without_first_and_last() {
    assertThrows(
        IllegalArgumentException.class,
        () -> articleDatafetcher.userFavorites(null, null, null, null, dgsDataFetchingEnvironment));
  }

  @Test
  public void should_get_user_articles() {
    anonymous();
    when(dgsDataFetchingEnvironment.<Profile>getSource())
        .thenReturn(Profile.newBuilder().username(user.getUsername()).build());
    when(articleQueryService.findRecentArticlesWithCursor(
            isNull(), eq(user.getUsername()), isNull(), any(), isNull()))
        .thenReturn(pagerOf(articleData));

    DataFetcherResult<ArticlesConnection> result =
        articleDatafetcher.userArticles(10, null, null, null, dgsDataFetchingEnvironment);

    assertEquals(1, result.getData().getEdges().size());
  }

  @Test
  public void should_get_user_articles_backward() {
    anonymous();
    when(dgsDataFetchingEnvironment.<Profile>getSource())
        .thenReturn(Profile.newBuilder().username(user.getUsername()).build());
    when(articleQueryService.findRecentArticlesWithCursor(
            isNull(), eq(user.getUsername()), isNull(), any(), isNull()))
        .thenReturn(pagerOf());

    assertTrue(
        articleDatafetcher
            .userArticles(null, null, 5, null, dgsDataFetchingEnvironment)
            .getData()
            .getEdges()
            .isEmpty());
  }

  @Test
  public void should_reject_user_articles_without_first_and_last() {
    assertThrows(
        IllegalArgumentException.class,
        () -> articleDatafetcher.userArticles(null, null, null, null, dgsDataFetchingEnvironment));
  }

  @Test
  public void should_get_articles_with_filters() {
    anonymous();
    when(articleQueryService.findRecentArticlesWithCursor(
            eq("java"), eq("johnjacob"), eq("someone"), any(), isNull()))
        .thenReturn(pagerOf(articleData));

    DataFetcherResult<ArticlesConnection> result =
        articleDatafetcher.getArticles(
            10, null, null, null, "johnjacob", "someone", "java", dgsDataFetchingEnvironment);

    assertEquals(1, result.getData().getEdges().size());
    assertFalse(result.getData().getPageInfo().isHasPreviousPage());
  }

  @Test
  public void should_get_articles_backward() {
    anonymous();
    when(articleQueryService.findRecentArticlesWithCursor(
            isNull(), isNull(), isNull(), any(), isNull()))
        .thenReturn(pagerOf());

    assertTrue(
        articleDatafetcher
            .getArticles(null, null, 5, null, null, null, null, dgsDataFetchingEnvironment)
            .getData()
            .getEdges()
            .isEmpty());
  }

  @Test
  public void should_reject_articles_without_first_and_last() {
    assertThrows(
        IllegalArgumentException.class,
        () ->
            articleDatafetcher.getArticles(
                null, null, null, null, null, null, null, dgsDataFetchingEnvironment));
  }

  @Test
  public void should_get_article_from_payload_local_context() {
    anonymous();
    io.spring.core.article.Article article =
        new io.spring.core.article.Article(
            "title", "desc", "body", Arrays.asList("java"), user.getId());
    when(dataFetchingEnvironment.<io.spring.core.article.Article>getLocalContext())
        .thenReturn(article);
    when(articleQueryService.findById(eq(article.getId()), isNull()))
        .thenReturn(Optional.of(articleData));

    DataFetcherResult<io.spring.graphql.types.Article> result =
        articleDatafetcher.getArticle(dataFetchingEnvironment);

    assertEquals("slug", result.getData().getSlug());
    Map<String, ArticleData> localContext = (Map<String, ArticleData>) result.getLocalContext();
    assertEquals(articleData, localContext.get("slug"));
  }

  @Test
  public void should_throw_not_found_when_payload_article_is_missing() {
    anonymous();
    io.spring.core.article.Article article =
        new io.spring.core.article.Article(
            "title", "desc", "body", Arrays.asList("java"), user.getId());
    when(dataFetchingEnvironment.<io.spring.core.article.Article>getLocalContext())
        .thenReturn(article);
    when(articleQueryService.findById(eq(article.getId()), isNull())).thenReturn(Optional.empty());

    assertThrows(
        ResourceNotFoundException.class,
        () -> articleDatafetcher.getArticle(dataFetchingEnvironment));
  }

  @Test
  public void should_get_article_of_comment() {
    anonymous();
    CommentData commentData =
        new CommentData(
            "comment-id",
            "body",
            "article-id",
            new DateTime(),
            new DateTime(),
            new ProfileData(user.getId(), user.getUsername(), "bio", "image", false));
    when(dataFetchingEnvironment.<CommentData>getLocalContext()).thenReturn(commentData);
    when(articleQueryService.findById(eq("article-id"), isNull()))
        .thenReturn(Optional.of(articleData));

    assertEquals(
        "slug", articleDatafetcher.getCommentArticle(dataFetchingEnvironment).getData().getSlug());
  }

  @Test
  public void should_throw_not_found_when_comment_article_is_missing() {
    anonymous();
    CommentData commentData =
        new CommentData(
            "comment-id",
            "body",
            "missing",
            new DateTime(),
            new DateTime(),
            new ProfileData(user.getId(), user.getUsername(), "bio", "image", false));
    when(dataFetchingEnvironment.<CommentData>getLocalContext()).thenReturn(commentData);
    when(articleQueryService.findById(eq("missing"), isNull())).thenReturn(Optional.empty());

    assertThrows(
        ResourceNotFoundException.class,
        () -> articleDatafetcher.getCommentArticle(dataFetchingEnvironment));
  }

  @Test
  public void should_find_article_by_slug() {
    authenticate(user);
    when(articleQueryService.findBySlug(eq("slug"), eq(user))).thenReturn(Optional.of(articleData));

    DataFetcherResult<io.spring.graphql.types.Article> result =
        articleDatafetcher.findArticleBySlug("slug");

    assertEquals("title", result.getData().getTitle());
    assertEquals(Arrays.asList("java"), result.getData().getTagList());
  }

  @Test
  public void should_throw_not_found_for_unknown_slug() {
    anonymous();
    when(articleQueryService.findBySlug(eq("ghost"), isNull())).thenReturn(Optional.empty());

    assertThrows(
        ResourceNotFoundException.class, () -> articleDatafetcher.findArticleBySlug("ghost"));
  }

  @Test
  public void should_build_page_info_without_cursor_for_empty_page() {
    anonymous();
    List<ArticleData> empty = Collections.emptyList();
    when(articleQueryService.findUserFeedWithCursor(isNull(), any()))
        .thenReturn(new CursorPager<>(empty, Direction.NEXT, false));

    DataFetcherResult<ArticlesConnection> result =
        articleDatafetcher.getFeed(10, null, null, null, dgsDataFetchingEnvironment);

    assertFalse(result.getData().getPageInfo().isHasNextPage());
  }
}
