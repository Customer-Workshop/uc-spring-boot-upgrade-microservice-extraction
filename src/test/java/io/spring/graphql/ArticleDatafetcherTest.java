package io.spring.graphql;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.netflix.graphql.dgs.DgsDataFetchingEnvironment;
import io.spring.api.exception.ResourceNotFoundException;
import io.spring.application.ArticleQueryService;
import io.spring.application.CursorPager;
import io.spring.application.data.ArticleData;
import io.spring.application.data.CommentData;
import io.spring.core.article.Article;
import io.spring.core.user.User;
import io.spring.core.user.UserRepository;
import io.spring.graphql.types.Profile;
import java.util.Collections;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

class ArticleDatafetcherTest extends GraphqlTestBase {
  private final User user = GraphqlTestFixtures.user();
  private final Article article = GraphqlTestFixtures.article(user);
  private final ArticleData data = GraphqlTestFixtures.articleData(article, user);

  @Test
  void getsFeedInBothDirections() {
    ArticleQueryService service = mock(ArticleQueryService.class);
    ArticleDatafetcher fetcher = new ArticleDatafetcher(service, mock(UserRepository.class));
    CursorPager<ArticleData> page =
        new CursorPager<>(Collections.singletonList(data), CursorPager.Direction.NEXT, true);
    when(service.findUserFeedWithCursor(any(), any())).thenReturn(page);
    try (MockedStatic<SecurityUtil> security = org.mockito.Mockito.mockStatic(SecurityUtil.class)) {
      security.when(SecurityUtil::getCurrentUser).thenReturn(Optional.of(user));
      assertEquals(1, fetcher.getFeed(1, null, null, null, mock(DgsDataFetchingEnvironment.class)).getData().getEdges().size());
      assertEquals(1, fetcher.getFeed(null, null, 1, null, mock(DgsDataFetchingEnvironment.class)).getData().getEdges().size());
    }
  }

  @Test
  void rejectsFeedWithoutPagination() {
    ArticleDatafetcher fetcher = new ArticleDatafetcher(mock(ArticleQueryService.class), mock(UserRepository.class));
    assertThrows(
        IllegalArgumentException.class,
        () -> fetcher.getFeed(null, null, null, null, mock(DgsDataFetchingEnvironment.class)));
  }

  @Test
  void getsUserArticlesFavoritesAndFeed() {
    ArticleQueryService service = mock(ArticleQueryService.class);
    UserRepository users = mock(UserRepository.class);
    ArticleDatafetcher fetcher = new ArticleDatafetcher(service, users);
    CursorPager<ArticleData> page =
        new CursorPager<>(Collections.singletonList(data), CursorPager.Direction.NEXT, false);
    when(service.findUserFeedWithCursor(any(), any())).thenReturn(page);
    when(service.findRecentArticlesWithCursor(any(), any(), any(), any(), any())).thenReturn(page);
    when(users.findByUsername(user.getUsername())).thenReturn(Optional.of(user));
    Profile profile = Profile.newBuilder().username(user.getUsername()).build();
    DgsDataFetchingEnvironment dfe =
        GraphqlTestFixtures.dgsEnvironment(profile, Collections.emptyMap());
    try (MockedStatic<SecurityUtil> security = org.mockito.Mockito.mockStatic(SecurityUtil.class)) {
      security.when(SecurityUtil::getCurrentUser).thenReturn(Optional.of(user));
      assertEquals(1, fetcher.userFeed(1, null, null, null, dfe).getData().getEdges().size());
      assertEquals(1, fetcher.userFavorites(1, null, null, null, dfe).getData().getEdges().size());
      assertEquals(1, fetcher.userArticles(1, null, null, null, dfe).getData().getEdges().size());
      assertEquals(1, fetcher.getArticles(1, null, null, null, null, null, null, dfe).getData().getEdges().size());
    }
  }

  @Test
  void rejectsMissingUserProfile() {
    UserRepository users = mock(UserRepository.class);
    ArticleDatafetcher fetcher = new ArticleDatafetcher(mock(ArticleQueryService.class), users);
    Profile profile = Profile.newBuilder().username("missing").build();
    when(users.findByUsername("missing")).thenReturn(Optional.empty());
    assertThrows(
        ResourceNotFoundException.class,
        () ->
            fetcher.userFeed(
                1,
                null,
                null,
                null,
                GraphqlTestFixtures.dgsEnvironment(profile, Collections.emptyMap())));
  }

  @Test
  void resolvesArticleAndCommentArticleOrNotFound() {
    ArticleQueryService service = mock(ArticleQueryService.class);
    ArticleDatafetcher fetcher = new ArticleDatafetcher(service, mock(UserRepository.class));
    when(service.findById(article.getId(), null)).thenReturn(Optional.of(data));
    when(service.findBySlug(article.getSlug(), null)).thenReturn(Optional.of(data));
    CommentData comment = GraphqlTestFixtures.commentData(GraphqlTestFixtures.comment(article, user), article, user);
    try (MockedStatic<SecurityUtil> security = org.mockito.Mockito.mockStatic(SecurityUtil.class)) {
      security.when(SecurityUtil::getCurrentUser).thenReturn(Optional.empty());
      assertEquals(article.getSlug(), fetcher.getArticle(GraphqlTestFixtures.environment(article)).getData().getSlug());
      assertEquals(article.getSlug(), fetcher.findArticleBySlug(article.getSlug()).getData().getSlug());
      assertEquals(article.getSlug(), fetcher.getCommentArticle(GraphqlTestFixtures.environment(comment)).getData().getSlug());
      when(service.findById(article.getId(), null)).thenReturn(Optional.empty());
      assertThrows(ResourceNotFoundException.class, () -> fetcher.getArticle(GraphqlTestFixtures.environment(article)));
      assertThrows(ResourceNotFoundException.class, () -> fetcher.getCommentArticle(GraphqlTestFixtures.environment(comment)));
      assertThrows(ResourceNotFoundException.class, () -> fetcher.findArticleBySlug("missing"));
    }
  }
}
