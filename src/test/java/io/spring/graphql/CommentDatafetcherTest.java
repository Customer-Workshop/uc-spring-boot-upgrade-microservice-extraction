package io.spring.graphql;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import com.netflix.graphql.dgs.DgsDataFetchingEnvironment;
import graphql.execution.DataFetcherResult;
import graphql.schema.DataFetchingEnvironment;
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
import java.util.HashMap;
import java.util.Map;
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
public class CommentDatafetcherTest {

  @Mock private CommentQueryService commentQueryService;
  @Mock private DataFetchingEnvironment dataFetchingEnvironment;

  private CommentDatafetcher commentDatafetcher;
  private User user;
  private CommentData commentData;
  private ArticleData articleData;

  @BeforeEach
  void setUp() {
    commentDatafetcher = new CommentDatafetcher(commentQueryService);
    user = new User("test@example.com", "testuser", "password", "bio", "image");

    DateTime now = new DateTime();
    commentData =
        new CommentData(
            "comment-id",
            "Test comment body",
            "article-id",
            now,
            now,
            new ProfileData(user.getId(), "testuser", "bio", "image", false));

    articleData =
        new ArticleData(
            "article-id",
            "test-article",
            "Test Article",
            "description",
            "body",
            false,
            0,
            now,
            now,
            asList("java"),
            new ProfileData(user.getId(), "testuser", "bio", "image", false));
  }

  @AfterEach
  void tearDown() {
    SecurityContextHolder.clearContext();
  }

  private void setAuthenticatedUser(User user) {
    SecurityContextHolder.getContext()
        .setAuthentication(new TestingAuthenticationToken(user, null));
  }

  private void setAnonymousUser() {
    SecurityContextHolder.getContext()
        .setAuthentication(
            new AnonymousAuthenticationToken(
                "key", "anonymous", AuthorityUtils.createAuthorityList("ROLE_ANONYMOUS")));
  }

  private DgsDataFetchingEnvironment dgsEnv() {
    return new DgsDataFetchingEnvironment(dataFetchingEnvironment);
  }

  @Test
  void getComment_returnsCommentFromLocalContext() {
    when(dataFetchingEnvironment.getLocalContext()).thenReturn(commentData);

    DataFetcherResult<Comment> result = commentDatafetcher.getComment(dgsEnv());

    assertNotNull(result);
    assertNotNull(result.getData());
    assertEquals("comment-id", result.getData().getId());
    assertEquals("Test comment body", result.getData().getBody());
    assertNotNull(result.getData().getCreatedAt());
    assertNotNull(result.getData().getUpdatedAt());
  }

  @Test
  void getComment_setsLocalContextWithCommentData() {
    when(dataFetchingEnvironment.getLocalContext()).thenReturn(commentData);

    DataFetcherResult<Comment> result = commentDatafetcher.getComment(dgsEnv());

    assertNotNull(result.getLocalContext());
    assertTrue(result.getLocalContext() instanceof Map);
  }

  @Test
  void articleComments_withFirst_returnsCommentsConnection() {
    setAuthenticatedUser(user);
    Article article = Article.newBuilder().slug("test-article").build();
    Map<String, ArticleData> map = new HashMap<>();
    map.put("test-article", articleData);

    when(dataFetchingEnvironment.getSource()).thenReturn(article);
    when(dataFetchingEnvironment.getLocalContext()).thenReturn(map);

    CursorPager<CommentData> pager = new CursorPager<>(asList(commentData), Direction.NEXT, false);
    when(commentQueryService.findByArticleIdWithCursor(eq("article-id"), eq(user), any()))
        .thenReturn(pager);

    DataFetcherResult<CommentsConnection> result =
        commentDatafetcher.articleComments(10, null, null, null, dgsEnv());

    assertNotNull(result);
    assertNotNull(result.getData());
    assertEquals(1, result.getData().getEdges().size());
    assertEquals("comment-id", result.getData().getEdges().get(0).getNode().getId());
  }

