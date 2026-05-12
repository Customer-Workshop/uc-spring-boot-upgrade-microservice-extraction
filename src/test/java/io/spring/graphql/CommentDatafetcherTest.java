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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.joda.time.DateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class CommentDatafetcherTest {

  @Mock private CommentQueryService commentQueryService;
  @Mock private DgsDataFetchingEnvironment dfe;

  @InjectMocks private CommentDatafetcher commentDatafetcher;

  private User user;
  private CommentData commentData;

  @BeforeEach
  void setUp() {
    user = new User("test@test.com", "testuser", "pass", "bio", "image");
    ProfileData profileData = new ProfileData(user.getId(), "testuser", "bio", "image", false);
    commentData =
        new CommentData(
            "comment-id", "body", "article-id", new DateTime(), new DateTime(), profileData);
  }

  @Test
  void should_get_comment_from_payload() {
    when(dfe.getLocalContext()).thenReturn(commentData);

    DataFetcherResult<Comment> result = commentDatafetcher.getComment(dfe);

    assertNotNull(result);
    assertNotNull(result.getData());
    assertEquals("comment-id", result.getData().getId());
    assertEquals("body", result.getData().getBody());
  }

  @Test
  void should_get_article_comments_with_first() {
    try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
      securityUtil.when(SecurityUtil::getCurrentUser).thenReturn(Optional.of(user));

      Article article = Article.newBuilder().slug("test-slug").build();
      when(dfe.getSource()).thenReturn(article);

      ProfileData profileData = new ProfileData(user.getId(), "testuser", "bio", "image", false);
      ArticleData articleData =
          new ArticleData(
              "article-id",
              "test-slug",
              "title",
              "desc",
              "body",
              false,
              0,
              new DateTime(),
              new DateTime(),
              null,
              profileData);
      Map<String, ArticleData> map = new HashMap<>();
      map.put("test-slug", articleData);
      when(dfe.getLocalContext()).thenReturn(map);

      CursorPager<CommentData> pager =
          new CursorPager<>(Arrays.asList(commentData), Direction.NEXT, false);
      when(commentQueryService.findByArticleIdWithCursor(eq("article-id"), eq(user), any()))
          .thenReturn(pager);

      DataFetcherResult<CommentsConnection> result =
          commentDatafetcher.articleComments(10, null, null, null, dfe);

      assertNotNull(result);
      assertEquals(1, result.getData().getEdges().size());
    }
  }

  @Test
  void should_get_article_comments_with_last() {
    try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
      securityUtil.when(SecurityUtil::getCurrentUser).thenReturn(Optional.of(user));

      Article article = Article.newBuilder().slug("test-slug").build();
      when(dfe.getSource()).thenReturn(article);

      ProfileData profileData = new ProfileData(user.getId(), "testuser", "bio", "image", false);
      ArticleData articleData =
          new ArticleData(
              "article-id",
              "test-slug",
              "title",
              "desc",
              "body",
              false,
              0,
              new DateTime(),
              new DateTime(),
              null,
              profileData);
      Map<String, ArticleData> map = new HashMap<>();
      map.put("test-slug", articleData);
      when(dfe.getLocalContext()).thenReturn(map);

      CursorPager<CommentData> pager = new CursorPager<>(new ArrayList<>(), Direction.PREV, false);
      when(commentQueryService.findByArticleIdWithCursor(eq("article-id"), eq(user), any()))
          .thenReturn(pager);

      DataFetcherResult<CommentsConnection> result =
          commentDatafetcher.articleComments(null, null, 5, null, dfe);

      assertNotNull(result);
    }
  }

  @Test
  void should_throw_when_article_comments_has_no_first_or_last() {
    assertThrows(
        IllegalArgumentException.class,
        () -> commentDatafetcher.articleComments(null, null, null, null, dfe));
  }

  @Test
  void should_get_article_comments_with_cursor_pagination() {
    try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
      securityUtil.when(SecurityUtil::getCurrentUser).thenReturn(Optional.empty());

      Article article = Article.newBuilder().slug("test-slug").build();
      when(dfe.getSource()).thenReturn(article);

      ProfileData profileData = new ProfileData(user.getId(), "testuser", "bio", "image", false);
      ArticleData articleData =
          new ArticleData(
              "article-id",
              "test-slug",
              "title",
              "desc",
              "body",
              false,
              0,
              new DateTime(),
              new DateTime(),
              null,
              profileData);
      Map<String, ArticleData> map = new HashMap<>();
      map.put("test-slug", articleData);
      when(dfe.getLocalContext()).thenReturn(map);

      CursorPager<CommentData> pager =
          new CursorPager<>(Arrays.asList(commentData), Direction.NEXT, true);
      when(commentQueryService.findByArticleIdWithCursor(eq("article-id"), eq(null), any()))
          .thenReturn(pager);

      DataFetcherResult<CommentsConnection> result =
          commentDatafetcher.articleComments(10, "1672531200000", null, null, dfe);

      assertNotNull(result);
      assertTrue(result.getData().getPageInfo().isHasNextPage());
    }
  }
}
