package io.spring.graphql;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.netflix.graphql.dgs.DgsDataFetchingEnvironment;
import graphql.execution.DataFetcherResult;
import io.spring.application.CommentQueryService;
import io.spring.application.CursorPager;
import io.spring.application.data.ArticleData;
import io.spring.application.data.CommentData;
import io.spring.application.data.ProfileData;
import io.spring.core.user.User;
import io.spring.graphql.types.Article;
import io.spring.graphql.types.Comment;
import io.spring.graphql.types.CommentsConnection;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import org.joda.time.DateTime;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

public class CommentDatafetcherTest {

  private CommentQueryService commentQueryService;
  private CommentDatafetcher commentDatafetcher;
  private User user;

  @BeforeEach
  public void setUp() {
    commentQueryService = Mockito.mock(CommentQueryService.class);
    commentDatafetcher = new CommentDatafetcher(commentQueryService);
    user = new User("test@test.com", "testuser", "123", "", "");
    SecurityContextHolder.getContext()
        .setAuthentication(new TestingAuthenticationToken(user, null));
  }

  @AfterEach
  public void tearDown() {
    SecurityContextHolder.clearContext();
  }

  @Test
  public void should_get_comment_from_payload() {
    ProfileData profile = new ProfileData(user.getId(), user.getUsername(), "", "", false);
    CommentData commentData =
        new CommentData(
            "cid", "comment body", "article-id", new DateTime(), new DateTime(), profile);

    DgsDataFetchingEnvironment dfe = Mockito.mock(DgsDataFetchingEnvironment.class);
    when(dfe.getLocalContext()).thenReturn(commentData);

    DataFetcherResult<Comment> result = commentDatafetcher.getComment(dfe);
    assertNotNull(result);
    assertEquals("cid", result.getData().getId());
    assertEquals("comment body", result.getData().getBody());
  }

  @Test
  public void should_get_article_comments_with_first() {
    ProfileData profile = new ProfileData(user.getId(), user.getUsername(), "", "", false);
    CommentData commentData =
        new CommentData("cid", "body", "article-id", new DateTime(), new DateTime(), profile);
    CursorPager<CommentData> pager =
        new CursorPager<>(Arrays.asList(commentData), CursorPager.Direction.NEXT, false);
    when(commentQueryService.findByArticleIdWithCursor(any(), any(), any())).thenReturn(pager);

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
            new java.util.ArrayList<>(),
            profile);
    Map<String, ArticleData> map = new HashMap<>();
    map.put("test-slug", articleData);

    DgsDataFetchingEnvironment dfe = Mockito.mock(DgsDataFetchingEnvironment.class);
    when(dfe.getLocalContext()).thenReturn(map);
    Article article = Article.newBuilder().slug("test-slug").build();
    when(dfe.getSource()).thenReturn(article);

    DataFetcherResult<CommentsConnection> result =
        commentDatafetcher.articleComments(10, null, null, null, dfe);
    assertNotNull(result);
    assertEquals(1, result.getData().getEdges().size());
  }

  @Test
  public void should_get_article_comments_with_last() {
    ProfileData profile = new ProfileData(user.getId(), user.getUsername(), "", "", false);
    CommentData commentData =
        new CommentData("cid", "body", "article-id", new DateTime(), new DateTime(), profile);
    CursorPager<CommentData> pager =
        new CursorPager<>(Arrays.asList(commentData), CursorPager.Direction.PREV, false);
    when(commentQueryService.findByArticleIdWithCursor(any(), any(), any())).thenReturn(pager);

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
            new java.util.ArrayList<>(),
            profile);
    Map<String, ArticleData> map = new HashMap<>();
    map.put("test-slug", articleData);

    DgsDataFetchingEnvironment dfe = Mockito.mock(DgsDataFetchingEnvironment.class);
    when(dfe.getLocalContext()).thenReturn(map);
    Article article = Article.newBuilder().slug("test-slug").build();
    when(dfe.getSource()).thenReturn(article);

    DataFetcherResult<CommentsConnection> result =
        commentDatafetcher.articleComments(null, null, 10, null, dfe);
    assertNotNull(result);
  }

  @Test
  public void should_throw_when_first_and_last_both_null() {
    DgsDataFetchingEnvironment dfe = Mockito.mock(DgsDataFetchingEnvironment.class);
    assertThrows(
        IllegalArgumentException.class,
        () -> commentDatafetcher.articleComments(null, null, null, null, dfe));
  }

  @Test
  public void should_build_comment_page_info_with_null_cursors() {
    CursorPager<CommentData> pager =
        new CursorPager<>(Arrays.asList(), CursorPager.Direction.NEXT, false);
    when(commentQueryService.findByArticleIdWithCursor(any(), any(), any())).thenReturn(pager);

    ArticleData articleData =
        new ArticleData(
            "aid",
            "slug",
            "t",
            "d",
            "b",
            false,
            0,
            new DateTime(),
            new DateTime(),
            new java.util.ArrayList<>(),
            new ProfileData(user.getId(), user.getUsername(), "", "", false));
    Map<String, ArticleData> map = new HashMap<>();
    map.put("slug", articleData);

    DgsDataFetchingEnvironment dfe = Mockito.mock(DgsDataFetchingEnvironment.class);
    when(dfe.getLocalContext()).thenReturn(map);
    Article article = Article.newBuilder().slug("slug").build();
    when(dfe.getSource()).thenReturn(article);

    DataFetcherResult<CommentsConnection> result =
        commentDatafetcher.articleComments(10, null, null, null, dfe);
    assertNotNull(result);
    assertNull(result.getData().getPageInfo().getStartCursor());
    assertNull(result.getData().getPageInfo().getEndCursor());
  }
}