  @Test
  void articleComments_withLast_returnsCommentsConnection() {
    setAuthenticatedUser(user);
    Article article = Article.newBuilder().slug("test-article").build();
    Map<String, ArticleData> map = new HashMap<>();
    map.put("test-article", articleData);

    when(dataFetchingEnvironment.getSource()).thenReturn(article);
    when(dataFetchingEnvironment.getLocalContext()).thenReturn(map);

    CursorPager<CommentData> pager = new CursorPager<>(asList(commentData), Direction.PREV, false);
    when(commentQueryService.findByArticleIdWithCursor(eq("article-id"), eq(user), any()))
        .thenReturn(pager);

    DataFetcherResult<CommentsConnection> result =
        commentDatafetcher.articleComments(null, null, 5, null, dgsEnv());

    assertNotNull(result);
    assertEquals(1, result.getData().getEdges().size());
  }

  @Test
  void articleComments_withoutFirstOrLast_throwsException() {
    assertThrows(
        IllegalArgumentException.class,
        () -> commentDatafetcher.articleComments(null, null, null, null, dgsEnv()));
  }

  @Test
  void articleComments_withEmptyResult_returnsEmptyConnection() {
    setAuthenticatedUser(user);
    Article article = Article.newBuilder().slug("test-article").build();
    Map<String, ArticleData> map = new HashMap<>();
    map.put("test-article", articleData);

    when(dataFetchingEnvironment.getSource()).thenReturn(article);
    when(dataFetchingEnvironment.getLocalContext()).thenReturn(map);

    CursorPager<CommentData> pager = new CursorPager<>(emptyList(), Direction.NEXT, false);
    when(commentQueryService.findByArticleIdWithCursor(eq("article-id"), eq(user), any()))
        .thenReturn(pager);

    DataFetcherResult<CommentsConnection> result =
        commentDatafetcher.articleComments(10, null, null, null, dgsEnv());

    assertNotNull(result);
    assertTrue(result.getData().getEdges().isEmpty());
  }

  @Test
  void articleComments_withHasNext_setsPageInfoCorrectly() {
    setAuthenticatedUser(user);
    Article article = Article.newBuilder().slug("test-article").build();
    Map<String, ArticleData> map = new HashMap<>();
    map.put("test-article", articleData);

    when(dataFetchingEnvironment.getSource()).thenReturn(article);
    when(dataFetchingEnvironment.getLocalContext()).thenReturn(map);

    CursorPager<CommentData> pager = new CursorPager<>(asList(commentData), Direction.NEXT, true);
    when(commentQueryService.findByArticleIdWithCursor(eq("article-id"), eq(user), any()))
        .thenReturn(pager);

    DataFetcherResult<CommentsConnection> result =
        commentDatafetcher.articleComments(10, null, null, null, dgsEnv());

    assertNotNull(result.getData().getPageInfo());
    assertTrue(result.getData().getPageInfo().isHasNextPage());
    assertFalse(result.getData().getPageInfo().isHasPreviousPage());
  }

  @Test
  void articleComments_withoutAuth_usesNullUser() {
    setAnonymousUser();
    Article article = Article.newBuilder().slug("test-article").build();
    Map<String, ArticleData> map = new HashMap<>();
    map.put("test-article", articleData);

    when(dataFetchingEnvironment.getSource()).thenReturn(article);
    when(dataFetchingEnvironment.getLocalContext()).thenReturn(map);

    CursorPager<CommentData> pager = new CursorPager<>(asList(commentData), Direction.NEXT, false);
    when(commentQueryService.findByArticleIdWithCursor(eq("article-id"), eq(null), any()))
        .thenReturn(pager);

    DataFetcherResult<CommentsConnection> result =
        commentDatafetcher.articleComments(10, null, null, null, dgsEnv());

    assertNotNull(result);
  }

  @Test
  void articleComments_setsLocalContextWithCommentMap() {
    setAuthenticatedUser(user);
    Article article = Article.newBuilder().slug("test-article").build();
    Map<String, ArticleData> map = new HashMap<>();
    map.put("test-article", articleData);

    when(dataFetchingEnvironment.getSource()).thenReturn(article);
    when(dataFetchingEnvironment.getLocalContext()).thenReturn(map);

    CursorPager<CommentData> pager = new CursorPager<>(asList(commentData), Direction.NEXT, false);
    when(commentQueryService.findByArticleIdWithCursor(eq("article-id"), eq(user), any()))
        .thenReturn(pager);

    DataFetcherResult<CommentsConnection> result =
        commentDatafetcher.articleComments(10, null, null, null, dgsEnv());

    assertNotNull(result.getLocalContext());
  }
}
