package io.spring.graphql;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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

  private CommentDatafetcher commentDatafetcher;
  private User user;

  @BeforeEach
  void setUp() {
    commentDatafetcher = new CommentDatafetcher(commentQueryService);
    user = new User("test@test.com", "testuser", "password", "bio", "image");
    SecurityContextHolder.getContext()
        .setAuthentication(new TestingAuthenticationToken(user, null));
  }

  @AfterEach
  void tearDown() {
    SecurityContextHolder.clearContext();
  }

  @Test
  void should_get_comment_from_comment_payload() {
    DgsDataFetchingEnvironment dfe = mock(DgsDataFetchingEnvironment.class);
    DateTime now = new DateTime();
    CommentData commentData =
        new CommentData(
            "comment1",
            "comment body",
            "article1",
            now,
            now,
            new ProfileData("userId", "username", "bio", "image", false));
    when(dfe.getLocalContext()).thenReturn(commentData);

    DataFetcherResult<Comment> result = commentDatafetcher.getComment(dfe);

    assertNotNull(result);
    assertNotNull(result.getData());
    assertEquals("comment1", result.getData().getId());
    assertEquals("comment body", result.getData().getBody());
    assertNotNull(result.getData().getCreatedAt());
    assertNotNull(result.getData().getUpdatedAt());
  }

  @Test
  void should_get_comment_with_local_context() {
    DgsDataFetchingEnvironment dfe = mock(DgsDataFetchingEnvironment.class);
    DateTime now = new DateTime();
    CommentData commentData =
        new CommentData(
            "comment1",
            "body",
            "article1",
            now,
            now,
            new ProfileData("userId", "username", "bio", "image", false));
    when(dfe.getLocalContext()).thenReturn(commentData);

    DataFetcherResult<Comment> result = commentDatafetcher.getComment(dfe);

    assertNotNull(result.getLocalContext());
  }

  @Test
  void should_get_article_comments_with_first_parameter() {
    DgsDataFetchingEnvironment dfe = mock(DgsDataFetchingEnvironment.class);

    Article article = Article.newBuilder().slug("test-slug").build();
    when(dfe.getSource()).thenReturn(article);

    DateTime now = new DateTime();
    ArticleData articleData =
        new ArticleData(
            "article1",
            "test-slug",
            "title",
            "desc",
            "body",
            false,
            0,
            now,
            now,
            new ArrayList<>(),
            new ProfileData("userId", "username", "bio", "image", false));
    Map<String, ArticleData> map = new HashMap<>();
    map.put("test-slug", articleData);
    when(dfe.getLocalContext()).thenReturn(map);

    CommentData commentData =
        new CommentData(
            "comment1",
            "body",
            "article1",
            now,
            now,
            new ProfileData("userId", "username", "bio", "image", false));
    CursorPager<CommentData> cursorPager =
        new CursorPager<>(Arrays.asList(commentData), Direction.NEXT, false);
    when(commentQueryService.findByArticleIdWithCursor(any(), any(), any()))
        .thenReturn(cursorPager);

    DataFetcherResult<CommentsConnection> result =
        commentDatafetcher.articleComments(10, null, null, null, dfe);

    assertNotNull(result);
    assertNotNull(result.getData());
    assertEquals(1, result.getData().getEdges().size());
    assertEquals("comment1", result.getData().getEdges().get(0).getNode().getId());
  }

  @Test
  void should_get_article_comments_with_last_parameter() {
    DgsDataFetchingEnvironment dfe = mock(DgsDataFetchingEnvironment.class);

    Article article = Article.newBuilder().slug("test-slug").build();
    when(dfe.getSource()).thenReturn(article);

    DateTime now = new DateTime();
    ArticleData articleData =
        new ArticleData(
            "article1",
            "test-slug",
            "title",
            "desc",
            "body",
            false,
            0,
            now,
            now,
            new ArrayList<>(),
            new ProfileData("userId", "username", "bio", "image", false));
    Map<String, ArticleData> map = new HashMap<>();
    map.put("test-slug", articleData);
    when(dfe.getLocalContext()).thenReturn(map);

    CommentData commentData =
        new CommentData(
            "comment1",
            "body",
            "article1",
            now,
            now,
            new ProfileData("userId", "username", "bio", "image", false));
    CursorPager<CommentData> cursorPager =
        new CursorPager<>(Arrays.asList(commentData), Direction.PREV, false);
    when(commentQueryService.findByArticleIdWithCursor(any(), any(), any()))
        .thenReturn(cursorPager);

    DataFetcherResult<CommentsConnection> result =
        commentDatafetcher.articleComments(null, null, 5, null, dfe);

    assertNotNull(result);
    assertEquals(1, result.getData().getEdges().size());
  }

  @Test
  void should_throw_when_article_comments_has_no_first_or_last() {
    DgsDataFetchingEnvironment dfe = mock(DgsDataFetchingEnvironment.class);
    assertThrows(
        IllegalArgumentException.class,
        () -> commentDatafetcher.articleComments(null, null, null, null, dfe));
  }

  @Test
  void should_get_article_comments_empty_result() {
    DgsDataFetchingEnvironment dfe = mock(DgsDataFetchingEnvironment.class);

    Article article = Article.newBuilder().slug("test-slug").build();
    when(dfe.getSource()).thenReturn(article);

    DateTime now = new DateTime();
    ArticleData articleData =
        new ArticleData(
            "article1",
            "test-slug",
            "title",
            "desc",
            "body",
            false,
            0,
            now,
            now,
            new ArrayList<>(),
            new ProfileData("userId", "username", "bio", "image", false));
    Map<String, ArticleData> map = new HashMap<>();
    map.put("test-slug", articleData);
    when(dfe.getLocalContext()).thenReturn(map);

    CursorPager<CommentData> cursorPager =
        new CursorPager<>(new ArrayList<>(), Direction.NEXT, false);
    when(commentQueryService.findByArticleIdWithCursor(any(), any(), any()))
        .thenReturn(cursorPager);

    DataFetcherResult<CommentsConnection> result =
        commentDatafetcher.articleComments(10, null, null, null, dfe);

    assertNotNull(result);
    assertEquals(0, result.getData().getEdges().size());
  }

  @Test
  void should_get_article_comments_with_cursor_params() {
    DgsDataFetchingEnvironment dfe = mock(DgsDataFetchingEnvironment.class);

    Article article = Article.newBuilder().slug("test-slug").build();
    when(dfe.getSource()).thenReturn(article);

    DateTime now = new DateTime();
    ArticleData articleData =
        new ArticleData(
            "article1",
            "test-slug",
            "title",
            "desc",
            "body",
            false,
            0,
            now,
            now,
            new ArrayList<>(),
            new ProfileData("userId", "username", "bio", "image", false));
    Map<String, ArticleData> map = new HashMap<>();
    map.put("test-slug", articleData);
    when(dfe.getLocalContext()).thenReturn(map);

    CommentData commentData =
        new CommentData(
            "comment1",
            "body",
            "article1",
            now,
            now,
            new ProfileData("userId", "username", "bio", "image", false));
    CursorPager<CommentData> cursorPager =
        new CursorPager<>(Arrays.asList(commentData), Direction.NEXT, true);
    when(commentQueryService.findByArticleIdWithCursor(any(), any(), any()))
        .thenReturn(cursorPager);

    String afterCursor = String.valueOf(now.getMillis());
    DataFetcherResult<CommentsConnection> result =
        commentDatafetcher.articleComments(10, afterCursor, null, null, dfe);

    assertNotNull(result);
    assertNotNull(result.getData().getPageInfo());
  }

  @Test
  void should_get_article_comments_without_authentication() {
    SecurityContextHolder.getContext()
        .setAuthentication(
            new AnonymousAuthenticationToken(
                "key", "anonymous", AuthorityUtils.createAuthorityList("ROLE_ANONYMOUS")));
    DgsDataFetchingEnvironment dfe = mock(DgsDataFetchingEnvironment.class);

    Article article = Article.newBuilder().slug("test-slug").build();
    when(dfe.getSource()).thenReturn(article);

    DateTime now = new DateTime();
    ArticleData articleData =
        new ArticleData(
            "article1",
            "test-slug",
            "title",
            "desc",
            "body",
            false,
            0,
            now,
            now,
            new ArrayList<>(),
            new ProfileData("userId", "username", "bio", "image", false));
    Map<String, ArticleData> map = new HashMap<>();
    map.put("test-slug", articleData);
    when(dfe.getLocalContext()).thenReturn(map);

    CursorPager<CommentData> cursorPager =
        new CursorPager<>(new ArrayList<>(), Direction.NEXT, false);
    when(commentQueryService.findByArticleIdWithCursor(any(), any(), any()))
        .thenReturn(cursorPager);

    DataFetcherResult<CommentsConnection> result =
        commentDatafetcher.articleComments(10, null, null, null, dfe);

    assertNotNull(result);
  }
}
