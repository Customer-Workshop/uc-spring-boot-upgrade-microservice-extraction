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
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
public class CommentDatafetcherTest {

  @Mock private CommentQueryService commentQueryService;

  @InjectMocks private CommentDatafetcher commentDatafetcher;

  private User user;
  private ProfileData profileData;

  @BeforeEach
  void setUp() {
    user = new User("test@example.com", "testuser", "password", "bio", "image");
    profileData = new ProfileData(user.getId(), "testuser", "bio", "image", false);
    SecurityContextHolder.getContext()
        .setAuthentication(new TestingAuthenticationToken(user, null));
  }

  @AfterEach
  void tearDown() {
    SecurityContextHolder.clearContext();
  }

  @Test
  void should_get_comment_from_payload() {
    DateTime now = new DateTime();
    CommentData commentData =
        new CommentData("comment-id", "body", "article-id", now, now, profileData);

    DgsDataFetchingEnvironment dfe = mock(DgsDataFetchingEnvironment.class);
    when(dfe.getLocalContext()).thenReturn(commentData);

    DataFetcherResult<Comment> result = commentDatafetcher.getComment(dfe);

    assertNotNull(result);
    assertEquals("comment-id", result.getData().getId());
    assertEquals("body", result.getData().getBody());
  }

  @Test
  void should_get_article_comments_with_first() {
    DateTime now = new DateTime();
    CommentData cd1 = new CommentData("c1", "body1", "a1", now, now, profileData);
    CommentData cd2 = new CommentData("c2", "body2", "a1", now, now, profileData);
    CursorPager<CommentData> pager =
        new CursorPager<>(Arrays.asList(cd1, cd2), Direction.NEXT, false);

    ArticleData articleData =
        new ArticleData(
            "a1", "test-slug", "title", "desc", "body", false, 0, now, now, null, profileData);
    Map<String, ArticleData> map = new HashMap<>();
    map.put("test-slug", articleData);

    Article article = Article.newBuilder().slug("test-slug").build();

    DgsDataFetchingEnvironment dfe = mock(DgsDataFetchingEnvironment.class);
    when(dfe.getSource()).thenReturn(article);
    when(dfe.getLocalContext()).thenReturn(map);
    when(commentQueryService.findByArticleIdWithCursor(eq("a1"), any(), any())).thenReturn(pager);

    DataFetcherResult<CommentsConnection> result =
        commentDatafetcher.articleComments(10, null, null, null, dfe);

    assertNotNull(result);
    assertEquals(2, result.getData().getEdges().size());
  }

  @Test
  void should_get_article_comments_with_last() {
    DateTime now = new DateTime();
    CommentData cd1 = new CommentData("c1", "body1", "a1", now, now, profileData);
    CursorPager<CommentData> pager = new CursorPager<>(Arrays.asList(cd1), Direction.PREV, false);

    ArticleData articleData =
        new ArticleData(
            "a1", "test-slug", "title", "desc", "body", false, 0, now, now, null, profileData);
    Map<String, ArticleData> map = new HashMap<>();
    map.put("test-slug", articleData);

    Article article = Article.newBuilder().slug("test-slug").build();

    DgsDataFetchingEnvironment dfe = mock(DgsDataFetchingEnvironment.class);
    when(dfe.getSource()).thenReturn(article);
    when(dfe.getLocalContext()).thenReturn(map);
    when(commentQueryService.findByArticleIdWithCursor(eq("a1"), any(), any())).thenReturn(pager);

    DataFetcherResult<CommentsConnection> result =
        commentDatafetcher.articleComments(null, null, 5, null, dfe);

    assertNotNull(result);
    assertEquals(1, result.getData().getEdges().size());
  }

  @Test
  void should_throw_when_first_and_last_both_null() {
    DgsDataFetchingEnvironment dfe = mock(DgsDataFetchingEnvironment.class);

    assertThrows(
        IllegalArgumentException.class,
        () -> commentDatafetcher.articleComments(null, null, null, null, dfe));
  }

  @Test
  void should_return_empty_comments() {
    CursorPager<CommentData> pager = new CursorPager<>(new ArrayList<>(), Direction.NEXT, false);

    DateTime now = new DateTime();
    ArticleData articleData =
        new ArticleData(
            "a1", "test-slug", "title", "desc", "body", false, 0, now, now, null, profileData);
    Map<String, ArticleData> map = new HashMap<>();
    map.put("test-slug", articleData);

    Article article = Article.newBuilder().slug("test-slug").build();

    DgsDataFetchingEnvironment dfe = mock(DgsDataFetchingEnvironment.class);
    when(dfe.getSource()).thenReturn(article);
    when(dfe.getLocalContext()).thenReturn(map);
    when(commentQueryService.findByArticleIdWithCursor(eq("a1"), any(), any())).thenReturn(pager);

    DataFetcherResult<CommentsConnection> result =
        commentDatafetcher.articleComments(10, null, null, null, dfe);

    assertNotNull(result);
    assertTrue(result.getData().getEdges().isEmpty());
  }
}
