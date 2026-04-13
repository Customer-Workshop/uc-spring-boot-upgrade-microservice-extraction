package io.spring.graphql;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.netflix.graphql.dgs.DgsDataFetchingEnvironment;
import graphql.execution.DataFetcherResult;
import graphql.schema.DataFetchingEnvironment;
import io.spring.api.exception.ResourceNotFoundException;
import io.spring.application.ArticleQueryService;
import io.spring.application.CursorPager;
import io.spring.application.CursorPager.Direction;
import io.spring.application.DateTimeCursor;
import io.spring.application.data.ArticleData;
import io.spring.application.data.CommentData;
import io.spring.application.data.ProfileData;
import io.spring.core.user.User;
import io.spring.core.user.UserRepository;
import io.spring.graphql.types.Article;
import io.spring.graphql.types.ArticlesConnection;
import io.spring.graphql.types.Profile;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.joda.time.DateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class ArticleDatafetcherTest {

  @Mock private ArticleQueryService articleQueryService;
  @Mock private UserRepository userRepository;
  @Mock private DgsDataFetchingEnvironment dfe;
  @Mock private DataFetchingEnvironment dataFetchingEnvironment;

  private ArticleDatafetcher articleDatafetcher;
  private User user;
  private ArticleData articleData;
  private ProfileData profileData;

  @BeforeEach
  void setUp() {
    articleDatafetcher = new ArticleDatafetcher(articleQueryService, userRepository);
    user = new User("test@test.com", "testuser", "password", "bio", "image");
    profileData =
        new ProfileData(user.getId(), user.getUsername(), user.getBio(), user.getImage(), false);
    DateTime now = new DateTime();
    articleData =
        new ArticleData(
            "article-id",
            "test-slug",
            "Test Title",
            "Test Description",
            "Test Body",
            false,
            0,
            now,
            now,
            Arrays.asList("java"),
            profileData);
  }

  @Test
  void should_get_feed_with_first_parameter() {
    CursorPager<ArticleData> cursorPager =
        new CursorPager<>(Arrays.asList(articleData), Direction.NEXT, false);

    try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
      securityUtil.when(SecurityUtil::getCurrentUser).thenReturn(Optional.of(user));

      when(articleQueryService.findUserFeedWithCursor(eq(user), any())).thenReturn(cursorPager);

      DataFetcherResult<ArticlesConnection> result =
          articleDatafetcher.getFeed(10, null, null, null, dfe);

      assertNotNull(result);
      assertNotNull(result.getData());
      assertEquals(1, result.getData().getEdges().size());
      assertEquals("Test Body", result.getData().getEdges().get(0).getNode().getBody());
    }
  }

  @Test
  void should_get_feed_with_last_parameter() {
    CursorPager<ArticleData> cursorPager =
        new CursorPager<>(Arrays.asList(articleData), Direction.PREV, false);

    try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
      securityUtil.when(SecurityUtil::getCurrentUser).thenReturn(Optional.of(user));

      when(articleQueryService.findUserFeedWithCursor(eq(user), any())).thenReturn(cursorPager);

      DataFetcherResult<ArticlesConnection> result =
          articleDatafetcher.getFeed(null, null, 10, null, dfe);

      assertNotNull(result);
      assertEquals(1, result.getData().getEdges().size());
    }
  }

  @Test
  void should_throw_when_feed_has_no_first_or_last() {
    try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
      securityUtil.when(SecurityUtil::getCurrentUser).thenReturn(Optional.of(user));

      assertThrows(
          IllegalArgumentException.class,
          () -> articleDatafetcher.getFeed(null, null, null, null, dfe));
    }
  }

  @Test
  void should_get_user_feed_with_first_parameter() {
    Profile profile = Profile.newBuilder().username("testuser").build();
    when(dfe.getSource()).thenReturn(profile);
    when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));

    CursorPager<ArticleData> cursorPager =
        new CursorPager<>(Arrays.asList(articleData), Direction.NEXT, false);
    when(articleQueryService.findUserFeedWithCursor(eq(user), any())).thenReturn(cursorPager);

    DataFetcherResult<ArticlesConnection> result =
        articleDatafetcher.userFeed(10, null, null, null, dfe);

    assertNotNull(result);
    assertEquals(1, result.getData().getEdges().size());
  }

  @Test
  void should_get_user_feed_with_last_parameter() {
    Profile profile = Profile.newBuilder().username("testuser").build();
    when(dfe.getSource()).thenReturn(profile);
    when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));

    CursorPager<ArticleData> cursorPager =
        new CursorPager<>(Arrays.asList(articleData), Direction.PREV, false);
    when(articleQueryService.findUserFeedWithCursor(eq(user), any())).thenReturn(cursorPager);

    DataFetcherResult<ArticlesConnection> result =
        articleDatafetcher.userFeed(null, null, 5, null, dfe);

    assertNotNull(result);
    assertEquals(1, result.getData().getEdges().size());
  }

  @Test
  void should_throw_when_user_feed_has_no_first_or_last() {
    assertThrows(
        IllegalArgumentException.class,
        () -> articleDatafetcher.userFeed(null, null, null, null, dfe));
  }

  @Test
  void should_throw_when_user_feed_user_not_found() {
    Profile profile = Profile.newBuilder().username("nonexistent").build();
    when(dfe.getSource()).thenReturn(profile);
    when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

    assertThrows(
        ResourceNotFoundException.class,
        () -> articleDatafetcher.userFeed(10, null, null, null, dfe));
  }

  @Test
  void should_get_user_favorites_with_first() {
    Profile profile = Profile.newBuilder().username("testuser").build();
    when(dfe.getSource()).thenReturn(profile);

    CursorPager<ArticleData> cursorPager =
        new CursorPager<>(Arrays.asList(articleData), Direction.NEXT, false);

    try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
      securityUtil.when(SecurityUtil::getCurrentUser).thenReturn(Optional.of(user));
      when(articleQueryService.findRecentArticlesWithCursor(
              isNull(), isNull(), eq("testuser"), any(), eq(user)))
          .thenReturn(cursorPager);

      DataFetcherResult<ArticlesConnection> result =
          articleDatafetcher.userFavorites(10, null, null, null, dfe);

      assertNotNull(result);
      assertEquals(1, result.getData().getEdges().size());
    }
  }

  @Test
  void should_get_user_favorites_with_last() {
    Profile profile = Profile.newBuilder().username("testuser").build();
    when(dfe.getSource()).thenReturn(profile);

    CursorPager<ArticleData> cursorPager =
        new CursorPager<>(Arrays.asList(articleData), Direction.PREV, false);

    try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
      securityUtil.when(SecurityUtil::getCurrentUser).thenReturn(Optional.of(user));
      when(articleQueryService.findRecentArticlesWithCursor(
              isNull(), isNull(), eq("testuser"), any(), eq(user)))
          .thenReturn(cursorPager);

      DataFetcherResult<ArticlesConnection> result =
          articleDatafetcher.userFavorites(null, null, 5, null, dfe);

      assertNotNull(result);
      assertEquals(1, result.getData().getEdges().size());
    }
  }

  @Test
  void should_throw_when_user_favorites_has_no_first_or_last() {
    try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
      securityUtil.when(SecurityUtil::getCurrentUser).thenReturn(Optional.of(user));

      assertThrows(
          IllegalArgumentException.class,
          () -> articleDatafetcher.userFavorites(null, null, null, null, dfe));
    }
  }

  @Test
  void should_get_user_articles_with_first() {
    Profile profile = Profile.newBuilder().username("testuser").build();
    when(dfe.getSource()).thenReturn(profile);

    CursorPager<ArticleData> cursorPager =
        new CursorPager<>(Arrays.asList(articleData), Direction.NEXT, false);

    try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
      securityUtil.when(SecurityUtil::getCurrentUser).thenReturn(Optional.of(user));
      when(articleQueryService.findRecentArticlesWithCursor(
              isNull(), eq("testuser"), isNull(), any(), eq(user)))
          .thenReturn(cursorPager);

      DataFetcherResult<ArticlesConnection> result =
          articleDatafetcher.userArticles(10, null, null, null, dfe);

      assertNotNull(result);
      assertEquals(1, result.getData().getEdges().size());
    }
  }

  @Test
  void should_get_user_articles_with_last() {
    Profile profile = Profile.newBuilder().username("testuser").build();
    when(dfe.getSource()).thenReturn(profile);

    CursorPager<ArticleData> cursorPager =
        new CursorPager<>(Arrays.asList(articleData), Direction.PREV, false);

    try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
      securityUtil.when(SecurityUtil::getCurrentUser).thenReturn(Optional.of(user));
      when(articleQueryService.findRecentArticlesWithCursor(
              isNull(), eq("testuser"), isNull(), any(), eq(user)))
          .thenReturn(cursorPager);

      DataFetcherResult<ArticlesConnection> result =
          articleDatafetcher.userArticles(null, null, 5, null, dfe);

      assertNotNull(result);
      assertEquals(1, result.getData().getEdges().size());
    }
  }

  @Test
  void should_throw_when_user_articles_has_no_first_or_last() {
    try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
      securityUtil.when(SecurityUtil::getCurrentUser).thenReturn(Optional.of(user));

      assertThrows(
          IllegalArgumentException.class,
          () -> articleDatafetcher.userArticles(null, null, null, null, dfe));
    }
  }

  @Test
  void should_get_articles_with_first() {
    CursorPager<ArticleData> cursorPager =
        new CursorPager<>(Arrays.asList(articleData), Direction.NEXT, true);

    try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
      securityUtil.when(SecurityUtil::getCurrentUser).thenReturn(Optional.of(user));
      when(articleQueryService.findRecentArticlesWithCursor(
              eq("java"), eq("testuser"), eq("favUser"), any(), eq(user)))
          .thenReturn(cursorPager);

      DataFetcherResult<ArticlesConnection> result =
          articleDatafetcher.getArticles(10, null, null, null, "testuser", "favUser", "java", dfe);

      assertNotNull(result);
      assertEquals(1, result.getData().getEdges().size());
      assertTrue(result.getData().getPageInfo().isHasNextPage());
    }
  }

  @Test
  void should_get_articles_with_last() {
    CursorPager<ArticleData> cursorPager =
        new CursorPager<>(Arrays.asList(articleData), Direction.PREV, false);

    try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
      securityUtil.when(SecurityUtil::getCurrentUser).thenReturn(Optional.of(user));
      when(articleQueryService.findRecentArticlesWithCursor(
              isNull(), isNull(), isNull(), any(), eq(user)))
          .thenReturn(cursorPager);

      DataFetcherResult<ArticlesConnection> result =
          articleDatafetcher.getArticles(null, null, 5, null, null, null, null, dfe);

      assertNotNull(result);
      assertEquals(1, result.getData().getEdges().size());
    }
  }

  @Test
  void should_throw_when_articles_has_no_first_or_last() {
    try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
      securityUtil.when(SecurityUtil::getCurrentUser).thenReturn(Optional.of(user));

      assertThrows(
          IllegalArgumentException.class,
          () ->
              articleDatafetcher.getArticles(
                  null, null, null, null, null, null, null, dfe));
    }
  }

  @Test
  void should_get_articles_with_empty_result() {
    CursorPager<ArticleData> cursorPager =
        new CursorPager<>(new ArrayList<>(), Direction.NEXT, false);

    try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
      securityUtil.when(SecurityUtil::getCurrentUser).thenReturn(Optional.empty());
      when(articleQueryService.findRecentArticlesWithCursor(
              isNull(), isNull(), isNull(), any(), isNull()))
          .thenReturn(cursorPager);

      DataFetcherResult<ArticlesConnection> result =
          articleDatafetcher.getArticles(10, null, null, null, null, null, null, dfe);

      assertNotNull(result);
      assertEquals(0, result.getData().getEdges().size());
      assertNull(result.getData().getPageInfo().getStartCursor());
      assertNull(result.getData().getPageInfo().getEndCursor());
    }
  }

  @Test
  void should_get_article_from_article_payload() {
    io.spring.core.article.Article coreArticle =
        new io.spring.core.article.Article(
            "Test Title", "Desc", "Body", Arrays.asList("java"), user.getId());

    when(dataFetchingEnvironment.getLocalContext()).thenReturn(coreArticle);

    try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
      securityUtil.when(SecurityUtil::getCurrentUser).thenReturn(Optional.of(user));
      when(articleQueryService.findById(eq(coreArticle.getId()), eq(user)))
          .thenReturn(Optional.of(articleData));

      DataFetcherResult<Article> result = articleDatafetcher.getArticle(dataFetchingEnvironment);

      assertNotNull(result);
      assertEquals("Test Body", result.getData().getBody());
      assertEquals("test-slug", result.getData().getSlug());
    }
  }

  @Test
  void should_throw_when_article_not_found_in_payload() {
    io.spring.core.article.Article coreArticle =
        new io.spring.core.article.Article(
            "Test Title", "Desc", "Body", Arrays.asList("java"), user.getId());

    when(dataFetchingEnvironment.getLocalContext()).thenReturn(coreArticle);

    try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
      securityUtil.when(SecurityUtil::getCurrentUser).thenReturn(Optional.of(user));
      when(articleQueryService.findById(eq(coreArticle.getId()), eq(user)))
          .thenReturn(Optional.empty());

      assertThrows(
          ResourceNotFoundException.class,
          () -> articleDatafetcher.getArticle(dataFetchingEnvironment));
    }
  }

  @Test
  void should_get_comment_article() {
    DateTime now = new DateTime();
    CommentData commentData =
        new CommentData("comment-id", "comment body", "article-id", now, now, profileData);

    when(dataFetchingEnvironment.getLocalContext()).thenReturn(commentData);

    try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
      securityUtil.when(SecurityUtil::getCurrentUser).thenReturn(Optional.of(user));
      when(articleQueryService.findById(eq("article-id"), eq(user)))
          .thenReturn(Optional.of(articleData));

      DataFetcherResult<Article> result =
          articleDatafetcher.getCommentArticle(dataFetchingEnvironment);

      assertNotNull(result);
      assertEquals("Test Body", result.getData().getBody());
    }
  }

  @Test
  void should_throw_when_comment_article_not_found() {
    DateTime now = new DateTime();
    CommentData commentData =
        new CommentData("comment-id", "comment body", "article-id", now, now, profileData);

    when(dataFetchingEnvironment.getLocalContext()).thenReturn(commentData);

    try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
      securityUtil.when(SecurityUtil::getCurrentUser).thenReturn(Optional.of(user));
      when(articleQueryService.findById(eq("article-id"), eq(user)))
          .thenReturn(Optional.empty());

      assertThrows(
          ResourceNotFoundException.class,
          () -> articleDatafetcher.getCommentArticle(dataFetchingEnvironment));
    }
  }

  @Test
  void should_find_article_by_slug() {
    try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
      securityUtil.when(SecurityUtil::getCurrentUser).thenReturn(Optional.of(user));
      when(articleQueryService.findBySlug(eq("test-slug"), eq(user)))
          .thenReturn(Optional.of(articleData));

      DataFetcherResult<Article> result = articleDatafetcher.findArticleBySlug("test-slug");

      assertNotNull(result);
      assertEquals("test-slug", result.getData().getSlug());
      assertEquals("Test Title", result.getData().getTitle());
      assertEquals("Test Description", result.getData().getDescription());
      assertTrue(result.getData().getFavoritesCount() == 0);
      assertFalse(result.getData().getFavorited());
    }
  }

  @Test
  void should_throw_when_slug_not_found() {
    try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
      securityUtil.when(SecurityUtil::getCurrentUser).thenReturn(Optional.empty());
      when(articleQueryService.findBySlug(eq("nonexistent"), isNull()))
          .thenReturn(Optional.empty());

      assertThrows(
          ResourceNotFoundException.class,
          () -> articleDatafetcher.findArticleBySlug("nonexistent"));
    }
  }

  @Test
  void should_get_feed_with_pagination_cursors() {
    DateTime now = new DateTime();
    ArticleData articleData2 =
        new ArticleData(
            "article-id-2",
            "test-slug-2",
            "Title 2",
            "Desc 2",
            "Body 2",
            true,
            5,
            now,
            now,
            Arrays.asList("spring"),
            profileData);

    CursorPager<ArticleData> cursorPager =
        new CursorPager<>(Arrays.asList(articleData, articleData2), Direction.NEXT, true);

    try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
      securityUtil.when(SecurityUtil::getCurrentUser).thenReturn(Optional.of(user));
      when(articleQueryService.findUserFeedWithCursor(eq(user), any())).thenReturn(cursorPager);

      DataFetcherResult<ArticlesConnection> result =
          articleDatafetcher.getFeed(10, null, null, null, dfe);

      assertNotNull(result);
      assertEquals(2, result.getData().getEdges().size());
      assertNotNull(result.getData().getPageInfo().getStartCursor());
      assertNotNull(result.getData().getPageInfo().getEndCursor());
      assertTrue(result.getData().getPageInfo().isHasNextPage());
      assertFalse(result.getData().getPageInfo().isHasPreviousPage());
    }
  }
}
