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
import org.joda.time.DateTime;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

public class CommentDatafetcherTest {

  @Mock private CommentQueryService commentQueryService;
  @Mock private DgsDataFetchingEnvironment dfe;

  private CommentDatafetcher commentDatafetcher;
  private User user;
  private AutoCloseable closeable;

  @BeforeEach
  void setUp() {
    closeable = MockitoAnnotations.openMocks(this);
    commentDatafetcher = new CommentDatafetcher(commentQueryService);
    user = new User("test@test.com", "testuser", "password", "bio", "image");
    SecurityContextHolder.getContext()
        .setAuthentication(new TestingAuthenticationToken(user, null));
  }

  @AfterEach
  void tearDown() throws Exception {
    SecurityContextHolder.clearContext();
    closeable.close();
  }

  private CommentData createCommentData(String id) {
    DateTime now = new DateTime();
    return new CommentData(
        id,
        "comment body " + id,
        "article-id",
        now,
        now,
        new ProfileData("userId", "author", "bio", "image", false));
  }

  @Test
  void should_get_comment_from_payload() {
    CommentData commentData = createCommentData("comment-1");
    when(dfe.getLocalContext()).thenReturn(commentData);

    DataFetcherResult<Comment> result = commentDatafetcher.getComment(dfe);

    assertNotNull(result);
    assertNotNull(result.getData());
    assertEquals("comment-1", result.getData().getId());
    assertEquals("comment body comment-1", result.getData().getBody());
  }

  @Test
  void should_get_article_comments_with_first() {
    Article article = Article.newBuilder().slug("test-slug").build();
    when(dfe.getSource()).thenReturn(article);

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
            new ArrayList<>(),
            new ProfileData("userId", "author", "bio", "image", false));
    Map<String, ArticleData> map = new HashMap<>();
    map.put("test-slug", articleData);
    when(dfe.getLocalContext()).thenReturn(map);

    CommentData c1 = createCommentData("c1");
    CursorPager<CommentData> pager = new CursorPager<>(Arrays.asList(c1), Direction.NEXT, false);
    when(commentQueryService.findByArticleIdWithCursor(eq("article-id"), eq(user), any()))
        .thenReturn(pager);

    DataFetcherResult<CommentsConnection> result =
        commentDatafetcher.articleComments(10, null, null, null, dfe);

    assertNotNull(result);
    assertEquals(1, result.getData().getEdges().size());
    assertEquals("c1", result.getData().getEdges().get(0).getNode().getId());
  }

  @Test
  void should_get_article_comments_with_last() {
    Article article = Article.newBuilder().slug("test-slug").build();
    when(dfe.getSource()).thenReturn(article);

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
            new ArrayList<>(),
            new ProfileData("userId", "author", "bio", "image", false));
    Map<String, ArticleData> map = new HashMap<>();
    map.put("test-slug", articleData);
    when(dfe.getLocalContext()).thenReturn(map);

    CursorPager<CommentData> pager = new CursorPager<>(new ArrayList<>(), Direction.PREV, false);
    when(commentQueryService.findByArticleIdWithCursor(eq("article-id"), eq(user), any()))
        .thenReturn(pager);

    DataFetcherResult<CommentsConnection> result =
        commentDatafetcher.articleComments(null, null, 5, null, dfe);

    assertNotNull(result);
    assertEquals(0, result.getData().getEdges().size());
  }

  @Test
  void should_throw_when_article_comments_has_no_first_or_last() {
    assertThrows(
        IllegalArgumentException.class,
        () -> commentDatafetcher.articleComments(null, null, null, null, dfe));
  }

  @Test
  void should_get_article_comments_with_has_next() {
    Article article = Article.newBuilder().slug("test-slug").build();
    when(dfe.getSource()).thenReturn(article);

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
            new ArrayList<>(),
            new ProfileData("userId", "author", "bio", "image", false));
    Map<String, ArticleData> map = new HashMap<>();
    map.put("test-slug", articleData);
    when(dfe.getLocalContext()).thenReturn(map);

    CommentData c1 = createCommentData("c1");
    CommentData c2 = createCommentData("c2");
    CursorPager<CommentData> pager = new CursorPager<>(Arrays.asList(c1, c2), Direction.NEXT, true);
    when(commentQueryService.findByArticleIdWithCursor(eq("article-id"), eq(user), any()))
        .thenReturn(pager);

    DataFetcherResult<CommentsConnection> result =
        commentDatafetcher.articleComments(2, null, null, null, dfe);

    assertNotNull(result);
    assertEquals(2, result.getData().getEdges().size());
    assertTrue(result.getData().getPageInfo().isHasNextPage());
  }
}
