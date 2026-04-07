package io.spring.graphql;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import com.netflix.graphql.dgs.DgsDataFetchingEnvironment;
import graphql.execution.DataFetcherResult;
import io.spring.application.CommentQueryService;
import io.spring.application.CursorPager;
import io.spring.application.CursorPager.Direction;
import io.spring.application.data.ArticleData;
import io.spring.application.data.CommentData;
import io.spring.application.data.ProfileData;
import io.spring.core.user.User;
import io.spring.graphql.types.Article;
import io.spring.graphql.types.Comment;
import io.spring.graphql.types.CommentsConnection;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.joda.time.DateTime;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
public class CommentDatafetcherTest {

  @Mock private CommentQueryService commentQueryService;
  @Mock private DgsDataFetchingEnvironment dfe;

  private CommentDatafetcher commentDatafetcher;
  private User user;
  private CommentData commentData;
  private ArticleData articleData;
  private DateTime now;

  @BeforeEach
  void setUp() {
    commentDatafetcher = new CommentDatafetcher(commentQueryService);
    user = new User("test@test.com", "testuser", "password", "bio", "image");
    now = new DateTime();
    ProfileData profileData = new ProfileData(user.getId(), "testuser", "bio", "image", false);
    commentData = new CommentData("comment-id", "comment body", "article-id", now, now, profileData);
    articleData =
        new ArticleData(
            "article-id",
            "test-title",
            "Test Title",
            "description",
            "body",
            false,
            0,
            now,
            now,
            Arrays.asList("java"),
            profileData);
  }

  @AfterEach
  void tearDown() {
    SecurityContextHolder.clearContext();
  }

  private void setAuthentication(User user) {
    TestingAuthenticationToken auth = new TestingAuthenticationToken(user, null);
    SecurityContextHolder.getContext().setAuthentication(auth);
  }

  @Test
  void should_get_comment_from_comment_payload() {
    when(dfe.getLocalContext()).thenReturn(commentData);

    DataFetcherResult<Comment> result = commentDatafetcher.getComment(dfe);

    assertNotNull(result);
    assertNotNull(result.getData());
    assertEquals("comment-id", result.getData().getId());
    assertEquals("comment body", result.getData().getBody());
    assertNotNull(result.getData().getCreatedAt());
    assertNotNull(result.getData().getUpdatedAt());
  }

  @Test
  void should_get_article_comments_with_first_parameter() {
    setAuthentication(user);
    Article article = Article.newBuilder().slug("test-title").build();
    when(dfe.getSource()).thenReturn(article);

    Map<String, ArticleData> map = new HashMap<>();
    map.put("test-title", articleData);
    when(dfe.getLocalContext()).thenReturn(map);

    CursorPager<CommentData> cursorPager =
        new CursorPager<>(Arrays.asList(commentData), Direction.NEXT, false);
    when(commentQueryService.findByArticleIdWithCursor(eq("article-id"), any(), any()))
        .thenReturn(cursorPager);

    DataFetcherResult<CommentsConnection> result =
        commentDatafetcher.articleComments(10, null, null, null, dfe);

    assertNotNull(result);
    assertNotNull(result.getData());
    assertEquals(1, result.getData().getEdges().size());
    assertNotNull(result.getData().getEdges().get(0).getCursor());
    assertNotNull(result.getData().getEdges().get(0).getNode());
  }

  @Test
  void should_get_article_comments_with_last_parameter() {
    setAuthentication(user);
    Article article = Article.newBuilder().slug("test-title").build();
    when(dfe.getSource()).thenReturn(article);

    Map<String, ArticleData> map = new HashMap<>();
    map.put("test-title", articleData);
    when(dfe.getLocalContext()).thenReturn(map);

    CursorPager<CommentData> cursorPager =
        new CursorPager<>(Arrays.asList(commentData), Direction.PREV, true);
    when(commentQueryService.findByArticleIdWithCursor(eq("article-id"), any(), any()))
        .thenReturn(cursorPager);

    DataFetcherResult<CommentsConnection> result =
        commentDatafetcher.articleComments(null, null, 5, null, dfe);

    assertNotNull(result);
    assertTrue(result.getData().getPageInfo().isHasPreviousPage());
  }

  @Test
  void should_throw_when_article_comments_missing_first_and_last() {
    assertThrows(
        IllegalArgumentException.class,
        () -> commentDatafetcher.articleComments(null, null, null, null, dfe));
  }

  @Test
  void should_get_article_comments_with_empty_result() {
    setAuthentication(user);
    Article article = Article.newBuilder().slug("test-title").build();
    when(dfe.getSource()).thenReturn(article);

    Map<String, ArticleData> map = new HashMap<>();
    map.put("test-title", articleData);
    when(dfe.getLocalContext()).thenReturn(map);

    CursorPager<CommentData> cursorPager =
        new CursorPager<>(Collections.emptyList(), Direction.NEXT, false);
    when(commentQueryService.findByArticleIdWithCursor(eq("article-id"), any(), any()))
        .thenReturn(cursorPager);

    DataFetcherResult<CommentsConnection> result =
        commentDatafetcher.articleComments(10, null, null, null, dfe);

    assertNotNull(result);
    assertTrue(result.getData().getEdges().isEmpty());
    assertNull(result.getData().getPageInfo().getStartCursor());
    assertNull(result.getData().getPageInfo().getEndCursor());
  }

  @Test
  void should_get_article_comments_with_has_next() {
    setAuthentication(user);
    Article article = Article.newBuilder().slug("test-title").build();
    when(dfe.getSource()).thenReturn(article);

    Map<String, ArticleData> map = new HashMap<>();
    map.put("test-title", articleData);
    when(dfe.getLocalContext()).thenReturn(map);

    CursorPager<CommentData> cursorPager =
        new CursorPager<>(Arrays.asList(commentData), Direction.NEXT, true);
    when(commentQueryService.findByArticleIdWithCursor(eq("article-id"), any(), any()))
        .thenReturn(cursorPager);

    DataFetcherResult<CommentsConnection> result =
        commentDatafetcher.articleComments(10, null, null, null, dfe);

    assertNotNull(result);
    assertTrue(result.getData().getPageInfo().isHasNextPage());
    assertFalse(result.getData().getPageInfo().isHasPreviousPage());
  }

  @Test
  void should_get_article_comments_with_cursor_params() {
    setAuthentication(user);
    Article article = Article.newBuilder().slug("test-title").build();
    when(dfe.getSource()).thenReturn(article);

    Map<String, ArticleData> map = new HashMap<>();
    map.put("test-title", articleData);
    when(dfe.getLocalContext()).thenReturn(map);

    String afterCursor = String.valueOf(now.getMillis());
    CursorPager<CommentData> cursorPager =
        new CursorPager<>(Arrays.asList(commentData), Direction.NEXT, false);
    when(commentQueryService.findByArticleIdWithCursor(eq("article-id"), any(), any()))
        .thenReturn(cursorPager);

    DataFetcherResult<CommentsConnection> result =
        commentDatafetcher.articleComments(10, afterCursor, null, null, dfe);

    assertNotNull(result);
    assertEquals(1, result.getData().getEdges().size());
  }
}
