package io.spring.graphql;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
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
public class CommentDatafetcherTest {

  @Mock private CommentQueryService commentQueryService;
  @Mock private DgsDataFetchingEnvironment dfe;

  private CommentDatafetcher commentDatafetcher;
  private User user;
  private CommentData commentData;
  private ProfileData profileData;
  private ArticleData articleData;

  @BeforeEach
  void setUp() {
    commentDatafetcher = new CommentDatafetcher(commentQueryService);
    user = new User("test@test.com", "testuser", "password", "bio", "image");
    profileData =
        new ProfileData(user.getId(), user.getUsername(), user.getBio(), user.getImage(), false);
    DateTime now = new DateTime();
    commentData = new CommentData("comment-id", "comment body", "article-id", now, now, profileData);
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
  void should_get_comment_from_payload() {
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
  void should_get_article_comments_with_first() {
    Article article = Article.newBuilder().slug("test-slug").build();
    Map<String, ArticleData> map = new HashMap<>();
    map.put("test-slug", articleData);

    when(dfe.getSource()).thenReturn(article);
    when(dfe.getLocalContext()).thenReturn(map);

    CursorPager<CommentData> cursorPager =
        new CursorPager<>(Arrays.asList(commentData), Direction.NEXT, false);

    try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
      securityUtil.when(SecurityUtil::getCurrentUser).thenReturn(Optional.of(user));
      when(commentQueryService.findByArticleIdWithCursor(eq("article-id"), eq(user), any()))
          .thenReturn(cursorPager);

      DataFetcherResult<CommentsConnection> result =
          commentDatafetcher.articleComments(10, null, null, null, dfe);

      assertNotNull(result);
      assertEquals(1, result.getData().getEdges().size());
      assertEquals("comment-id", result.getData().getEdges().get(0).getNode().getId());
      assertEquals("comment body", result.getData().getEdges().get(0).getNode().getBody());
    }
  }

  @Test
  void should_get_article_comments_with_last() {
    Article article = Article.newBuilder().slug("test-slug").build();
    Map<String, ArticleData> map = new HashMap<>();
    map.put("test-slug", articleData);

    when(dfe.getSource()).thenReturn(article);
    when(dfe.getLocalContext()).thenReturn(map);

    CursorPager<CommentData> cursorPager =
        new CursorPager<>(Arrays.asList(commentData), Direction.PREV, false);

    try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
      securityUtil.when(SecurityUtil::getCurrentUser).thenReturn(Optional.of(user));
      when(commentQueryService.findByArticleIdWithCursor(eq("article-id"), eq(user), any()))
          .thenReturn(cursorPager);

      DataFetcherResult<CommentsConnection> result =
          commentDatafetcher.articleComments(null, null, 5, null, dfe);

      assertNotNull(result);
      assertEquals(1, result.getData().getEdges().size());
    }
  }

  @Test
  void should_throw_when_article_comments_has_no_first_or_last() {
    assertThrows(
        IllegalArgumentException.class,
        () -> commentDatafetcher.articleComments(null, null, null, null, dfe));
  }

  @Test
  void should_get_article_comments_with_empty_result() {
    Article article = Article.newBuilder().slug("test-slug").build();
    Map<String, ArticleData> map = new HashMap<>();
    map.put("test-slug", articleData);

    when(dfe.getSource()).thenReturn(article);
    when(dfe.getLocalContext()).thenReturn(map);

    CursorPager<CommentData> cursorPager =
        new CursorPager<>(new ArrayList<>(), Direction.NEXT, false);

    try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
      securityUtil.when(SecurityUtil::getCurrentUser).thenReturn(Optional.empty());
      when(commentQueryService.findByArticleIdWithCursor(eq("article-id"), isNull(), any()))
          .thenReturn(cursorPager);

      DataFetcherResult<CommentsConnection> result =
          commentDatafetcher.articleComments(10, null, null, null, dfe);

      assertNotNull(result);
      assertEquals(0, result.getData().getEdges().size());
      assertNull(result.getData().getPageInfo().getStartCursor());
      assertNull(result.getData().getPageInfo().getEndCursor());
    }
  }

  @Test
  void should_get_article_comments_with_pagination_info() {
    DateTime now = new DateTime();
    CommentData commentData2 =
        new CommentData("comment-id-2", "second body", "article-id", now, now, profileData);

    Article article = Article.newBuilder().slug("test-slug").build();
    Map<String, ArticleData> map = new HashMap<>();
    map.put("test-slug", articleData);

    when(dfe.getSource()).thenReturn(article);
    when(dfe.getLocalContext()).thenReturn(map);

    CursorPager<CommentData> cursorPager =
        new CursorPager<>(Arrays.asList(commentData, commentData2), Direction.NEXT, true);

    try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
      securityUtil.when(SecurityUtil::getCurrentUser).thenReturn(Optional.of(user));
      when(commentQueryService.findByArticleIdWithCursor(eq("article-id"), eq(user), any()))
          .thenReturn(cursorPager);

      DataFetcherResult<CommentsConnection> result =
          commentDatafetcher.articleComments(10, null, null, null, dfe);

      assertNotNull(result);
      assertEquals(2, result.getData().getEdges().size());
      assertNotNull(result.getData().getPageInfo().getStartCursor());
      assertNotNull(result.getData().getPageInfo().getEndCursor());
      assertTrue(result.getData().getPageInfo().isHasNextPage());
      assertFalse(result.getData().getPageInfo().isHasPreviousPage());
    }
  }
}
